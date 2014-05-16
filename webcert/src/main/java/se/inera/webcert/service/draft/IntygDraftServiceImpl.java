package se.inera.webcert.service.draft;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
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
import se.inera.webcert.eid.services.SignatureService;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.modules.IntygModuleRegistry;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;
import se.inera.webcert.persistence.intyg.model.Signatur;
import se.inera.webcert.persistence.intyg.model.VardpersonReferens;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;
import se.inera.webcert.persistence.intyg.repository.SignaturRepository;
import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.draft.dto.CreateNewDraftRequest;
import se.inera.webcert.service.draft.dto.DraftValidation;
import se.inera.webcert.service.draft.dto.DraftValidationStatus;
import se.inera.webcert.service.draft.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.draft.dto.SigneringsBiljett;
import se.inera.webcert.service.draft.util.CreateIntygsIdStrategy;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.dto.Patient;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.log.dto.LogRequest;
import se.inera.webcert.web.service.WebCertUserService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IntygDraftServiceImpl implements IntygDraftService {

    private static final List<IntygsStatus> ALL_DRAFT_STATUSES = Arrays.asList(IntygsStatus.DRAFT_COMPLETE,
            IntygsStatus.DRAFT_INCOMPLETE);

    private static final Logger LOG = LoggerFactory.getLogger(IntygDraftServiceImpl.class);

    @Autowired
    private IntygRepository intygRepository;

    @Autowired
    private SignaturRepository signaturRepository;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private CreateIntygsIdStrategy intygsIdStrategy;

    @Autowired
    private LogService logService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private BiljettTracker biljettTracker;

    @Autowired
    private IntygService intygService;

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private ObjectMapper objectMapper;

    public IntygDraftServiceImpl() {

    }

    @Override
    @Transactional
    public String createNewDraft(CreateNewDraftRequest request) {

        populateRequestWithIntygId(request);

        String intygType = request.getIntygType();

        CreateNewDraftHolder draftRequest = createModuleRequest(request);

        String intygJsonModel = getPopulatedModelFromIntygModule(intygType, draftRequest);

        Intyg persistedIntyg = persistNewDraft(request, intygJsonModel);

        LogRequest logRequest = createLogRequestFromDraft(persistedIntyg);
        logService.logCreateOfDraft(logRequest);

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

        return savedDraft;
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
                reqVardenhet.getPostnummer(), reqVardenhet.getPostort(), reqVardenhet.getTelefonnummer(), reqVardenhet.getEpost(), reqVardenhet.getArbetsplatskod(), vardgivare);

        HoSPerson reqHosPerson = request.getHosPerson();
        HoSPersonal hosPerson = new HoSPersonal(reqHosPerson.getHsaId(), reqHosPerson.getNamn(),
                reqHosPerson.getForskrivarkod(), reqHosPerson.getBefattning(), vardenhet);

        Patient reqPatient = request.getPatient();

        se.inera.certificate.modules.support.api.dto.Patient patient = new se.inera.certificate.modules.support.api.dto.Patient(reqPatient.getForNamn(),
                reqPatient.getEfterNamn(), reqPatient.getPersonNummer(), reqPatient.getPostAdress(), reqPatient.getPostNummer(), reqPatient.getPostOrt());

        return new CreateNewDraftHolder(request.getIntygId(), hosPerson, patient);
    }

    public Intyg getDraft(String intygId) {

        LOG.debug("Fetching Intyg '{}'", intygId);

        Intyg intyg = intygRepository.findOne(intygId);

        if (intyg == null) {
            LOG.warn("Intyg '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The intyg could not be found");
        }

        LogRequest logRequest = createLogRequestFromDraft(intyg);
        logService.logReadOfIntyg(logRequest);

        return intyg;
    }

    @Override
    public SigneringsBiljett biljettStatus(String biljettId) {
        SigneringsBiljett biljett = biljettTracker.getBiljett(biljettId);
        if (biljett != null && biljett.getId().equals(biljettId)) {
            return biljett;
        } else {
            return new SigneringsBiljett(biljettId, SigneringsBiljett.Status.OKAND, null, null, new LocalDateTime());
        }
    }

    @Override
    @Transactional
    public SigneringsBiljett skapaUtkastHash(String intygId) {

        LOG.debug("Underlag för klientsignering av utkast '{}'", intygId);

        Intyg intyg = getIntygForSignering(intygId);

        WebCertUser user = webCertUserService.getWebCertUser();
        String userId = user.getHsaId();

        intyg.getSenastSparadAv().setHsaId(userId);
        intyg.getSenastSparadAv().setNamn(user.getNamn());


        String payload = intyg.getModel();
        SigneringsBiljett statusBiljett = createSigneringsBiljett(intyg, payload);

        Intyg persisted = intygRepository.save(intyg);

        // TODO PDL log skapade signatursunderlag?
        return statusBiljett;
    }


    @Override
    @Transactional
    public SigneringsBiljett klientSigneraUtkast(String biljettId, String rawSignatur) {

        SigneringsBiljett biljett = biljettTracker.getBiljett(biljettId);

        LOG.debug("Klientsignering biljett '{}' intyg '{}'", biljett.getId(), biljett.getIntygsId());

        WebCertUser user = webCertUserService.getWebCertUser();
        String userId = user.getHsaId();

        try {
            String signature = objectMapper.readTree(rawSignatur).get("signature").textValue();
            if (!signatureService.validateSiths(userId, biljett.getHash(), signature)) {
                throw new RuntimeException("Kunde inte validera intyget");
            }
        } catch (IOException e) {
            // TODO Handle correctly
            throw new RuntimeException(e);
        }

        Intyg intyg = getIntygForSignering(biljett.getIntygsId());
        String payload = intyg.getModel();

        // TODO Verifiera att intyget inte ändrats

        intyg.setStatus(IntygsStatus.SIGNED);
        Intyg persisted = intygRepository.save(intyg);


        Signatur signatur = new Signatur(new LocalDateTime(), userId, biljett.getIntygsId(), payload, biljett.getHash(), rawSignatur);
        signaturRepository.save(signatur);

        biljett = biljettTracker.updateStatusBiljett(biljett.getId(), SigneringsBiljett.Status.SIGNERAD);

        // Skicka till intygstjansten
        intygService.storeIntyg(intyg);

        LogRequest logRequest = createLogRequestFromDraft(persisted);
        logService.logSigningOfDraft(logRequest);

        return biljett;
    }

    @Override
    @Transactional
    public SigneringsBiljett serverSigneraUtkast(String intygId) {

        LOG.debug("Signera utkast '{}'", intygId);

        Intyg intyg = getIntygForSignering(intygId);

        WebCertUser user = webCertUserService.getWebCertUser();
        String userId = user.getHsaId();

        intyg.getSenastSparadAv().setHsaId(userId);
        intyg.getSenastSparadAv().setNamn(user.getNamn());


        String payload = intyg.getModel();
        SigneringsBiljett statusBiljett = createSigneringsBiljett(intyg, payload);

        intyg.setStatus(IntygsStatus.SIGNED);
        Intyg persisted = intygRepository.save(intyg);
        Signatur signatur = new Signatur(new LocalDateTime(), userId, intygId, payload, statusBiljett.getHash(), "Signatur");
        signaturRepository.save(signatur);

        biljettTracker.updateStatusBiljett(statusBiljett.getId(), SigneringsBiljett.Status.SIGNERAD);

        // Skicka till intygstjansten
        intygService.storeIntyg(intyg);

        LogRequest logRequest = createLogRequestFromDraft(persisted);
        logService.logSigningOfDraft(logRequest);

        return statusBiljett;
    }

    private Intyg getIntygForSignering(String intygId) {
        Intyg intyg = intygRepository.findOne(intygId);

        if (intyg == null) {
            LOG.warn("Intyg '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The intyg could not be found");
        } else if (intyg.getStatus() != IntygsStatus.DRAFT_COMPLETE) {
            LOG.warn("Intyg '{}' with status '{}' can not be signed", intygId, intyg.getStatus());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "The intyg was not in state " + IntygsStatus.DRAFT_COMPLETE);
        }
        return intyg;
    }

    private SigneringsBiljett createSigneringsBiljett(Intyg intyg, String payload) {
        String hash = createHash(intyg, payload);

        SigneringsBiljett statusBiljett = new SigneringsBiljett(UUID.randomUUID().toString(), SigneringsBiljett.Status.BEARBETAR, intyg.getIntygsId(), hash, new LocalDateTime());
        biljettTracker.trackBiljett(statusBiljett);
        return statusBiljett;
    }

    private String createHash(Intyg intyg, String payload) { // TODO ta bort intygsparameter
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(payload.getBytes("UTF-8"));
            byte[] digest = sha.digest();
            return new String(Hex.encodeHex(digest));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LOG.error("Fel vid hashgenerering intyg {}. {}", intyg.getIntygsId(), e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM, "Internal error signing intyg");
        }
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

        IntygsStatus intygStatus = (draftValidation.isDraftValid()) ? IntygsStatus.DRAFT_COMPLETE
                : IntygsStatus.DRAFT_INCOMPLETE;

        intyg.setModel(draftAsJson);
        intyg.setStatus(intygStatus);

        VardpersonReferens vardPersonRef = createVardpersonFromHosPerson(request.getSavedBy());
        intyg.setSenastSparadAv(vardPersonRef);

        Intyg persistedDraft = intygRepository.save(intyg);

        LOG.debug("Draft '{}' updated", persistedDraft.getIntygsId());

        LogRequest logRequest = createLogRequestFromDraft(persistedDraft);
        logService.logUpdateOfDraft(logRequest);

        return draftValidation;
    }

    private LogRequest createLogRequestFromDraft(Intyg draft) {

        LogRequest logRequest = new LogRequest();

        logRequest.setIntygId(draft.getIntygsId());
        logRequest.setPatientId(draft.getPatientPersonnummer());
        logRequest.setPatientName(draft.getPatientFornamn(), draft.getPatientEfternamn());

        logRequest.setIntygCareUnitId(draft.getEnhetsId());
        logRequest.setIntygCareUnitName(draft.getEnhetsNamn());

        logRequest.setIntygCareGiverId(draft.getVardgivarId());
        logRequest.setIntygCareGiverName(draft.getVardgivarNamn());

        return logRequest;
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

        LogRequest logRequest = createLogRequestFromDraft(intyg);
        logService.logDeleteOfDraft(logRequest);
    }

    private boolean isTheDraftStillADraft(IntygsStatus intygStatus) {
        return ALL_DRAFT_STATUSES.contains(intygStatus);
    }
}
