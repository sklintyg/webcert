package se.inera.intyg.webcert.web.service.utkast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.OptimisticLockException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateNewDraftHolder;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelHolder;
import se.inera.intyg.common.support.modules.support.api.dto.InternalModelResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessage;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.dto.HoSPerson;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.dto.Patient;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;
import se.inera.intyg.webcert.web.service.dto.Vardgivare;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.UpdateUserUtil;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationStatus;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveAndValidateDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveAndValidateDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

@Service
public class UtkastServiceImpl implements UtkastService {

    private static final List<UtkastStatus> ALL_DRAFT_STATUSES = Arrays.asList(UtkastStatus.DRAFT_COMPLETE,
            UtkastStatus.DRAFT_INCOMPLETE);

    private static final Logger LOG = LoggerFactory.getLogger(UtkastServiceImpl.class);

    @Autowired
    private CreateIntygsIdStrategy intygsIdStrategy;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private LogService logService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MonitoringLogService monitoringService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Override
    @Transactional("jpaTransactionManager") // , readOnly=true
    public int countFilterIntyg(UtkastFilter filter) {
        return utkastRepository.countFilterIntyg(filter);
    }

    @Override
    public Utkast createNewDraft(CreateNewDraftRequest request) {

        populateRequestWithIntygId(request);
        request.setStatus(UtkastStatus.DRAFT_INCOMPLETE);

        String intygType = request.getIntygType();

        CreateNewDraftHolder draftRequest = createModuleRequest(request);

        String intygJsonModel = getPopulatedModelFromIntygModule(intygType, draftRequest);

        Utkast savedUtkast = persistNewDraft(request, intygJsonModel);

        monitoringService.logUtkastCreated(savedUtkast.getIntygsId(),
                savedUtkast.getIntygsTyp(), savedUtkast.getEnhetsId(), savedUtkast.getSkapadAv().getHsaId());

        // Notify stakeholders when a draft has been created
        sendNotification(savedUtkast, Event.CREATED);

        // Create a PDL log for this action
        logCreateDraftPDL(savedUtkast, request.getVardenhet().getVardgivare().getHsaId(),
                request.getVardenhet().getVardgivare().getNamn(), request.getVardenhet().getHsaId(),
                request.getVardenhet().getNamn(), request.getHosPerson().getHsaId(),
                request.getHosPerson().getNamn());

        return savedUtkast;
    }

    @Override
    @Transactional
    public void deleteUnsignedDraft(String intygId, long version) {

        LOG.debug("Deleting utkast '{}'", intygId);

        Utkast utkast = utkastRepository.findOne(intygId);

        // check that the draft exists
        if (utkast == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "The draft could not be deleted since it could not be found");
        }

        // check that the draft hasn't been modified concurrently
        if (utkast.getVersion() != version) {
            LOG.debug("Utkast '{}' was concurrently modified", intygId);
            throw new OptimisticLockException(utkast.getSenastSparadAv().getNamn());
        }

        // check that the draft is still unsigned
        if (!isTheDraftStillADraft(utkast.getStatus())) {
            LOG.error("Intyg '{}' can not be deleted since it is no longer a draft", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "The draft can not be deleted since it is no longer a draft");
        }

        // Delete draft from repository
        utkastRepository.delete(utkast);
        LOG.debug("Deleted draft '{}'", utkast.getIntygsId());

        // Audit log
        monitoringService.logUtkastDeleted(utkast.getIntygsId(), utkast.getIntygsTyp());

        // Notify stakeholders when a draft is deleted
        sendNotification(utkast, Event.DELETED);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logDeleteIntyg(logRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Utkast> filterIntyg(UtkastFilter filter) {
        List<Utkast> utkastList = utkastRepository.filterIntyg(filter);
        Iterator<Utkast> i = utkastList.iterator();
        Set<String> intygsTyper = webCertUserService.getUser().getIntygsTyper();
        while (i.hasNext()) {
            Utkast utkast = i.next();
            if (!intygsTyper.contains(utkast.getIntygsTyp())) {
                i.remove();
            }
        }
        return utkastList;
    }

    @Override
    @Transactional(readOnly = true)
    public Utkast getDraft(String intygId) {
        Utkast utkast = getIntygAsDraft(intygId);
        abortIfUserNotAuthorizedForUnit(utkast.getVardgivarId(), utkast.getEnhetsId());

        // Log read to PDL
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logReadIntyg(logRequest);

        // Log read to monitoring log
        monitoringService.logUtkastRead(utkast.getIntygsId(), utkast.getIntygsTyp());

        return utkast;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lakare> getLakareWithDraftsByEnhet(String enhetsId) {

        List<Lakare> lakareList = new ArrayList<>();

        List<Object[]> result = utkastRepository.findDistinctLakareFromIntygEnhetAndStatuses(enhetsId, ALL_DRAFT_STATUSES);

        for (Object[] lakareArr : result) {
            lakareList.add(new Lakare((String) lakareArr[0], (String) lakareArr[1]));
        }

        return lakareList;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getNbrOfUnsignedDraftsByCareUnits(List<String> careUnitIds) {

        Map<String, Long> resultsMap = new HashMap<>();

        if (careUnitIds == null || careUnitIds.isEmpty()) {
            LOG.warn("No ids for Vardenheter was supplied");
            return resultsMap;
        }

        List<Object[]> countResults = utkastRepository.countIntygWithStatusesGroupedByEnhetsId(careUnitIds, ALL_DRAFT_STATUSES);

        for (Object[] resultArr : countResults) {
            resultsMap.put((String) resultArr[0], (Long) resultArr[1]);
        }

        return resultsMap;
    }

    @Override
    @Transactional(value = "jpaTransactionManager", readOnly = true)
    public void logPrintOfDraftToPDL(String intygId) {
        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast == null) {
            return;
        }

        // Log print to PDL log
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logPrintIntygAsDraft(logRequest);

        // Log print to monitoring log
        monitoringService.logUtkastPrint(utkast.getIntygsId(), utkast.getIntygsTyp());
    }

    @Override
    @Transactional("jpaTransactionManager")
    public SaveAndValidateDraftResponse saveAndValidateDraft(SaveAndValidateDraftRequest request, boolean createPdlLogEvent) {

        String intygId = request.getIntygId();

        LOG.debug("Saving and validating utkast '{}'", intygId);

        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast == null) {
            LOG.warn("Utkast '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The utkast could not be found");
        }

        // check that the draft hasn't been modified concurrently
        if (utkast.getVersion() != request.getVersion()) {
            LOG.debug("Utkast '{}' was concurrently modified", intygId);
            throw new OptimisticLockException(utkast.getSenastSparadAv().getNamn());
        }

        // check that the draft is still a draft
        if (!isTheDraftStillADraft(utkast.getStatus())) {
            LOG.error("Utkast '{}' can not be updated since it is no longer in draft mode", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "This utkast can not be updated since it is no longer in draft mode");
        }

        String intygType = utkast.getIntygsTyp();
        String draftAsJson = request.getDraftAsJson();

        // Keep persisted json for comparsion
        String persistedJson = utkast.getModel();

        // Update draft with user information
        updateWithUser(utkast, draftAsJson);

        // Is draft valid?
        DraftValidation draftValidation = validateDraft(intygId, intygType, draftAsJson);

        UtkastStatus utkastStatus = (draftValidation.isDraftValid()) ? UtkastStatus.DRAFT_COMPLETE : UtkastStatus.DRAFT_INCOMPLETE;
        utkast.setStatus(utkastStatus);

        // Save the updated draft
        utkast = saveDraft(utkast);
        LOG.debug("Utkast '{}' updated", utkast.getIntygsId());

        if (createPdlLogEvent) {
            LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
            logService.logUpdateIntyg(logRequest);

            monitoringService.logUtkastEdited(utkast.getIntygsId(), utkast.getIntygsTyp());
        }

        // Notify stakeholders when a draft has been changed/updated
        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            if (moduleApi.isModelChanged(persistedJson, draftAsJson)) {
                LOG.debug("*** Detected changes in model, sending notification! ***");
                sendNotification(utkast, Event.CHANGED);
            }
        } catch (ModuleException | ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }

        // Flush JPA changes, to make sure the version attribute is updated
        utkastRepository.flush();

        return new SaveAndValidateDraftResponse(utkast.getVersion(), draftValidation);
    }

    @Override
    @Transactional
    public Utkast setNotifiedOnDraft(String intygsId, long version, Boolean notified) {

        Utkast utkast = utkastRepository.findOne(intygsId);

        if (utkast == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "Could not find Utkast with id: " + intygsId);
        }

        // check that the draft hasn't been modified concurrently
        if (utkast.getVersion() != version) {
            LOG.debug("Utkast '{}' was concurrently modified", intygsId);
            throw new OptimisticLockException(utkast.getSenastSparadAv().getNamn());
        }

        utkast.setVidarebefordrad(notified);

        return saveDraft(utkast);
    }

    @Override
    public DraftValidation validateDraft(String intygId, String intygType, String draftAsJson) {

        DraftValidation draftValidation;

        LOG.debug("Validating Intyg '{}' with type '{}'", intygId, intygType);

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            InternalModelHolder intHolder = new InternalModelHolder(draftAsJson);
            ValidateDraftResponse validateDraftResponse = moduleApi.validateDraft(intHolder);

            draftValidation = convertToDraftValidation(validateDraftResponse);

        } catch (ModuleException | ModuleNotFoundException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }

        return draftValidation;
    }

    protected void abortIfUserNotAuthorizedForUnit(String vardgivarHsaId, String enhetsHsaId) {
        if (!webCertUserService.isAuthorizedForUnit(vardgivarHsaId, enhetsHsaId, false)) {
            LOG.debug("User not authorized for enhet");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsHsaId);
        }
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
            draftValidation.addMessage(new se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage(
                    validationMsg.getField(), validationMsg.getType(), validationMsg.getMessage()));
        }

        LOG.debug("Validation failed with {} validation messages", draftValidation.getMessages().size());

        return draftValidation;
    }

    private CreateNewDraftHolder createModuleRequest(CreateNewDraftRequest request) {

        Vardgivare reqVardgivare = request.getVardenhet().getVardgivare();
        se.inera.intyg.common.support.modules.support.api.dto.Vardgivare vardgivare = new se.inera.intyg.common.support.modules.support.api.dto.Vardgivare(
                reqVardgivare.getHsaId(), reqVardgivare.getNamn());

        Vardenhet reqVardenhet = request.getVardenhet();
        se.inera.intyg.common.support.modules.support.api.dto.Vardenhet vardenhet = new se.inera.intyg.common.support.modules.support.api.dto.Vardenhet(
                reqVardenhet.getHsaId(), reqVardenhet.getNamn(), reqVardenhet.getPostadress(),
                reqVardenhet.getPostnummer(), reqVardenhet.getPostort(), reqVardenhet.getTelefonnummer(), reqVardenhet.getEpost(),
                reqVardenhet.getArbetsplatskod(), vardgivare);

        HoSPerson reqHosPerson = request.getHosPerson();
        se.inera.intyg.common.support.modules.support.api.dto.HoSPersonal hosPerson = new se.inera.intyg.common.support.modules.support.api.dto.HoSPersonal(
                reqHosPerson.getHsaId(),
                reqHosPerson.getNamn(), reqHosPerson.getForskrivarkod(), reqHosPerson.getBefattning(), reqHosPerson.getSpecialiseringar(), vardenhet);

        Patient reqPatient = request.getPatient();

        se.inera.intyg.common.support.modules.support.api.dto.Patient patient = new se.inera.intyg.common.support.modules.support.api.dto.Patient(
                reqPatient.getFornamn(),
                reqPatient.getMellannamn(), reqPatient.getEfternamn(), reqPatient.getPersonnummer(), reqPatient.getPostadress(),
                reqPatient.getPostnummer(), reqPatient.getPostort());

        return new CreateNewDraftHolder(request.getIntygId(), hosPerson, patient);
    }

    private Utkast getIntygAsDraft(String intygsId) {

        LOG.debug("Fetching utkast '{}'", intygsId);

        Utkast utkast = utkastRepository.findOne(intygsId);

        if (utkast == null) {
            LOG.warn("Utkast '{}' was not found", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Utkast could not be found");
        }

        return utkast;
    }

    private String getPopulatedModelFromIntygModule(String intygType, CreateNewDraftHolder draftRequest) {

        LOG.debug("Calling module '{}' to get populated model", intygType);

        String modelAsJson;

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            InternalModelResponse draftResponse = moduleApi.createNewInternal(draftRequest);
            modelAsJson = draftResponse.getInternalModel();
        } catch (ModuleException | ModuleNotFoundException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }

        LOG.debug("Got populated model of {} chars from module '{}'", getSafeLength(modelAsJson), intygType);

        return modelAsJson;
    }

    private int getSafeLength(String str) {
        return (StringUtils.isNotBlank(str)) ? str.length() : 0;
    }

    private boolean isTheDraftStillADraft(UtkastStatus utkastStatus) {
        return ALL_DRAFT_STATUSES.contains(utkastStatus);
    }

    private void logCreateDraftPDL(Utkast utkast, String hsaIdVardgivare, String namnVardgivare,
                                   String hsaIdVardenhet, String namnVardenhet, String hsaIdHosPerson,
                                   String namnHosPerson) {

        LogUser logUser = new LogUser();
        logUser.setVardgivareId(hsaIdVardgivare);
        logUser.setVardgivareNamn(namnVardgivare);
        logUser.setEnhetsId(hsaIdVardenhet);
        logUser.setEnhetsNamn(namnVardenhet);
        logUser.setUserId(hsaIdHosPerson);
        logUser.setUserName(namnHosPerson);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logCreateIntyg(logRequest, logUser);
    }

    private Utkast persistNewDraft(CreateNewDraftRequest request, String draftAsJson) {

        Utkast utkast = new Utkast();

        Patient patient = request.getPatient();

        utkast.setPatientPersonnummer(patient.getPersonnummer());
        utkast.setPatientFornamn(patient.getFornamn());
        utkast.setPatientMellannamn(patient.getMellannamn());
        utkast.setPatientEfternamn(patient.getEfternamn());

        utkast.setIntygsId(request.getIntygId());
        utkast.setIntygsTyp(request.getIntygType());

        utkast.setStatus(request.getStatus());

        utkast.setModel(draftAsJson);

        Vardenhet vardenhet = request.getVardenhet();

        utkast.setEnhetsId(vardenhet.getHsaId());
        utkast.setEnhetsNamn(vardenhet.getNamn());

        Vardgivare vardgivare = vardenhet.getVardgivare();

        utkast.setVardgivarId(vardgivare.getHsaId());
        utkast.setVardgivarNamn(vardgivare.getNamn());

        VardpersonReferens creator = UpdateUserUtil.createVardpersonFromHosPerson(request.getHosPerson());

        utkast.setSenastSparadAv(creator);
        utkast.setSkapadAv(creator);

        return saveDraft(utkast);
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

    private Utkast saveDraft(Utkast utkast) {
        Utkast savedUtkast = utkastRepository.save(utkast);
        LOG.debug("Draft '{}' saved", savedUtkast.getIntygsId());
        return savedUtkast;
    }

    private void sendNotification(Utkast utkast, Event event) {

        switch (event) {
        case CHANGED:
            notificationService.sendNotificationForDraftChanged(utkast);
            break;
        case CREATED:
            notificationService.sendNotificationForDraftCreated(utkast);
            break;
        case DELETED:
            notificationService.sendNotificationForDraftDeleted(utkast);
            break;
        default:
            LOG.debug("IntygDraftServiceImpl.sendNotification(Intyg, Event) was called but with an unhandled event. No notification was sent.",
                    utkast.getIntygsId());
        }
    }

    private void updateWithUser(Utkast utkast, String modelJson) {
        WebCertUser user = webCertUserService.getUser();
        se.inera.intyg.common.support.modules.support.api.dto.HoSPersonal hosPerson = UpdateUserUtil.createUserObject(user);
        utkast.setSenastSparadAv(UpdateUserUtil.createVardpersonFromWebCertUser(user));

        try {
            InternalModelHolder internalModel = new InternalModelHolder(modelJson);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp());
            InternalModelResponse updatedInternal = moduleApi.updateBeforeSave(internalModel, hosPerson);
            utkast.setModel(updatedInternal.getInternalModel());
        } catch (ModuleException | ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not update with HoS personal", e);
        }
    }

    public enum Event {
        CHANGED, CREATED, DELETED;
    }
}
