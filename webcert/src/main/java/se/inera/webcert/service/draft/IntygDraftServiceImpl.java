package se.inera.webcert.service.draft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.CreateNewDraftHolder;
import se.inera.certificate.modules.support.api.dto.HoSPersonal;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.DraftValidationStatus;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.draft.util.CreateIntygsIdStrategy;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.dto.Patient;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;

@Service
public class IntygDraftServiceImpl implements IntygDraftService {
    
    private static final List<IntygsStatus> ALL_DRAFTS = Arrays.asList(IntygsStatus.DRAFT_COMPLETE,
            IntygsStatus.DRAFT_INCOMPLETE);
    
    private static Logger LOG = LoggerFactory.getLogger(IntygDraftServiceImpl.class);

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CreateIntygsIdStrategy intygsIdStrategy;

    public IntygDraftServiceImpl() {

    }

    @Override
    @Transactional
    public String createNewDraft(CreateNewDraftRequest request) {

        populateRequestWithIntygId(request);

        String intygType = request.getIntygType();

        CreateNewDraftHolder draftRequest = createModuleRequest(request);

        String intygJsonModel = getPopulatedModelFromIntygModule(intygType, draftRequest);

        String persistedIntygId = persistNewDraft(request, intygJsonModel);

        return persistedIntygId;
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

    private String persistNewDraft(CreateNewDraftRequest request, String draftAsJson) {

        Intyg draft = new Intyg();

        se.inera.webcert.service.dto.Patient patient = request.getPatient();

        draft.setPatientPersonnummer(patient.getPersonNummer());
        draft.setPatientFornamn(patient.getForNamn());
        draft.setPatientEfternamn(patient.getEfterNamn());

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

        return savedDraft.getIntygsId();
    }

    private VardpersonReferens createVardpersonFromHosPerson(HoSPerson hosPerson) {

        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(hosPerson.getNamn());
        vardPerson.setHsaId(hosPerson.getHsaId());

        return vardPerson;
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
                reqVardenhet.getPostnummer(), reqVardenhet.getPostort(), reqVardenhet.getTelefonnummer(), reqVardenhet.getEpost(), vardgivare);

        HoSPerson reqHosPerson = request.getHosPerson();
        HoSPersonal hosPerson = new HoSPersonal(reqHosPerson.getHsaId(), reqHosPerson.getNamn(),
                reqHosPerson.getForskrivarkod(), reqHosPerson.getBefattning(), vardenhet);

        Patient reqPatient = request.getPatient();

        se.inera.certificate.modules.support.api.dto.Patient patient = new se.inera.certificate.modules.support.api.dto.Patient(reqPatient.getForNamn(), 
                reqPatient.getEfterNamn(), reqPatient.getPersonNummer(), reqPatient.getPostAdress(), reqPatient.getPostNummer(), reqPatient.getPostOrt());
        
        return new CreateNewDraftHolder(request.getIntygId(), hosPerson, patient);
    }

    @Override
    @Transactional
    public DraftValidation saveAndValidateDraft(SaveAndValidateDraftRequest request) {

        String intygId = request.getIntygId();

        LOG.debug("Saving and validating Intyg with id '{}'", intygId);

        Intyg intyg = intygRepository.findOne(intygId);

        if (intyg == null) {
            LOG.warn("Intyg with id '{}' was not found", intygId);
            // TODO Throw exception perhaps?
            return null;
        }

        String intygType = intyg.getIntygsTyp();

        String draftAsJson = request.getDraftAsJson();

        DraftValidation draftValidation = validateDraft(intygId, intygType, draftAsJson);

        IntygsStatus intygStatus = (draftValidation.isDraftValid()) ? IntygsStatus.DRAFT_COMPLETE
                : IntygsStatus.DRAFT_INCOMPLETE;

        intyg.setModel(draftAsJson);
        intyg.setStatus(intygStatus);

        VardpersonReferens vardPersonRef = createVardpersonFromHosPerson(request.getSavedBy());
        intyg.setSenastSparadAv(vardPersonRef);

        intygRepository.save(intyg);

        LOG.debug("Intyg '{}' updated", intygId);

        return draftValidation;
    }

    @Override
    public DraftValidation validateDraft(String intygId, String intygType, String draftAsJson) {
        
        DraftValidation draftValidation;
        
        LOG.debug("Validating Intyg with id {} and type {}", intygId, intygType);

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
        
        List<Object[]> result = intygRepository.findDistinctLakareFromIntygEnhetAndStatuses(enhetsId, ALL_DRAFTS);
        
        for (Object[] lakareArr : result) {
            lakareList.add(new Lakare((String) lakareArr[0], (String) lakareArr[1]));
        }
        
        return lakareList;
    }

    @Override
    public Intyg setForwardOnDraft(String intygsId, Boolean forwarded) {

        Intyg intyg = intygRepository.findOne(intygsId);
        
        if (intyg == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "Could not find Intyg with id: " + intygsId);
        }
        
        intyg.setVidarebefordrad(forwarded);
        
        return intygRepository.save(intyg);
    }
    
    public Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds) {
        
        Map<String, Long> resultsMap = new HashMap<String, Long>();
        
        List<Object[]> countResults = intygRepository.countIntygWithStatusesGroupedByEnhetsId(careUnitIds, ALL_DRAFTS);
        
        for (Object[] resultArr : countResults) {
            resultsMap.put((String) resultArr[0], (Long) resultArr[1]);
        }
        
        return resultsMap;
    }

}
