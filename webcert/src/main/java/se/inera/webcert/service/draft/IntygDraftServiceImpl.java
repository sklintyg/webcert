package se.inera.webcert.service.draft;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.CreateNewDraftHolder;
import se.inera.certificate.modules.support.api.dto.ExternalModelHolder;
import se.inera.certificate.modules.support.api.dto.HoSPersonal;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.hsa.model.AbstractVardenhet;
import se.inera.webcert.hsa.model.SelectableVardenhet;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.DraftValidationStatus;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.draft.dto.SignatureTicket;
import se.inera.webcert.service.draft.util.CreateIntygsIdStrategy;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.dto.Patient;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.intyg.dto.IntygContentHolder;
import se.inera.webcert.service.log.LogRequestFactory;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.web.service.WebCertUserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IntygDraftServiceImpl implements IntygDraftService {

    private static final List<IntygsStatus> ALL_DRAFT_STATUSES = Arrays.asList(IntygsStatus.DRAFT_COMPLETE,
            IntygsStatus.DRAFT_INCOMPLETE);

    private static final Logger LOG = LoggerFactory.getLogger(IntygDraftServiceImpl.class);

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CreateIntygsIdStrategy intygsIdStrategy;

    @Autowired
    private IntygService intygService;

    @Autowired
    private LogService logService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private IntygSignatureService signatureService;

    @Override
    @Transactional
    public String createNewDraft(CreateNewDraftRequest request) {

        populateRequestWithIntygId(request);

        String intygType = request.getIntygType();

        CreateNewDraftHolder draftRequest = createModuleRequest(request);

        String intygJsonModel = getPopulatedModelFromIntygModule(intygType, draftRequest);

        Intyg persistedIntyg = persistNewDraft(request, intygJsonModel);

        return persistedIntyg.getIntygsId();
    }

    private void populateRequestWithIntygId(CreateNewDraftRequest request) {

        if (StringUtils.isNotBlank(request.getIntygId())) {
            LOG.debug("Detected that the CreateNewDraftRequest already contains an intygId!");
            return;
        }

        String generatedIntygId = intygsIdStrategy.createId();
        request.setIntygId(generatedIntygId);

        LOG.debug("Created id '{}' for the new draft", generatedIntygId);
    }

    private Intyg persistNewDraft(CreateNewDraftRequest request, String draftAsJson) {

        Intyg draft = new Intyg();

        se.inera.webcert.service.dto.Patient patient = request.getPatient();

        draft.setPatientPersonnummer(patient.getPersonnummer());
        draft.setPatientFornamn(patient.getFornamn());
        draft.setPatientMellannamn(patient.getMellannamn());
        draft.setPatientEfternamn(patient.getEfternamn());

        draft.setIntygsId(request.getIntygId());
        draft.setIntygsTyp(request.getIntygType());

        draft.setStatus(IntygsStatus.DRAFT_INCOMPLETE);

        draft.setModel(draftAsJson);

        Vardenhet vardenhet = request.getVardenhet();

        draft.setEnhetsId(vardenhet.getHsaId());
        draft.setEnhetsNamn(vardenhet.getNamn());

        Vardgivare vardgivare = vardenhet.getVardgivare();

        draft.setVardgivarId(vardgivare.getHsaId());
        draft.setVardgivarNamn(vardgivare.getNamn());

        VardpersonReferens creator = createVardpersonFromHosPerson(request.getHosPerson());

        draft.setSenastSparadAv(creator);
        draft.setSkapadAv(creator);

        Intyg savedDraft = intygRepository.save(draft);

        LOG.debug("Draft '{}' persisted", savedDraft.getIntygsId());

        return savedDraft;
    }

    private String getPopulatedModelFromIntygModule(String intygType, CreateNewDraftHolder draftRequest) {

        LOG.debug("Calling module '{}' to get populated model", intygType);

        String modelAsJson;

        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);

        try {
            InternalModelResponse draftResponse = moduleApi.createNewInternal(draftRequest);
            modelAsJson = draftResponse.getInternalModel();
        } catch (ModuleException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }

        LOG.debug("Got populated model of {} chars from module '{}'", getSafeLength(modelAsJson), intygType);

        return modelAsJson;
    }

    private int getSafeLength(String str) {
        return (StringUtils.isNotBlank(str)) ? str.length() : 0;
    }

    private CreateNewDraftHolder createModuleRequest(CreateNewDraftRequest request) {

        Vardgivare reqVardgivare = request.getVardenhet().getVardgivare();
        se.inera.certificate.modules.support.api.dto.Vardgivare vardgivare = new se.inera.certificate.modules.support.api.dto.Vardgivare(
                reqVardgivare.getHsaId(), reqVardgivare.getNamn());

        Vardenhet reqVardenhet = request.getVardenhet();
        se.inera.certificate.modules.support.api.dto.Vardenhet vardenhet = new se.inera.certificate.modules.support.api.dto.Vardenhet(
                reqVardenhet.getHsaId(), reqVardenhet.getNamn(), reqVardenhet.getPostadress(),
                reqVardenhet.getPostnummer(), reqVardenhet.getPostort(), reqVardenhet.getTelefonnummer(), reqVardenhet.getEpost(), reqVardenhet.getArbetsplatskod(), vardgivare);

        HoSPerson reqHosPerson = request.getHosPerson();
        HoSPersonal hosPerson = new HoSPersonal(reqHosPerson.getHsaId(), reqHosPerson.getNamn(),
                reqHosPerson.getForskrivarkod(), reqHosPerson.getBefattning(), reqHosPerson.getSpecialiseringar(), vardenhet);

        Patient reqPatient = request.getPatient();

        se.inera.certificate.modules.support.api.dto.Patient patient = new se.inera.certificate.modules.support.api.dto.Patient(reqPatient.getFornamn(),
                reqPatient.getMellannamn(), reqPatient.getEfternamn(), reqPatient.getPersonnummer(), reqPatient.getPostadress(), reqPatient.getPostnummer(), reqPatient.getPostort());

        return new CreateNewDraftHolder(request.getIntygId(), hosPerson, patient);
    }

    public Intyg getIntygAsDraft(String intygId) {

        LOG.debug("Fetching Intyg '{}'", intygId);

        Intyg intyg = intygRepository.findOne(intygId);

        if (intyg == null) {
            LOG.warn("Intyg '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The intyg could not be found");
        }

        return intyg;
    }
    
    @Override
    public Intyg getDraft(String intygId) {
        Intyg intyg = getIntygAsDraft(intygId);
        LogRequest logRequest = LogRequestFactory.createLogRequestFromDraft(intyg);
        logService.logReadOfIntyg(logRequest);
        return intyg;
    }

    @Override
    public SignatureTicket createDraftHash(String intygsId) {
        Intyg intyg = getIntygAsDraft(intygsId);
        updateWithUser(intyg);
        intygRepository.save(intyg);
        return signatureService.createDraftHash(intygsId);
    }

    @Override
    public SignatureTicket serverSignature(String intygsId) {
        Intyg intyg = getIntygAsDraft(intygsId);
        updateWithUser(intyg);
        intygRepository.save(intyg);
        return signatureService.serverSignature(intygsId);
    }

    @Override
    public String createNewDraftCopy(CreateNewDraftRequest request, String intygsId) {
        populateRequestWithIntygId(request);

        String intygType = request.getIntygType();

        LOG.debug("Calling module '{}' to get populated model", intygType);

        String modelAsJson;

        try {
            IntygContentHolder template = intygService.fetchExternalIntygData(intygsId);
            String type = template.getMetaData().getType();
            ModuleApi moduleApi = moduleRegistry.getModuleApi(type);
            request.setIntygType(type);
            CreateNewDraftHolder draftRequest = createModuleRequest(request);
            InternalModelResponse draftResponse = moduleApi.createNewInternalFromTemplate(draftRequest, new ExternalModelHolder(template.getContents()));

            modelAsJson = draftResponse.getInternalModel();
        } catch (ModuleException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }

        LOG.debug("Got populated model of {} chars from module '{}'", getSafeLength(modelAsJson), intygType);

        Intyg persistedIntyg = persistNewDraft(request, modelAsJson);

        return persistedIntyg.getIntygsId();
    }

    @Override
    @Transactional
    public DraftValidation saveAndValidateDraft(SaveAndValidateDraftRequest request) {

        String intygId = request.getIntygId();

        LOG.debug("Saving and validating Intyg '{}'", intygId);

        Intyg intyg = intygRepository.findOne(intygId);

        if (intyg == null) {
            LOG.warn("Intyg '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The intyg could not be found");
        }

        // check that the draft is still a draft
        if (!isTheDraftStillADraft(intyg.getStatus())) {
            LOG.error("Intyg '{}' can not be updated since it is no longer a draft", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "This intyg can not be updated since it is no longer a draft");
        }

        String intygType = intyg.getIntygsTyp();

        String draftAsJson = request.getDraftAsJson();

        DraftValidation draftValidation = validateDraft(intygId, intygType, draftAsJson);

        IntygsStatus intygStatus = (draftValidation.isDraftValid()) ? IntygsStatus.DRAFT_COMPLETE : IntygsStatus.DRAFT_INCOMPLETE;

        updateWithUser(intyg, draftAsJson);

        intyg.setStatus(intygStatus);

        Intyg persistedDraft = intygRepository.save(intyg);

        LOG.debug("Draft '{}' updated", persistedDraft.getIntygsId());

        return draftValidation;
    }

    @Override
    public DraftValidation validateDraft(String intygId, String intygType, String draftAsJson) {

        DraftValidation draftValidation;

        LOG.debug("Validating Intyg '{}' with type '{}'", intygId, intygType);

        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);

        try {
            InternalModelHolder intHolder = new InternalModelHolder(draftAsJson);
            ValidateDraftResponse validateDraftResponse = moduleApi.validateDraft(intHolder);

            draftValidation = convertToDraftValidation(validateDraftResponse);

        } catch (ModuleException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }

        return draftValidation;
    }

    private DraftValidation convertToDraftValidation(ValidateDraftResponse dr) {

        DraftValidation draftValidation = new DraftValidation();

        ValidationStatus validationStatus = dr.getStatus();

        if (ValidationStatus.VALID.equals(validationStatus)) {
            LOG.debug("Validation is OK");
            return draftValidation;
        }

        draftValidation.setStatus(DraftValidationStatus.INVALID);

        for (ValidationMessage validationMsg : dr.getValidationErrors()) {
            draftValidation.addMessage(new se.inera.webcert.service.draft.dto.DraftValidationMessage(validationMsg.getField(), validationMsg
                    .getMessage()));
        }

        LOG.debug("Validation failed with {} validation messages", draftValidation.getMessages().size());

        return draftValidation;
    }

    @Override
    public List<Lakare> getLakareWithDraftsByEnhet(String enhetsId) {

        List<Lakare> lakareList = new ArrayList<>();

        List<Object[]> result = intygRepository.findDistinctLakareFromIntygEnhetAndStatuses(enhetsId, ALL_DRAFT_STATUSES);

        for (Object[] lakareArr : result) {
            lakareList.add(new Lakare((String) lakareArr[0], (String) lakareArr[1]));
        }

        return lakareList;
    }

    @Override
    @Transactional
    public Intyg setForwardOnDraft(String intygsId, Boolean forwarded) {

        Intyg intyg = intygRepository.findOne(intygsId);

        if (intyg == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "Could not find Intyg with id: " + intygsId);
        }

        intyg.setVidarebefordrad(forwarded);

        return intygRepository.save(intyg);
    }

    @Override
    public Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds) {

        Map<String, Long> resultsMap = new HashMap<>();

        List<Object[]> countResults = intygRepository.countIntygWithStatusesGroupedByEnhetsId(careUnitIds, ALL_DRAFT_STATUSES);

        for (Object[] resultArr : countResults) {
            resultsMap.put((String) resultArr[0], (Long) resultArr[1]);
        }

        return resultsMap;
    }

    @Override
    @Transactional
    public void deleteUnsignedDraft(String intygId) {

        LOG.debug("Deleting draft with id '{}'", intygId);

        Intyg intyg = intygRepository.findOne(intygId);

        // check that the draft exists
        if (intyg == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The intyg could not be deleted since it could not be found");
        }

        // check that the draft is still unsigned
        if (!isTheDraftStillADraft(intyg.getStatus())) {
            LOG.error("Intyg '{}' can not be deleted since it is no longer a draft", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "The intyg can not be deleted since it is no longer a draft");
        }

        intygRepository.delete(intyg);
    }

    private void updateWithUser(Intyg intyg) {
        updateWithUser(intyg, intyg.getModel());
    }

    private void updateWithUser(Intyg intyg, String draftAsJson) {
        WebCertUser user = webCertUserService.getWebCertUser();
        VardpersonReferens vardPersonRef = createVardpersonFromHosPerson(HoSPerson.create(user));
        intyg.setSenastSparadAv(vardPersonRef);

        SelectableVardenhet valdVardgivare = user.getValdVardgivare();
        se.inera.certificate.modules.support.api.dto.Vardgivare vardgivare = new se.inera.certificate.modules.support.api.dto.Vardgivare(valdVardgivare.getId(), valdVardgivare.getNamn());
        AbstractVardenhet valdVardenhet = (AbstractVardenhet) user.getValdVardenhet();
        se.inera.certificate.modules.support.api.dto.Vardenhet vardenhet = new se.inera.certificate.modules.support.api.dto.Vardenhet(valdVardenhet.getId(), valdVardenhet.getNamn(), valdVardenhet.getPostadress(), valdVardenhet.getPostnummer(), valdVardenhet.getPostort(), valdVardenhet.getTelefonnummer(), valdVardenhet.getEpost(), valdVardenhet.getArbetsplatskod(), vardgivare);
        HoSPersonal hosPerson = new HoSPersonal(user.getHsaId(), user.getNamn(), user.getForskrivarkod(), user.getTitel(), user.getSpecialiseringar(), vardenhet);

        try {
            InternalModelHolder internalModel = new InternalModelHolder(draftAsJson);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intyg.getIntygsTyp());
            InternalModelResponse updatedInternal = moduleApi.updateInternal(internalModel, hosPerson, LocalDateTime.now());
            intyg.setModel(updatedInternal.getInternalModel());
        } catch (ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not update with HoS personal", e);
        }
    }

    private VardpersonReferens createVardpersonFromHosPerson(HoSPerson hosPerson) {
        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(hosPerson.getNamn());
        vardPerson.setHsaId(hosPerson.getHsaId());

        return vardPerson;
    }

    private boolean isTheDraftStillADraft(IntygsStatus intygStatus) {
        return ALL_DRAFT_STATUSES.contains(intygStatus);
    }
}
