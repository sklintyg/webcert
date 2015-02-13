package se.inera.webcert.service.utkast;

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

import se.inera.certificate.modules.registry.IntygModuleRegistry;
import se.inera.certificate.modules.registry.ModuleNotFoundException;
import se.inera.certificate.modules.support.api.ModuleApi;
import se.inera.certificate.modules.support.api.dto.CreateNewDraftHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelHolder;
import se.inera.certificate.modules.support.api.dto.InternalModelResponse;
import se.inera.certificate.modules.support.api.dto.ValidateDraftResponse;
import se.inera.certificate.modules.support.api.dto.ValidationMessage;
import se.inera.certificate.modules.support.api.dto.ValidationStatus;
import se.inera.certificate.modules.support.api.exception.ModuleException;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.webcert.service.draft.util.UpdateUserUtil;
import se.inera.webcert.service.dto.HoSPerson;
import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.dto.Patient;
import se.inera.webcert.service.dto.Vardenhet;
import se.inera.webcert.service.dto.Vardgivare;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.log.LogService;
import se.inera.webcert.service.notification.NotificationMessageFactory;
import se.inera.webcert.service.notification.NotificationService;
import se.inera.webcert.service.signatur.SignaturService;
import se.inera.webcert.service.signatur.dto.SignaturTicket;
import se.inera.webcert.service.utkast.dto.CreateNewDraftRequest;
import se.inera.webcert.service.utkast.dto.DraftValidation;
import se.inera.webcert.service.utkast.dto.DraftValidationStatus;
import se.inera.webcert.service.utkast.dto.SaveAndValidateDraftRequest;
import se.inera.webcert.service.utkast.util.CreateIntygsIdStrategy;
import se.inera.webcert.web.service.WebCertUserService;

@Service
public class UtkastServiceImpl implements UtkastService {

    public enum Event {
        CHANGED, CREATED, DELETED;
    }

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
    private SignaturService signatureService;

    @Autowired
    private LogService logService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private WebCertUserService webCertUserService;

    @Override
    @Transactional
    public String createNewDraft(CreateNewDraftRequest request) {

        populateRequestWithIntygId(request);
        request.setStatus(UtkastStatus.DRAFT_INCOMPLETE);

        String intygType = request.getIntygType();

        CreateNewDraftHolder draftRequest = createModuleRequest(request);

        String intygJsonModel = getPopulatedModelFromIntygModule(intygType, draftRequest);

        Utkast savedUtkast = persistNewDraft(request, intygJsonModel);

        LOG.debug("Utkast '{}' created and persisted", savedUtkast.getIntygsId());

        sendNotification(savedUtkast, Event.CREATED);

        return savedUtkast.getIntygsId();
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

    private Utkast persistNewDraft(CreateNewDraftRequest request, String draftAsJson) {

        Utkast utkast = new Utkast();

        se.inera.webcert.service.dto.Patient patient = request.getPatient();

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

    private String getPopulatedModelFromIntygModule(String intygType, CreateNewDraftHolder draftRequest) {

        LOG.debug("Calling module '{}' to get populated model", intygType);

        String modelAsJson;

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            InternalModelResponse draftResponse = moduleApi.createNewInternal(draftRequest);
            modelAsJson = draftResponse.getInternalModel();
        } catch (ModuleException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        } catch (ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
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
                reqVardenhet.getPostnummer(), reqVardenhet.getPostort(), reqVardenhet.getTelefonnummer(), reqVardenhet.getEpost(),
                reqVardenhet.getArbetsplatskod(), vardgivare);

        HoSPerson reqHosPerson = request.getHosPerson();
        se.inera.certificate.modules.support.api.dto.HoSPersonal hosPerson = new se.inera.certificate.modules.support.api.dto.HoSPersonal(
                reqHosPerson.getHsaId(),
                reqHosPerson.getNamn(), reqHosPerson.getForskrivarkod(), reqHosPerson.getBefattning(), reqHosPerson.getSpecialiseringar(), vardenhet);

        Patient reqPatient = request.getPatient();

        se.inera.certificate.modules.support.api.dto.Patient patient = new se.inera.certificate.modules.support.api.dto.Patient(
                reqPatient.getFornamn(),
                reqPatient.getMellannamn(), reqPatient.getEfternamn(), reqPatient.getPersonnummer(), reqPatient.getPostadress(),
                reqPatient.getPostnummer(), reqPatient.getPostort());

        return new CreateNewDraftHolder(request.getIntygId(), hosPerson, patient);
    }

    public Utkast getIntygAsDraft(String intygId) {

        LOG.debug("Fetching draft '{}'", intygId);

        Utkast intyg = utkastRepository.findOne(intygId);

        if (intyg == null) {
            LOG.warn("Draft '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The draft could not be found");
        }

        return intyg;
    }

    @Override
    public Utkast getDraft(String intygId) {
        return getIntygAsDraft(intygId);
    }

    @Override
    public SignaturTicket createDraftHash(String intygsId) {
        Utkast intyg = getIntygAsDraft(intygsId);
        updateWithUser(intyg);
        utkastRepository.save(intyg);

        return signatureService.createDraftHash(intygsId);
    }

    @Override
    public SignaturTicket serverSignature(String intygsId) {
        Utkast intyg = getIntygAsDraft(intygsId);
        updateWithUser(intyg);
        utkastRepository.save(intyg);

        return signatureService.serverSignature(intygsId);
    }

    @Override
    public CreateNewDraftCopyResponse createNewDraftCopy(CreateNewDraftCopyRequest copyRequest) {

        String orgIntygsId = copyRequest.getOriginalIntygId();
        String typ = copyRequest.getTyp();

        LOG.debug("Creating a new draft of type {} based on intyg '{}'", typ, orgIntygsId);

        try {

            IntygContentHolder template = intygService.fetchIntygData(orgIntygsId, typ);

            String intygType = template.getUtlatande().getTyp();
            String patientPersonnummer = template.getUtlatande().getGrundData().getPatient().getPersonId();

            if (copyRequest.containsNyttPatientPersonnummer()) {
                patientPersonnummer = copyRequest.getNyttPatientPersonnummer();
                LOG.debug("Request contained a new personnummer ({}) to use for the copy of '{}'", patientPersonnummer, orgIntygsId);
            }

            LOG.debug("Refreshing person data to use for the copy of '{}'", orgIntygsId);
            PersonSvar personSvar = personUppgiftsService.getPerson(patientPersonnummer);
            Person person = personSvar.getPerson();

            if (personSvar.getStatus() == PersonSvar.Status.NOT_FOUND) {
                LOG.error("No person data was found using '{}'", patientPersonnummer);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "No person data found using '"
                        + patientPersonnummer + "'");
            } else if (personSvar.getStatus() == PersonSvar.Status.ERROR) {
                LOG.error("External error while looking up person data '{}'", patientPersonnummer);

                // If there is a problem with the external system use old patient information.
                person = getPersonFromTemplate(template, patientPersonnummer);

            } else if (personSvar.getStatus() != PersonSvar.Status.FOUND) {
                LOG.error("Unknown status while looking up person data '{}'", patientPersonnummer);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
                        "Unknown status while looking up person data '"
                                + patientPersonnummer + "'");
            }

            String newDraftIntygId = intygsIdStrategy.createId();
            LOG.debug("Assigning the new draft copy id '{}'", newDraftIntygId);

            CreateNewDraftHolder moduleRequest = createModuleRequestForCopying(newDraftIntygId, copyRequest, person);

            String internalModelString = null;
            Utkast webcertIntyg = utkastRepository.findOne(orgIntygsId);

            if (webcertIntyg != null) {
                //Check for data in the webcert database first:
                internalModelString = webcertIntyg.getModel();
            } else if (webcertIntyg == null) {
                // Get data from Intygstjänsten since it is not present in webcert
                internalModelString = template.getContents();
            }

            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            InternalModelResponse draftResponse = moduleApi.createNewInternalFromTemplate(moduleRequest,
                    new InternalModelHolder(internalModelString));
            String newDraftModelAsJson = draftResponse.getInternalModel();

            LOG.debug("Got populated model of {} chars from module '{}'", getSafeLength(newDraftModelAsJson), intygType);

            DraftValidation draftValidation = validateDraft(newDraftIntygId, intygType, newDraftModelAsJson);
            UtkastStatus status = (draftValidation.isDraftValid()) ? UtkastStatus.DRAFT_COMPLETE : UtkastStatus.DRAFT_INCOMPLETE;

            CreateNewDraftRequest newDraftRequest = createNewDraftRequestForCopying(newDraftIntygId, intygType, status, copyRequest, person);
            Utkast savedDraft = persistNewDraft(newDraftRequest, newDraftModelAsJson);

            sendNotification(savedDraft, Event.CREATED);

            return new CreateNewDraftCopyResponse(intygType, savedDraft.getIntygsId());

        } catch (ModuleException me) {
            LOG.error("Module exception occured when trying to make a copy of " + orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        } catch (ModuleNotFoundException e) {
            LOG.error("Module exception occured when trying to make a copy of " + orgIntygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    private Person getPersonFromTemplate(IntygContentHolder template, String patientPersonnummer) {
        Person person;
        String fornamn = Strings.join(" ", template.getUtlatande().getGrundData().getPatient().getFornamn());
        String efternamn = template.getUtlatande().getGrundData().getPatient().getEfternamn();
        if (fornamn == null || fornamn.length() == 0) {
            // In this case use the last name from the template efternamn as efternamn and the rest as fornamn.
            String[] namn = efternamn.split(" ");
            if (namn.length > 0) {
                fornamn = Strings.join(" ", java.util.Arrays.copyOfRange(namn, 0, namn.length - 1));
                if (namn.length > 1) {
                    efternamn = namn[namn.length - 1];
                } else {
                    efternamn = "";
                }
            }
        }
        person = new Person(
            patientPersonnummer,
            fornamn,
            Strings.join(" ", template.getUtlatande().getGrundData().getPatient().getMellannamn()),
            efternamn,
            template.getUtlatande().getGrundData().getPatient().getPostadress(),
            template.getUtlatande().getGrundData().getPatient().getPostnummer(),
            template.getUtlatande().getGrundData().getPatient().getPostort());
        return person;
    }

    private CreateNewDraftHolder createModuleRequestForCopying(String newDraftIntygId, CreateNewDraftCopyRequest copyRequest, Person person) {

        Vardgivare reqVardgivare = copyRequest.getVardenhet().getVardgivare();
        se.inera.certificate.modules.support.api.dto.Vardgivare vardgivare = new se.inera.certificate.modules.support.api.dto.Vardgivare(
                reqVardgivare.getHsaId(), reqVardgivare.getNamn());

        Vardenhet reqVardenhet = copyRequest.getVardenhet();
        se.inera.certificate.modules.support.api.dto.Vardenhet vardenhet = new se.inera.certificate.modules.support.api.dto.Vardenhet(
                reqVardenhet.getHsaId(), reqVardenhet.getNamn(), reqVardenhet.getPostadress(),
                reqVardenhet.getPostnummer(), reqVardenhet.getPostort(), reqVardenhet.getTelefonnummer(), reqVardenhet.getEpost(),
                reqVardenhet.getArbetsplatskod(), vardgivare);

        HoSPerson reqHosPerson = copyRequest.getHosPerson();
        se.inera.certificate.modules.support.api.dto.HoSPersonal hosPerson = new se.inera.certificate.modules.support.api.dto.HoSPersonal(
                reqHosPerson.getHsaId(),
                reqHosPerson.getNamn(), reqHosPerson.getForskrivarkod(), reqHosPerson.getBefattning(), reqHosPerson.getSpecialiseringar(), vardenhet);

        se.inera.certificate.modules.support.api.dto.Patient patient = new se.inera.certificate.modules.support.api.dto.Patient(person.getFornamn(),
                person.getMellannamn(), person.getEfternamn(), person.getPersonnummer(), person.getPostadress(), person.getPostnummer(),
                person.getPostort());

        return new CreateNewDraftHolder(newDraftIntygId, hosPerson, patient);
    }

    private CreateNewDraftRequest createNewDraftRequestForCopying(String newDraftIntygId, String intygType, UtkastStatus status,
            CreateNewDraftCopyRequest request, Person person) {

        Patient patient = new Patient();
        patient.setPersonnummer(person.getPersonnummer());
        patient.setFornamn(person.getFornamn());
        patient.setMellannamn(person.getMellannamn());
        patient.setEfternamn(person.getEfternamn());
        patient.setPostadress(person.getPostadress());
        patient.setPostnummer(person.getPostnummer());
        patient.setPostort(person.getPostort());

        return new CreateNewDraftRequest(newDraftIntygId, intygType, status, request.getHosPerson(), request.getVardenhet(), patient);
    }

    @Override
    public DraftValidation saveAndValidateDraft(SaveAndValidateDraftRequest request) {

        String intygId = request.getIntygId();

        LOG.debug("Saving and validating utkast '{}'", intygId);

        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast == null) {
            LOG.warn("Utkast '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The utkast could not be found");
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

        return draftValidation;
    }


    @Transactional
    private Utkast saveDraft(Utkast utkast) {
        Utkast savedUtkast = utkastRepository.save(utkast);
        LOG.debug("Draft '{}' saved", savedUtkast.getIntygsId());
        return savedUtkast;
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

        } catch (ModuleException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        } catch (ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
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
            draftValidation.addMessage(new se.inera.webcert.service.utkast.dto.DraftValidationMessage(validationMsg.getField(), validationMsg
                    .getMessage()));
        }

        LOG.debug("Validation failed with {} validation messages", draftValidation.getMessages().size());

        return draftValidation;
    }

    @Override
    public List<Lakare> getLakareWithDraftsByEnhet(String enhetsId) {

        List<Lakare> lakareList = new ArrayList<>();

        List<Object[]> result = utkastRepository.findDistinctLakareFromIntygEnhetAndStatuses(enhetsId, ALL_DRAFT_STATUSES);

        for (Object[] lakareArr : result) {
            lakareList.add(new Lakare((String) lakareArr[0], (String) lakareArr[1]));
        }

        return lakareList;
    }

    @Override
    public Utkast setForwardOnDraft(String intygsId, Boolean forwarded) {

        Utkast utkast = utkastRepository.findOne(intygsId);

        if (utkast == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "Could not find Utkast with id: " + intygsId);
        }

        utkast.setVidarebefordrad(forwarded);

        return saveDraft(utkast);
    }

    @Override
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
    public void deleteUnsignedDraft(String intygId) {

        LOG.debug("Deleting draft with id '{}'", intygId);

        Utkast utkast = utkastRepository.findOne(intygId);

        // check that the draft exists
        if (utkast == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "The draft could not be deleted since it could not be found");
        }

        // check that the draft is still unsigned
        if (!isTheDraftStillADraft(utkast.getStatus())) {
            LOG.error("Intyg '{}' can not be deleted since it is no longer a draft", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "The draft can not be deleted since it is no longer a draft");
        }

        // Delete draft from repository
        deleteUnsignedDraft(utkast);

        // Notify stakeholders when a draft is deleted
        sendNotification(utkast, Event.DELETED);
    }

    @Transactional
    private void deleteUnsignedDraft(Utkast utkast) {
        utkastRepository.delete(utkast);
        LOG.debug("Deleteing draft '{}'", utkast.getIntygsId());
    }

    private void updateWithUser(Utkast utkast) {
        updateWithUser(utkast, utkast.getModel());
    }

    private void updateWithUser(Utkast utkast, String modelJson) {
        WebCertUser user = webCertUserService.getWebCertUser();
        se.inera.certificate.modules.support.api.dto.HoSPersonal hosPerson = UpdateUserUtil.createUserObject(user);
        utkast.setSenastSparadAv(UpdateUserUtil.createVardpersonFromWebCertUser(user));

        try {
            InternalModelHolder internalModel = new InternalModelHolder(modelJson);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp());
            InternalModelResponse updatedInternal = moduleApi.updateBeforeSave(internalModel, hosPerson);
            utkast.setModel(updatedInternal.getInternalModel());
        } catch (ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not update with HoS personal", e);
        } catch (ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not update with HoS personal", e);
        }
    }

    private boolean isTheDraftStillADraft(UtkastStatus utkastStatus) {
        return ALL_DRAFT_STATUSES.contains(utkastStatus);
    }

    private void sendNotification(Utkast utkast, Event event) {

        NotificationRequestType notificationRequestType = null;
        String logMsg = "";

        switch (event) {
        case CHANGED:
            notificationRequestType = NotificationMessageFactory.createNotificationFromChangedCertificateDraft(utkast);
            logMsg = "Notification sent: certificate draft with id '{}' was changed/updated.";
            break;
        case CREATED:
            notificationRequestType = NotificationMessageFactory.createNotificationFromCreatedDraft(utkast);
            logMsg = "Notification sent: certificate draft with id '{}' was created.";
            break;
        case DELETED:
            notificationRequestType = NotificationMessageFactory.createNotificationFromDeletedDraft(utkast);
            logMsg = "Notification sent: certificate draft with id '{}' was deleted.";
            break;
        default:
            logMsg = "IntygDraftServiceImpl.sendNotification(Intyg, Event) was called but with an unhandled event. No notification was sent.";
        }

        if (notificationRequestType != null) {
            notificationService.notify(notificationRequestType);
        }

        LOG.debug(logMsg, utkast.getIntygsId());
    }

}
