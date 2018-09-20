/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.OptimisticLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.model.UtkastStatus;
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
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.StatisticsGroupByUtil;
import se.inera.intyg.webcert.web.service.util.UpdateUserUtil;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.UpdatePatientOnDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.util.CreateIntygsIdStrategy;

@Service
public class UtkastServiceImpl implements UtkastService {

    private static final List<UtkastStatus> ALL_EDITABLE_DRAFT_STATUSES = Arrays.asList(UtkastStatus.DRAFT_COMPLETE,
            UtkastStatus.DRAFT_INCOMPLETE);
    private static final List<UtkastStatus> ALL_DRAFT_STATUSES_INCLUDE_LOCKED = Arrays.asList(UtkastStatus.DRAFT_COMPLETE,
            UtkastStatus.DRAFT_INCOMPLETE, UtkastStatus.DRAFT_LOCKED);

    private static final Logger LOG = LoggerFactory.getLogger(UtkastServiceImpl.class);
    private static final String INTYG_INDICATOR = "intyg";
    private static final String UTKAST_INDICATOR = "utkast";

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
    private ReferensService referensService;

    public static boolean isUtkast(Utkast utkast) {
        return utkast != null && ALL_DRAFT_STATUSES_INCLUDE_LOCKED.contains(utkast.getStatus());
    }

    public static boolean isEditableUtkast(Utkast utkast) {
        return utkast != null && ALL_EDITABLE_DRAFT_STATUSES.contains(utkast.getStatus());
    }

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

        // Persist the referens if supplied
        if (!Strings.isNullOrEmpty(request.getReferens())) {
            referensService.saveReferens(request.getIntygId(), request.getReferens());
        }

        monitoringService.logUtkastCreated(savedUtkast.getIntygsId(),
                savedUtkast.getIntygsTyp(), savedUtkast.getEnhetsId(), savedUtkast.getSkapadAv().getHsaId());

        // Notify stakeholders when a draft has been created
        sendNotification(savedUtkast, Event.CREATED);

        // Create a PDL log for this action
        LogUser logUser = createLogUser(request);
        logCreateDraftPDL(savedUtkast, logUser);

        return savedUtkast;
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

        // check that the draft is still unsigned
        if (!isTheDraftStillADraft(utkast.getStatus())) {
            LOG.error("Intyg '{}' can not be set to klart for signera since it is no longer a draft", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "The draft can not be set to klart for signera since it is no longer a draft");
        }

        if (utkast.getKlartForSigneringDatum() == null) {
            notificationService.sendNotificationForDraftReadyToSign(utkast);
            utkast.setKlartForSigneringDatum(LocalDateTime.now());
            monitoringService.logUtkastMarkedAsReadyToSignNotificationSent(intygsId, intygType);
            saveDraft(utkast);
            LOG.debug("Sent, saved and logged utkast '{}' ready to sign", intygsId);
        }
    }

    @Override
    public Map<String, Map<String, PreviousIntyg>> checkIfPersonHasExistingIntyg(Personnummer personnummer, IntygUser user) {
        List<Utkast> toFilter = utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(),
                authoritiesHelper.getIntygstyperForFeature(user, AuthoritiesConstants.FEATURE_UNIKT_INTYG,
                        AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG));

        List<Utkast> signedList = toFilter.stream()
                .filter(utkast -> utkast.getStatus() == UtkastStatus.SIGNED)
                .filter(utkast -> utkast.getAterkalladDatum() == null)
                .sorted(Comparator.comparing(u -> u.getSignatur().getSigneringsDatum()))
                .collect(Collectors.toList());

        Map<String, Map<String, PreviousIntyg>> ret = new HashMap<>();

        ret.put(INTYG_INDICATOR, signedList.stream()
                .collect(Collectors.groupingBy(Utkast::getIntygsTyp,
                        Collectors.mapping(utkast ->
                                        new PreviousIntyg(
                                                Objects.equals(user.getValdVardgivare().getId(), utkast.getVardgivarId()),
                                                Objects.equals(user.getValdVardenhet().getId(), utkast.getEnhetsId()),
                                                utkast.getEnhetsNamn(),
                                                utkast.getIntygsId()
                                        ),
                                Collectors.reducing(new PreviousIntyg(), (a, b) -> b.isSameVardgivare() ? b : a)))));

        ret.put(UTKAST_INDICATOR, toFilter.stream()
                .filter(utkast -> utkast.getStatus() != UtkastStatus.SIGNED && utkast.getStatus() != UtkastStatus.DRAFT_LOCKED)
                .sorted(Comparator.comparing(Utkast::getSkapad, Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.groupingBy(Utkast::getIntygsTyp,
                        Collectors.mapping(utkast -> new PreviousIntyg(
                                        Objects.equals(user.getValdVardgivare().getId(), utkast.getVardgivarId()),
                                        Objects.equals(user.getValdVardenhet().getId(), utkast.getEnhetsId()),
                                        utkast.getEnhetsNamn(),
                                        utkast.getIntygsId()
                                ),
                                Collectors.reducing(new PreviousIntyg(), (a, b) -> b.isSameVardgivare() ? b : a)))));

        return ret;
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
        sendNotification(utkast, Event.DELETED);

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

        List<Object[]> result = utkastRepository.findDistinctLakareFromIntygEnhetAndStatuses(enhetsId, ALL_DRAFT_STATUSES_INCLUDE_LOCKED);

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

        List<GroupableItem> resultArr = utkastRepository.getIntygWithStatusesByEnhetsId(careUnitIds, ALL_DRAFT_STATUSES_INCLUDE_LOCKED,
                intygsTyper);
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
                sendNotification(utkast, Event.CHANGED);
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
        Patient draftPatient = getPatientFromCurrentDraft(moduleApi, utkast.getModel());

        Optional<Personnummer> optionalPnr = Optional.ofNullable(request.getPersonnummer());
        Optional<Personnummer> optionalDraftPnr = Optional.ofNullable(draftPatient.getPersonId());

        if (optionalDraftPnr.isPresent()) {
            // Spara undan det gamla personnummret temporärt
            String oldPersonId = optionalDraftPnr.get().getPersonnummer();
            webCertUserService.getUser().getParameters().setBeforeAlternateSsn(oldPersonId);
        }

        if ((optionalPnr.isPresent() || SamordningsnummerValidator.isSamordningsNummer(optionalPnr))
            && !isHashEqual(optionalPnr, optionalDraftPnr)) {


            // INTYG-4086: Ta reda på om man skall kunna uppdatera annat än personnumret? Och om man uppdaterar
            // personnumret -
            // vilka regler gäller då för namn och adress? Samma regler som i PatientDetailsResolverImpl?
            draftPatient.setPersonId(optionalPnr.get());

            try {
                String updatedModel = moduleApi.updateBeforeSave(utkast.getModel(), draftPatient);
                updateUtkastModel(utkast, updatedModel);
                saveDraft(utkast);
                monitoringService.logUtkastPatientDetailsUpdated(utkast.getIntygsId(), utkast.getIntygsTyp());
                sendNotification(utkast, Event.CHANGED);

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

        // check that the draft is still unsigned
        if (!isTheDraftStillADraft(utkast.getStatus())) {
            LOG.error("Intyg '{}' can not be set to notified since it is no longer a draft", intygsId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "The draft can not be set to notified since it is no longer a draft");
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

    @Override
    @Transactional
    public int lockOldDrafts(int lockedAfterDay, LocalDate today) {

        LocalDate lastCreatedDate = today.minusDays(lockedAfterDay);
        LocalDateTime lastCreatedDateTime = lastCreatedDate.atStartOfDay();

        List<Utkast> utkasts = utkastRepository.findDraftsByNotLockedOrSignedAndSkapadBefore(lastCreatedDateTime);

        utkasts.forEach(utkast -> {
            // Remove relations from other intyg
            utkastRepository.removeRelationsToDraft(utkast.getIntygsId());

            // Remove relations to other intyg
            utkast.setRelationKod(null);
            utkast.setRelationIntygsId(null);

            // Set status locked
            utkast.setStatus(UtkastStatus.DRAFT_LOCKED);
            utkastRepository.save(utkast);
        });

        return utkasts.size();
    }

    @Override
    public void revokeLockedDraft(String intygId, String intygTyp, String revokeMessage, String reason) {

        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "Could not find Utkast with id: " + intygId);
        }

        if (!utkast.getIntygsTyp().equals(intygTyp)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "IntygTyp did not match : " + intygTyp + " " + utkast.getIntygsTyp());
        }

        // Validate draft locked
        if (!UtkastStatus.DRAFT_LOCKED.equals(utkast.getStatus())) {
            LOG.info("User is not allowed to revoke intyg with status: {}", utkast.getStatus());
            final String message = "Revoke failed due to wrong utkast status";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }

        if (utkast.getAterkalladDatum() != null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Already revoked : " + utkast.getAterkalladDatum());
        }

        abortIfUserNotAuthorizedForUnit(utkast.getVardgivarId(), utkast.getEnhetsId());

        revokeUtkast(utkast, reason, revokeMessage);
    }

    /**
     * Send a notification message to stakeholders informing that
     * a question related to a revoked certificate has been closed.
     */
    private void revokeUtkast(Utkast utkast, String reason, String revokeMessage) {
        String intygsId = utkast.getIntygsId();

        String hsaId = webCertUserService.getUser().getHsaId();
        monitoringService.logUtkastRevoked(intygsId, hsaId, reason, revokeMessage);

        // First: mark the originating Utkast as REVOKED
        utkast.setAterkalladDatum(LocalDateTime.now());
        utkastRepository.save(utkast);

        // Third: create a log event
        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logRevokeIntyg(logRequest);
    }

    private void abortIfUserNotAuthorizedForUnit(String vardgivarHsaId, String enhetsHsaId) {
        if (!webCertUserService.isAuthorizedForUnit(vardgivarHsaId, enhetsHsaId, false)) {
            LOG.debug("User not authorized for enhet");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User not authorized for for enhet " + enhetsHsaId);
        }
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

        } catch (ModuleException | IOException e) {
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
                    validationWarning.getCategory(), validationWarning.getField(), validationWarning.getType(),
                    validationWarning.getMessage(), validationWarning.getDynamicKey()));
        }

        if (ValidationStatus.VALID.equals(validationStatus)) {
            LOG.debug("Validation is OK");
            return draftValidation;
        }

        draftValidation.setStatus(ValidationStatus.INVALID);

        // Only bother with returning validation (e.g. error) messages if the ArendeDraft is INVALID.
        for (ValidationMessage validationMsg : dr.getValidationErrors()) {
            draftValidation.addMessage(new DraftValidationMessage(
                    validationMsg.getCategory(), validationMsg.getField(), validationMsg.getType(),
                    validationMsg.getMessage(), validationMsg.getDynamicKey()));
        }

        LOG.debug("Validation failed with {} validation messages", draftValidation.getMessages().size());

        return draftValidation;
    }

    private CreateNewDraftHolder createModuleRequest(CreateNewDraftRequest request) {
        return new CreateNewDraftHolder(request.getIntygId(), request.getIntygTypeVersion(), request.getHosPerson(), request.getPatient());
    }

    private Utkast getIntygAsDraft(String intygsId, String intygType) {

        LOG.debug("Fetching utkast '{}'", intygsId);

        Utkast utkast = utkastRepository.findByIntygsIdAndIntygsTyp(intygsId, intygType);

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

    private boolean isHashEqual(Optional<Personnummer> thiz, Optional<Personnummer> that) {
        if (thiz.isPresent() && that.isPresent()) {
            return thiz.get().getPersonnummerHash().equals(that.get().getPersonnummerHash());
        }

        return false;
    }

    private boolean isTheDraftStillADraft(UtkastStatus utkastStatus) {
        return ALL_EDITABLE_DRAFT_STATUSES.contains(utkastStatus);
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
        utkast.setIntygTypeVersion(request.getIntygTypeVersion());

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
        utkast.setSkapad(LocalDateTime.now());

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
        }
    }

    private void updateUtkastModel(Utkast utkast, String modelJson) {
        WebCertUser user = webCertUserService.getUser();

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp());

            GrundData grundData = moduleApi.getUtlatandeFromJson(modelJson).getGrundData();

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

    public enum Event {
        CHANGED,
        CREATED,
        DELETED
    }
}
