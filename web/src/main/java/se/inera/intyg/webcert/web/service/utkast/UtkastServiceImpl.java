/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.utkast;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateNewDraftHolder;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessage;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertFeature;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.StatisticsGroupByUtil;
import se.inera.intyg.webcert.web.service.util.UpdateUserUtil;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.UpdatePatientOnDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

import javax.persistence.OptimisticLockException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private IntygTextsService intygTextsService;

    @Autowired
    private AuthoritiesHelper authoritiesHelper;

    @Autowired
    private StatisticsGroupByUtil statisticsGroupByUtil;

    @Autowired
    private WebcertFeatureService featureService;

    @Override
    @Transactional("jpaTransactionManager") // , readOnly=true
    public int countFilterIntyg(UtkastFilter filter) {

        // Get intygstyper from write privilege
        Set<String> intygsTyper = authoritiesHelper.getIntygstyperForPrivilege(webCertUserService.getUser(),
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        return utkastRepository.countFilterIntyg(filter, intygsTyper);
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
        sendNotification(savedUtkast, Event.CREATED, getUserReference());

        // Create a PDL log for this action
        LogUser logUser = createLogUser(request);
        logCreateDraftPDL(savedUtkast, logUser);

        return savedUtkast;
    }

    private LogUser createLogUser(CreateNewDraftRequest request) {
        HoSPersonal hosPerson = request.getHosPerson();

        String personId = hosPerson.getPersonId();
        String vardenhetId = hosPerson.getVardenhet().getEnhetsid();
        String vardgivareId = hosPerson.getVardenhet().getVardgivare().getVardgivarid();

        return new LogUser.Builder(personId, vardenhetId, vardgivareId)
                .userName(hosPerson.getFullstandigtNamn())
                .userTitle(hosPerson.getTitel())
                .userAssignment(hosPerson.getMedarbetarUppdrag())
                .enhetsNamn(hosPerson.getVardenhet().getEnhetsnamn())
                .vardgivareNamn(hosPerson.getVardenhet().getVardgivare().getVardgivarnamn())
                .build();
    }

    @Override
    public String getQuestions(String intygsTyp, String version) {
        String questionsAsJson = intygTextsService.getIntygTexts(intygsTyp, version);

        LOG.debug("Got questions of {} chars from module '{}'", getSafeLength(questionsAsJson), intygsTyp);

        return questionsAsJson;
    }

    @Override
    @Transactional
    public void setKlarForSigneraAndSendStatusMessage(String intygsId, String intygType) {

        validateUserAllowedToSendKFSignNotification(intygsId, intygType);

        Utkast utkast = getIntygAsDraft(intygsId, intygType);
        if (utkast.getKlartForSigneringDatum() == null) {
            notificationService.sendNotificationForDraftReadyToSign(utkast, getUserReference());
            utkast.setKlartForSigneringDatum(LocalDateTime.now());
            monitoringService.logUtkastMarkedAsReadyToSignNotificationSent(intygsId, intygType);
            saveDraft(utkast);
            LOG.debug("Sent, saved and logged utkast '{}' ready to sign", intygsId);
        }
    }

    @Override
    public Map<String, Boolean> checkIfPersonHasExistingIntyg(Personnummer personnummer, IntygUser user) {
        return utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummer(),
                authoritiesHelper.getIntygstyperForModuleFeature(user, WebcertFeature.UNIKT_INTYG, WebcertFeature.UNIKT_INTYG_INOM_VG))
                .stream()
                .filter(utkast -> utkast.getStatus() == UtkastStatus.SIGNED)
                .filter(utkast -> utkast.getAterkalladDatum() == null)
                .collect(Collectors.groupingBy(Utkast::getIntygsTyp,
                        Collectors.mapping(utkast -> Objects.equals(user.getValdVardgivare().getId(), utkast.getVardgivarId()),
                                Collectors.reducing(false, (a, b) -> a || b))));
    }

    private void validateUserAllowedToSendKFSignNotification(String intygsId, String intygType) {
        Set<String> intygsTyper = authoritiesHelper.getIntygstyperForPrivilege(webCertUserService.getUser(),
                AuthoritiesConstants.PRIVILEGE_NOTIFIERING_UTKAST);

        if (intygsTyper.size() == 0 || !intygsTyper.contains(intygType)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not allowed to send KFSIGN notification for utkast '" + intygsId + "', "
                            + "user must be Vardadministrator or intygsTyp '" + intygType + "' is not eligible "
                            + "for KFSIGN notifications.");
        }
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
        sendNotification(utkast, Event.DELETED, getUserReference());

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logDeleteIntyg(logRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Utkast> filterIntyg(UtkastFilter filter) {

        // Get intygstyper from write privilege
        Set<String> intygsTyper = authoritiesHelper.getIntygstyperForPrivilege(webCertUserService.getUser(),
                AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        // If intygstyper is an empty set, user are not granted access to view intyg of any intygstyp.
        if (intygsTyper.isEmpty()) {
            return Collections.emptyList();
        }

        // Get a list of drafts
        return utkastRepository.filterIntyg(filter, intygsTyper);
    }

    @Override
    @Transactional(readOnly = true)
    public Utkast getDraft(String intygId, String intygType) {
        Utkast utkast = getIntygAsDraft(intygId, intygType);
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        abortIfUserNotAuthorizedForUnit(utkast.getVardgivarId(), utkast.getEnhetsId());

        // Log read to PDL
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

        WebCertUser user = webCertUserService.getUser();

        // Get intygstyper from write privilege
        Set<String> intygsTyper = authoritiesHelper.getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        List<GroupableItem> resultArr = utkastRepository.getIntygWithStatusesByEnhetsId(careUnitIds, ALL_DRAFT_STATUSES, intygsTyper);
        return statisticsGroupByUtil.toSekretessFilteredMap(resultArr);
    }

    @Override
    @Transactional("jpaTransactionManager")
    public SaveDraftResponse saveDraft(String intygId, long version, String draftAsJson, boolean createPdlLogEvent) {
        LOG.debug("Saving and validating utkast '{}'", intygId);

        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast == null) {
            LOG.warn("Utkast '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The utkast could not be found");
        }

        // check that the draft hasn't been modified concurrently
        if (utkast.getVersion() != version) {
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

        // Keep persisted json for comparsion
        String persistedJson = utkast.getModel();

        // Update draft with user information
        updateUtkastModel(utkast, draftAsJson);

        // Is draft valid?
        DraftValidation draftValidation = validateDraft(intygId, intygType, draftAsJson);

        UtkastStatus utkastStatus = draftValidation.isDraftValid() ? UtkastStatus.DRAFT_COMPLETE : UtkastStatus.DRAFT_INCOMPLETE;
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
            if (moduleApi.shouldNotify(persistedJson, draftAsJson)) {
                LOG.debug("*** Detected changes in model, sending notification! ***");
                sendNotification(utkast, Event.CHANGED, getUserReference());
            }
        } catch (ModuleException | ModuleNotFoundException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }

        // Flush JPA changes, to make sure the version attribute is updated
        utkastRepository.flush();

        return new SaveDraftResponse(utkast.getVersion(), utkastStatus);
    }

    @Override
    public void updatePatientOnDraft(UpdatePatientOnDraftRequest request) {
        // diff draftPatient and request patient: if no changes, do nothing
        String draftId = request.getDraftId();

        LOG.debug("Checking that Patient is up-to-date on Utkast '{}'", draftId);

        Utkast utkast = utkastRepository.findOne(draftId);

        if (utkast == null) {
            LOG.warn("Utkast '{}' was not found", draftId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "The utkast could not be found");
        }

        if (webCertUserService.getUser().getIdsOfAllVardenheter().stream()
                .noneMatch(enhet -> enhet.equalsIgnoreCase(utkast.getEnhetsId()))) {
            LOG.error("User did not have any medarbetaruppdrag for enhet '{}'", utkast.getEnhetsId());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User did not have any medarbetaruppdrag for enhet " + utkast.getEnhetsId());
        }

        // check that the draft hasn't been modified concurrently
        if (utkast.getVersion() != request.getVersion()) {
            LOG.debug("Utkast '{}' was concurrently modified", draftId);
            throw new OptimisticLockException(utkast.getSenastSparadAv().getNamn());
        }

        // check that the draft is still a draft
        if (!isTheDraftStillADraft(utkast.getStatus())) {
            LOG.error("Utkast '{}' can not be updated since it is no longer in draft mode", draftId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "This utkast can not be updated since it is no longer in draft mode");
        }

        final ModuleApi moduleApi = getModuleApi(utkast.getIntygsTyp());

        // INTYG-4086
        Personnummer personId = request.getPersonnummer();
        Patient draftPatient = getPatientFromCurrentDraft(moduleApi, utkast.getModel());
        if (personId != null
                && (Personnummer.createValidatedPersonnummerWithDash(personId).isPresent()
                || SamordningsnummerValidator.isSamordningsNummer(personId))
                && !personId.getPnrHash().equals(draftPatient.getPersonId().getPnrHash())) {

            String oldPersonId = draftPatient.getPersonId().getPersonnummer();

            // INTYG-4086: Ta reda på om man skall kunna uppdatera annat än personnumret? Och om man uppdaterar
            // personnumret -
            // vilka regler gäller då för namn och adress? Samma regler som i PatientDetailsResolverImpl?
            draftPatient.setPersonId(personId);
            try {
                String updatedModel = moduleApi.updateBeforeSave(utkast.getModel(), draftPatient);
                updateUtkastModel(utkast, updatedModel);
                saveDraft(utkast);
                monitoringService.logUtkastPatientDetailsUpdated(utkast.getIntygsId(), utkast.getIntygsTyp());
                sendNotification(utkast, Event.CHANGED, getUserReference());

                // Spara undan det gamla personnummret temporärt
                webCertUserService.getUser().getParameters().setBeforeAlternateSsn(oldPersonId);
            } catch (ModuleException e) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM,
                        "Patient details on Utkast " + draftId + " could not be updated", e);
            }
        } else {
            LOG.debug("Utkast '{}' patient details were already up-to-date: no update needed", draftId);
        }
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
        LOG.debug("Validating Intyg '{}' with type '{}'", intygId, intygType);

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            ValidateDraftResponse validateDraftResponse = moduleApi.validateDraft(draftAsJson);

            return convertToDraftValidation(validateDraftResponse);
        } catch (ModuleException | ModuleNotFoundException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    private void abortIfUserNotAuthorizedForUnit(String vardgivarHsaId, String enhetsHsaId) {
        if (!webCertUserService.isAuthorizedForUnit(vardgivarHsaId, enhetsHsaId, false)) {
            LOG.debug("User not authorized for enhet");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsHsaId);
        }
    }

    private ModuleApi getModuleApi(String intygsTyp) {
        try {
            return moduleRegistry.getModuleApi(intygsTyp);

        } catch (ModuleNotFoundException e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                // This error message is helpful when debugging save problems.
                LOG.debug(e.getCause().getCause().getMessage());
            }
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not get module for type " + intygsTyp, e);
        }
    }

    private Patient getPatientFromCurrentDraft(ModuleApi moduleApi, String draftModel) {
        try {
            return moduleApi.getUtlatandeFromJson(draftModel).getGrundData().getPatient();

        } catch (IOException e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                // This error message is helpful when debugging save problems.
                LOG.debug(e.getCause().getCause().getMessage());
            }
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not get Patient from draft", e);
        }
    }

    private DraftValidation convertToDraftValidation(ValidateDraftResponse dr) {

        DraftValidation draftValidation = new DraftValidation();
        ValidationStatus validationStatus = dr.getStatus();

        // Always return the warning messages
        for (ValidationMessage validationWarning : dr.getValidationWarnings()) {
            draftValidation.addWarning(new DraftValidationMessage(
                    validationWarning.getField(), validationWarning.getType(), validationWarning.getMessage(),
                    validationWarning.getDynamicKey()));
        }

        if (ValidationStatus.VALID.equals(validationStatus)) {
            LOG.debug("Validation is OK");
            return draftValidation;
        }

        draftValidation.setStatus(ValidationStatus.INVALID);

        // Only bother with returning validation (e.g. error) messages if the ArendeDraft is INVALID.
        for (ValidationMessage validationMsg : dr.getValidationErrors()) {
            draftValidation.addMessage(new DraftValidationMessage(
                    validationMsg.getField(), validationMsg.getType(), validationMsg.getMessage(), validationMsg.getDynamicKey()));
        }

        LOG.debug("Validation failed with {} validation messages", draftValidation.getMessages().size());

        return draftValidation;
    }

    private CreateNewDraftHolder createModuleRequest(CreateNewDraftRequest request) {
        return new CreateNewDraftHolder(request.getIntygId(), request.getHosPerson(), request.getPatient());
    }

    private Utkast getIntygAsDraft(String intygsId, String intygType) {

        LOG.debug("Fetching utkast '{}'", intygsId);

        Utkast utkast = utkastRepository.findOneByIntygsIdAndIntygsTyp(intygsId, intygType);

        if (utkast == null) {
            LOG.warn("Utkast '{}' of type {} was not found", intygsId, intygType);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Utkast could not be found");
        }

        return utkast;
    }

    private String getPopulatedModelFromIntygModule(String intygType, CreateNewDraftHolder draftRequest) {

        LOG.debug("Calling module '{}' to get populated model", intygType);

        String modelAsJson;

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType);
            modelAsJson = moduleApi.createNewInternal(draftRequest);
        } catch (ModuleException | ModuleNotFoundException me) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }

        LOG.debug("Got populated model of {} chars from module '{}'", getSafeLength(modelAsJson), intygType);

        return modelAsJson;
    }

    private int getSafeLength(String str) {
        return Strings.nullToEmpty(str).trim().length();
    }

    private boolean isTheDraftStillADraft(UtkastStatus utkastStatus) {
        return ALL_DRAFT_STATUSES.contains(utkastStatus);
    }

    private void logCreateDraftPDL(Utkast utkast, LogUser logUser) {

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logCreateIntyg(logRequest, logUser);
    }

    private Utkast persistNewDraft(CreateNewDraftRequest request, String draftAsJson) {

        Utkast utkast = new Utkast();

        Patient patient = request.getPatient();

        utkast.setPatientPersonnummer(patient.getPersonId());
        utkast.setPatientFornamn(patient.getFornamn());
        utkast.setPatientMellannamn(patient.getMellannamn());
        utkast.setPatientEfternamn(patient.getEfternamn());

        utkast.setIntygsId(request.getIntygId());
        utkast.setIntygsTyp(request.getIntygType());

        utkast.setStatus(request.getStatus());

        utkast.setModel(draftAsJson);

        Vardenhet vardenhet = request.getHosPerson().getVardenhet();

        utkast.setEnhetsId(vardenhet.getEnhetsid());
        utkast.setEnhetsNamn(vardenhet.getEnhetsnamn());

        Vardgivare vardgivare = vardenhet.getVardgivare();

        utkast.setVardgivarId(vardgivare.getVardgivarid());
        utkast.setVardgivarNamn(vardgivare.getVardgivarnamn());

        VardpersonReferens creator = UpdateUserUtil.createVardpersonFromHosPerson(request.getHosPerson());

        utkast.setSenastSparadAv(creator);
        utkast.setSkapadAv(creator);

        return saveDraft(utkast);
    }

    private void populateRequestWithIntygId(CreateNewDraftRequest request) {

        if (!Strings.nullToEmpty(request.getIntygId()).trim().isEmpty()) {
            LOG.debug("Detected that the CreateNewDraftRequest already contains an intygId!");
            return;
        }

        String generatedIntygId = intygsIdStrategy.createId();
        request.setIntygId(generatedIntygId);

        LOG.debug("Created id '{}' for the new draft", generatedIntygId);
    }

    private Utkast saveDraft(Utkast utkast) {
        Utkast savedUtkast = utkastRepository.save(utkast);
        LOG.debug("ArendeDraft '{}' saved", savedUtkast.getIntygsId());
        return savedUtkast;
    }

    private void sendNotification(Utkast utkast, Event event, String reference) {

        switch (event) {
        case CHANGED:
            notificationService.sendNotificationForDraftChanged(utkast, reference);
            break;
        case CREATED:
            notificationService.sendNotificationForDraftCreated(utkast, reference);
            break;
        case DELETED:
            notificationService.sendNotificationForDraftDeleted(utkast, reference);
            break;
        }
    }

    private void updateUtkastModel(Utkast utkast, String modelJson) {
        WebCertUser user = webCertUserService.getUser();

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp());

            GrundData grundData = moduleApi.getUtlatandeFromJson(modelJson).getGrundData();

            // INTYG-4086: Make sure we never save unwanted patient information to the backend.
            // Patient updatedPatientForSaving = patientDetailsResolver.updatePatientForSaving(grundData.getPatient(),
            // utkast.getIntygsTyp());

            Vardenhet vardenhetFromJson = grundData.getSkapadAv().getVardenhet();
            HoSPersonal hosPerson = IntygConverterUtil.buildHosPersonalFromWebCertUser(user, vardenhetFromJson);
            utkast.setSenastSparadAv(UpdateUserUtil.createVardpersonFromWebCertUser(user));
            utkast.setPatientPersonnummer(grundData.getPatient().getPersonId());
            String updatedInternal = moduleApi.updateBeforeSave(modelJson, hosPerson);
            // String updatedInternalWithResolvedPatient = moduleApi.updateBeforeSave(updatedInternal,
            // updatedPatientForSaving);
            utkast.setModel(updatedInternal);

            updatePatientNameFromModel(utkast, grundData.getPatient());

        } catch (ModuleException | ModuleNotFoundException | IOException e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                // This error message is helpful when debugging save problems.
                LOG.debug(e.getCause().getCause().getMessage());
            }
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not update with HoS personal", e);
        }
    }

    /**
     * See INTYG-3077 - when autosaving we make sure that the columns for fornamn, mellannamn and efternamn match
     * whatever values that are present in the actual utkast model.
     * <p>
     * In the rare occurance that a patient has a name change after the initial utkast was created - e.g. the utkast
     * was continued on at a subsequent date - this method makes sure that the three "metadata" 'name' columns in the
     * INTYG table reflects the actual model.
     * <p>
     * The one exception is when the utkast is of type fk7263 and copied from Intygstjänsten (or have an ancestor which
     * is created as a copy of an intyg in Intygstjänsten). In this case the JSON will not have a fornamn and we cannot
     * save null in UTKAST.PATIENT_FORNAMN.
     */
    private void updatePatientNameFromModel(Utkast utkast, Patient patient) {
        if (patient == null || patient.getFornamn() == null) {
            return;
        }
        if (utkast.getPatientFornamn() != null && !utkast.getPatientFornamn().equals(patient.getFornamn())) {
            utkast.setPatientFornamn(patient.getFornamn());
        }
        if (utkast.getPatientMellannamn() != null && !utkast.getPatientMellannamn().equals(patient.getMellannamn())) {
            utkast.setPatientMellannamn(patient.getMellannamn());
        }
        if (utkast.getPatientEfternamn() != null && !utkast.getPatientEfternamn().equals(patient.getEfternamn())) {
            utkast.setPatientEfternamn(patient.getEfternamn());
        }
    }

    private String getUserReference() {
        if (!webCertUserService.hasAuthenticationContext()) {
            return null;
        }

        WebCertUser user = webCertUserService.getUser();
        return user != null && user.getParameters() != null ? user.getParameters().getReference() : null;
    }

    public enum Event {
        CHANGED,
        CREATED,
        DELETED
    }
}
