/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import jakarta.persistence.OptimisticLockException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.common.enumerations.KvIntygstyp;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.mapper.Mapper;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.CreateDraftCopyHolder;
import se.inera.intyg.common.support.modules.support.api.dto.CreateNewDraftHolder;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftResponse;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationMessage;
import se.inera.intyg.common.support.modules.support.api.dto.ValidationStatus;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.peristence.dao.util.DaoUtil;
import se.inera.intyg.common.support.validate.SamordningsnummerValidator;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.infra.security.common.service.CareUnitAccessHelper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.GroupableItem;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastFilter;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.ArendeConverter;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.integration.util.HoSPersonHelper;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.dto.Lakare;
import se.inera.intyg.webcert.web.service.employee.EmployeeNameService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.dto.LogUser;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
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
import se.inera.intyg.webcert.web.service.utkast.util.UtkastServiceHelper;

@Service
public class UtkastServiceImpl implements UtkastService {

    public enum Event {
        CHANGED,
        CREATED,
        DELETED,
        REVOKED
    }

    private static final Set<UtkastStatus> ALL_EDITABLE_DRAFT_STATUSES = UtkastStatus.getEditableDraftStatuses();
    private static final Set<UtkastStatus> ALL_DRAFT_STATUSES_INCLUDE_LOCKED = UtkastStatus.getDraftStatuses();
    private static final List<UtkastStatus> ALL_DRAFTS = Arrays.asList(
        UtkastStatus.DRAFT_COMPLETE,
        UtkastStatus.DRAFT_INCOMPLETE,
        UtkastStatus.DRAFT_LOCKED,
        UtkastStatus.SIGNED
    );

    private static final Logger LOG = LoggerFactory.getLogger(UtkastServiceImpl.class);
    public static final String INTYG_INDICATOR = "intyg";
    public static final String UTKAST_INDICATOR = "utkast";
    public static final String ERSATT_INDICATOR = "ersatt";

    @Autowired
    private CreateIntygsIdStrategy intygsIdStrategy;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private LogService logService;

    @Autowired
    private LogRequestFactory logRequestFactory;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CertificateEventService certificateEventService;

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

    @Autowired
    private DraftAccessServiceHelper draftAccessServiceHelper;

    @Autowired
    private EmployeeNameService employeeNameService;

    @Autowired
    private UtkastServiceHelper utkastServiceHelper;

    @Autowired
    private HashUtility hashUtility;

    @Autowired
    private CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @Autowired
    private PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;

    public static boolean isUtkast(Utkast utkast) {
        return utkast != null && ALL_DRAFT_STATUSES_INCLUDE_LOCKED.contains(utkast.getStatus());
    }

    public static boolean isEditableUtkast(Utkast utkast) {
        return utkast != null && ALL_EDITABLE_DRAFT_STATUSES.contains(utkast.getStatus());
    }

    @Override
    @Transactional() // , readOnly=true
    public int countFilterIntyg(UtkastFilter filter) {

        // Get intygstyper from write privilege
        Set<String> intygsTyper = authoritiesHelper.getIntygstyperForPrivilege(webCertUserService.getUser(),
            AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG);

        return utkastRepository.countFilterIntyg(filter, intygsTyper);
    }

    @Override
    public Utkast createNewDraft(CreateNewDraftRequest request) {

        populateRequestWithIntygId(request);

        String intygType = request.getIntygType();

        CreateNewDraftHolder draftRequest = createModuleRequest(request);

        String intygJsonModel = getPopulatedModelFromIntygModule(intygType, draftRequest);

        setUtkastStatus(intygJsonModel, request);

        Utkast savedUtkast = persistNewDraft(request, intygJsonModel);

        // Persist the referens if supplied
        if (!Strings.isNullOrEmpty(request.getReferens())) {
            referensService.saveReferens(request.getIntygId(), request.getReferens());
        }
        int nrPrefillElements = request.getForifyllnad().isPresent() ? request.getForifyllnad().get().getSvar().size() : 0;

        generateCertificateEvent(savedUtkast, EventCode.SKAPAT);

        if (UtkastStatus.DRAFT_COMPLETE == savedUtkast.getStatus()) {
            generateCertificateEvent(savedUtkast, EventCode.KFSIGN);
        }

        // If testability is set, return the draft without sending notifications or pdl log
        if (request.isTestability()) {
            monitoringService.logUtkastCreated(savedUtkast.getIntygsId(), savedUtkast.getIntygsTyp(),
                savedUtkast.getEnhetsId(), savedUtkast.getSkapadAv().getHsaId(), nrPrefillElements);
            return savedUtkast;
        }

        // Notify stakeholders when a draft has been created
        sendNotification(savedUtkast, Event.CREATED);

        // Create a PDL log for this action
        logCreateDraft(savedUtkast, createLogUser(request), nrPrefillElements);

        return savedUtkast;
    }

    private void setUtkastStatus(String intygJsonModel, CreateNewDraftRequest request) {
        if (request.getForifyllnad().isPresent()) {
            DraftValidation draftValidation = validateDraft(request.getIntygId(),
                request.getIntygType(), intygJsonModel != null ? intygJsonModel : "");
            request.setStatus(draftValidation.isDraftValid() ? UtkastStatus.DRAFT_COMPLETE : UtkastStatus.DRAFT_INCOMPLETE);
        } else {
            request.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        }
    }

    /**
     * @return {@link SaveDraftResponse}
     * @see UtkastService#updateDraftFromCandidate(String, String, String, String)
     */
    @Override
    public SaveDraftResponse updateDraftFromCandidate(String fromIntygId, String fromIntygType, String toUtkastId, String toUtkastType) {
        Utkast toUtkast = getIntygAsDraft(toUtkastId, toUtkastType);
        verifyUtkastExists(toUtkast, toUtkastId, toUtkastType,
            "The draft with id '" + toUtkastId + "' cannot be updated since it could not be found");

        return updateDraftFromCandidate(fromIntygId, fromIntygType, toUtkast);
    }

    /**
     * @return {@link SaveDraftResponse}
     * @see UtkastService#updateDraftFromCandidate(String, String, Utkast)
     */
    @Override
    public SaveDraftResponse updateDraftFromCandidate(String fromIntygId, String fromIntygType, Utkast toUtkast) {
        Utkast to = toUtkast;
        String toIntygsId = to.getIntygsId();
        String toIntygsType = to.getIntygsTyp();

        // Draft must be incomplete and only just created (no saving or updates).
        if (!UtkastStatus.DRAFT_INCOMPLETE.equals(to.getStatus()) && to.getVersion() != 0) {
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.INVALID_STATE,
                "The draft (utkast) you are trying to copy data to must have status DRAFT_INCOMPLETE and version 0");
        }

        try {
            Utlatande fromUtlatande = utkastServiceHelper.getUtlatandeForCandidateFromIT(fromIntygId, fromIntygType, true);

            String draftVersion = to.getIntygTypeVersion();
            if (draftVersion == null) {
                draftVersion = fromUtlatande.getTextVersion();
                if (draftVersion == null) {
                    throw new WebCertServiceException(
                        WebCertServiceErrorCodeEnum.MISSING_PARAMETER, "Expected type version to be set but value is null");
                }
            }

            // Get mapper and copy data to draft
            CreateDraftCopyHolder draftCopyHolder = new CreateDraftCopyHolder(toIntygsId, getHosPersonal(to));
            draftCopyHolder.setPatient(getPatientFromDraft(to));
            draftCopyHolder.setIntygTypeVersion(draftVersion);

            ModuleApi toModuleApi = getModuleApi(toIntygsType, draftVersion);
            Mapper moduleMapper = toModuleApi.getMapper().orElseThrow(() ->
                new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM,
                    String.format("Error copying data from intyg '%s' to utkast '%s'", fromIntygId, toIntygsId)));
            String draftAsJson = moduleMapper.map(fromUtlatande, draftCopyHolder).json();

            // Keep persisted json for comparsion
            String persistedJson = to.getModel();

            // Update draft
            updateUtkastModel(to, draftAsJson);

            // Save the updated draft
            to = saveDraft(to);
            LOG.debug("Utkast '{}' updated", toIntygsId);

            // Notify stakeholders when a draft has been changed/updated
            sendNotificationWhenDraftChanged(to, persistedJson);

            // Do the mandatory PDL logging
            logUpdateOfIntyg(to);

            // Flush JPA changes, to make sure the version attribute is updated
            utkastRepository.flush();

            return new SaveDraftResponse(to.getVersion(), to.getStatus());

        } catch (ModuleException | IOException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM,
                String.format("Error copying data from intyg '%s' to utkast '%s'", fromIntygId, toIntygsId), e);
        }

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
        verifyUtkastExists(utkast, intygsId, intygType, "The draft can not be set to klart for signera since it could not be found");

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
            publishCertificateAnalyticsMessage.publishEvent(
                certificateAnalyticsMessageFactory.draftReadyForSign(utkast)
            );
            saveDraft(utkast);

            generateCertificateEvent(utkast, EventCode.KFSIGN);

            LOG.debug("Sent, saved and logged utkast '{}' ready to sign", intygsId);
        }
    }

    @Override
    public Map<String, Map<String, PreviousIntyg>> checkIfPersonHasExistingIntyg(final Personnummer personnummer,
        final IntygUser user, final String currentDraftId) {

        List<Utkast> toFilter = utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(personnummer.getPersonnummerWithDash(),
            authoritiesHelper.getIntygstyperForFeature(user, AuthoritiesConstants.FEATURE_UNIKT_INTYG,
                AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG));

        List<Utkast> signedList = toFilter.stream()
            .filter(utkast -> utkast.getStatus() == UtkastStatus.SIGNED)
            .filter(utkast -> utkast.getAterkalladDatum() == null)
            .sorted(Comparator.comparing(u -> u.getSignatur().getSigneringsDatum()))
            .collect(Collectors.toList());

        List<Utkast> replacedList = toFilter.stream()
            .filter(utkast -> utkast.getRelationKod() == RelationKod.ERSATT && utkast.getIntygsId().equals(currentDraftId))
            .collect(Collectors.toList());

        Map<String, Map<String, PreviousIntyg>> ret = new HashMap<>();

        ret.put(ERSATT_INDICATOR, replacedList.stream()
            .collect(getUtkastMapCollector(user)));

        ret.put(INTYG_INDICATOR, signedList.stream()
            .collect(getUtkastMapCollector(user)));

        ret.put(UTKAST_INDICATOR, toFilter.stream()
            .filter(utkast -> utkast.getStatus() != UtkastStatus.SIGNED
                && utkast.getStatus() != UtkastStatus.DRAFT_LOCKED
                && !utkast.getIntygsId().equals(currentDraftId))
            .sorted(Comparator.comparing(Utkast::getSkapad, Comparator.nullsFirst(Comparator.naturalOrder())))
            .collect(getUtkastMapCollector(user)));

        return ret;
    }

    private Collector<Utkast, ?, Map<String, PreviousIntyg>> getUtkastMapCollector(IntygUser user) {
        return Collectors.groupingBy(Utkast::getIntygsTyp,
            Collectors.mapping(utkast -> PreviousIntyg.of(
                    Objects.equals(user.getValdVardgivare().getId(), utkast.getVardgivarId()),
                    Objects.equals(user.getValdVardenhet().getId(), utkast.getEnhetsId()),
                    enableShowDoiButton(user, utkast),
                    utkast.getEnhetsNamn(),
                    utkast.getIntygsId(),
                    utkast.getSkapad()),
                Collectors.reducing(new PreviousIntyg(), (a, b) -> b.isSameVardgivare() ? b : a)));
    }

    private boolean enableShowDoiButton(IntygUser user, Utkast utkast) {
        if (!utkast.getIntygsTyp().equalsIgnoreCase(KvIntygstyp.DOI.name())) {
            return false;
        }

        final var isSameCareUnit = CareUnitAccessHelper.userIsLoggedInOnEnhetOrUnderenhet(user, utkast.getEnhetsId());
        if (user.getOrigin().equals(UserOriginType.DJUPINTEGRATION.name())) {
            return isSameCareUnit;
        }

        if (user.getOrigin().equals(UserOriginType.NORMAL.name())) {
            final var isSameUnit = user.getValdVardenhet().getId().equals(utkast.getEnhetsId());
            final var unitOfUser = HoSPersonHelper.findVardenhetEllerMottagning(user, user.getValdVardenhet().getId());
            if (unitOfUser.isPresent()) {
                final var userOnCareUnit = unitOfUser.get() instanceof se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
                return userOnCareUnit ? isSameCareUnit : isSameUnit;
            }
        }

        return false;
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

        Utkast utkast = utkastRepository.findById(intygId).orElse(null);
        verifyUtkastExists(utkast, intygId, null, "The draft could not be deleted since it could not be found");

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
        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.draftDeleted(utkast)
        );

        // Notify stakeholders when a draft is deleted
        sendNotification(utkast, Event.DELETED);

        generateCertificateEvent(utkast, EventCode.RADERAT);

        LogRequest logRequest = logRequestFactory.createLogRequestFromUtkast(utkast);
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
    public String getCertificateType(String certificateId) {
        return utkastRepository.getIntygsTyp(certificateId);
    }

    @Override
    @Transactional(readOnly = true)
    public Utkast getDraft(String intygId, boolean pdlLog) {
        final String intygType = getCertificateType(intygId);
        return getDraft(intygId, intygType, pdlLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Utkast getDraft(String intygId, String intygType) {
        return getDraft(intygId, intygType, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Utkast getDraft(String intygId, String intygType, boolean createPdlLogEvent) {
        Utkast utkast = getIntygAsDraft(intygId, intygType);
        verifyUtkastExists(utkast, intygId, intygType, "Could not get draft since it could not be found");

        if (createPdlLogEvent) {
            // Log read to PDL
            LogRequest logRequest = logRequestFactory.createLogRequestFromUtkast(utkast, shouldPdlLogSjf(utkast));
            logService.logReadIntyg(logRequest);

            // Log read to monitoring log
            monitoringService.logUtkastRead(utkast.getIntygsId(), utkast.getIntygsTyp());
        }

        return utkast;
    }

    private boolean shouldPdlLogSjf(Utkast draft) {
        final var user = webCertUserService.getUser();
        return isSjf(user) && isDifferentCareProvider(draft, user);
    }

    private boolean isDifferentCareProvider(Utkast draft, WebCertUser user) {
        return !draft.getVardgivarId().equalsIgnoreCase(user.getValdVardgivare().getId());
    }

    private boolean isSjf(WebCertUser user) {
        return user != null && user.getParameters() != null && user.getParameters().isSjf();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lakare> getLakareWithDraftsByEnhet(String enhetsId) {

        List<Object[]> result = utkastRepository.findDistinctLakareFromIntygEnhetAndStatuses(enhetsId, ALL_DRAFT_STATUSES_INCLUDE_LOCKED);

        Set<String> hsaIDs = result.stream().map(arr -> (String) arr[0]).collect(Collectors.toSet());

        Map<String, String> hsaToNameMap = ArendeConverter.getNamesByHsaIds(hsaIDs, employeeNameService);

        return result.stream()
            .map(lakareArr -> {
                String hsaId = (String) lakareArr[0];
                String name = (String) lakareArr[1];

                if (hsaToNameMap.containsKey(hsaId)) {
                    name = hsaToNameMap.get(hsaId);
                }

                return new Lakare(hsaId, name);
            })
            .sorted(Comparator.comparing(Lakare::getName))
            .collect(Collectors.toList());
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
    @Transactional
    public SaveDraftResponse saveDraft(String intygId, long version, String draftAsJson, boolean createPdlLogEvent) {
        LOG.debug("Saving and validating utkast '{}'", intygId);

        Utkast utkast = utkastRepository.findById(intygId).orElse(null);
        verifyUtkastExists(utkast, intygId, null, "The draft could not be saved since it could not be found");

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

        // Notify stakeholders when a draft has been changed/updated
        sendNotificationWhenDraftChanged(utkast, persistedJson);

        if (createPdlLogEvent) {
            logUpdateOfIntyg(utkast);
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

        Utkast utkast = utkastRepository.findById(draftId).orElse(null);
        verifyUtkastExists(utkast, draftId, null,
            "The draft could not be updated with patient details since it could not be found");

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

        final ModuleApi moduleApi = getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion());

        // INTYG-4086
        Patient draftPatient = getPatientFromCDraft(moduleApi, utkast.getModel());

        Optional<Personnummer> optionalPnr = Optional.ofNullable(request.getPersonnummer());
        Optional<Personnummer> optionalDraftPnr = Optional.ofNullable(draftPatient.getPersonId());

        if (optionalDraftPnr.isPresent()) {
            // Spara undan det gamla personnummret temporärt
            String oldPersonId;
            if (request.getOldPersonnummer() != null) {
                oldPersonId = request.getOldPersonnummer().getPersonnummer();
            } else {
                oldPersonId = optionalDraftPnr.get().getPersonnummer();
            }
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
        Utkast utkast = utkastRepository.findById(intygsId).orElse(null);

        verifyUtkastExists(utkast, intygsId, null, "The draft could not be set to notified since it could not be found");
        draftAccessServiceHelper.validateAllowToForwardDraft(utkast);

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
        LOG.debug("Validating draft with id '{}' and type '{}'", intygId, intygType);

        try {
            ModuleApi moduleApi = getModuleApi(intygType, moduleRegistry.resolveVersionFromUtlatandeJson(intygType, draftAsJson));
            return convertToDraftValidation(moduleApi.validateDraft(draftAsJson));
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

            certificateEventService
                .createCertificateEvent(utkast.getIntygsId(), "UtkastLockJob", EventCode.LAST, "Draft locked after 14 days");

            monitoringService.logUtkastLocked(utkast.getIntygsId(), utkast.getIntygsTyp());
        });

        return utkasts.size();
    }

    @Override
    public void revokeLockedDraft(String intygId, String intygTyp, String revokeMessage, String reason) {

        Utkast utkast = utkastRepository.findById(intygId).orElse(null);
        verifyUtkastExists(utkast, intygId, intygTyp, "The locked draft could not be revoked since it could not be found");

        if (!utkast.getIntygsTyp().equals(intygTyp)) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                "IntygTyp did not match : " + intygTyp + " " + utkast.getIntygsTyp());
        }

        // Validate draft locked
        if (!UtkastStatus.DRAFT_LOCKED.equals(utkast.getStatus())) {
            LOG.info("User cannot revoke a draft with status: {}", utkast.getStatus());
            final String message = "Revoke failed due to wrong utkast status";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }

        if (utkast.getAterkalladDatum() != null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                "Already revoked : " + utkast.getAterkalladDatum());
        }

        revokeUtkast(utkast, reason, revokeMessage);
    }

    @Override
    public boolean isDraftCreatedFromReplacement(String certificateId) {
        return utkastRepository.findParentRelation(certificateId).stream()
            .anyMatch(relation -> relation.getRelationKod() == RelationKod.ERSATT);
    }

    @Override
    public List<Utkast> findUtkastByPatientAndUnits(Personnummer patientId, List<String> unitIds) {
        final var user = webCertUserService.getUser();
        if (new AuthoritiesValidator().given(user).features(AuthoritiesConstants.FEATURE_HANTERA_INTYGSUTKAST).isVerified()) {
            Set<String> intygstyper = authoritiesHelper
                .getIntygstyperForPrivilege(user, AuthoritiesConstants.PRIVILEGE_VISA_INTYG);
            final var drafts = utkastRepository.findDraftsByPatientAndEnhetAndStatus(
                DaoUtil.formatPnrForPersistence(patientId),
                unitIds,
                ALL_DRAFTS,
                intygstyper);

            LOG.debug("Got #{} utkast", drafts.size());
            return drafts;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Revoke draft and notify stakeholders that this draft is now deleted.
     */
    private void revokeUtkast(Utkast utkast, String reason, String revokeMessage) {
        final var intygsId = utkast.getIntygsId();
        final var hsaId = webCertUserService.getUser().getHsaId();
        monitoringService.logUtkastRevoked(intygsId, hsaId, reason, revokeMessage);

        // First: mark the originating Utkast as REVOKED
        utkast.setAterkalladDatum(LocalDateTime.now());
        utkastRepository.save(utkast);

        // Secondly: notify stakeholders that draft is revoked
        sendNotification(utkast, Event.REVOKED);
        generateCertificateEvent(utkast, EventCode.MAKULERAT);
        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.lockedDraftRevoked(utkast)
        );

        // Third: create a log event
        LogRequest logRequest = logRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logRevokeIntyg(logRequest);
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

    private ModuleApi getModuleApi(String intygsTyp, String intygsTypeVersion) {
        try {
            return moduleRegistry.getModuleApi(intygsTyp, intygsTypeVersion);

        } catch (ModuleNotFoundException e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                // This error message is helpful when debugging save problems.
                LOG.debug(e.getCause().getCause().getMessage());
            }
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, "Could not get module for type " + intygsTyp, e);
        }
    }

    private Patient getPatientFromDraft(Utkast utkast) {
        final ModuleApi moduleApi = getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion());
        return getPatientFromCDraft(moduleApi, utkast.getModel());
    }

    private Patient getPatientFromCDraft(ModuleApi moduleApi, String draftModel) {
        try {
            return moduleApi.getUtlatandeFromJson(draftModel).getGrundData().getPatient();
        } catch (ModuleException | IOException e) {
            if (e.getCause() != null && e.getCause().getCause() != null) {
                // This error message is helpful when debugging save problems.
                LOG.debug(e.getCause().getCause().getMessage());
            }
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM,
                "Could not get Patient object from draft", e);
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
                validationMsg.getMessage(), validationMsg.getDynamicKey(), validationMsg.getQuestionId()));
        }

        LOG.debug("Validation failed with {} validation messages", draftValidation.getMessages().size());

        return draftValidation;
    }

    private CreateNewDraftHolder createModuleRequest(CreateNewDraftRequest request) {
        return new CreateNewDraftHolder(request.getIntygId(), request.getIntygTypeVersion(), request.getHosPerson(), request.getPatient(),
            request.getForifyllnad(), request.getPatient().isTestIndicator());
    }

    private Utkast getIntygAsDraft(String intygsId, String intygsTyp) {
        LOG.debug("Trying to fetch draft '{}' from repository", intygsId);
        return utkastRepository.findByIntygsIdAndIntygsTyp(intygsId, intygsTyp);
    }

    private void verifyUtkastExists(Utkast utkast, String intygsId, String intygsTyp, String errMsg) {
        if (utkast == null) {
            StringBuilder sb = new StringBuilder("Utkast");

            if (!Strings.isNullOrEmpty(intygsId)) {
                sb.append(String.format(" with id '%s'", intygsId));
                if (!Strings.isNullOrEmpty(intygsTyp)) {
                    sb.append(String.format(" and of type %s", intygsTyp));
                }
            }
            sb.append(" could not be found");

            LOG.warn(sb.toString());

            if (!Strings.isNullOrEmpty(errMsg)) {
                sb.replace(0, sb.length(), errMsg);
            }
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, sb.toString());
        }
    }

    private String getPopulatedModelFromIntygModule(String intygType, CreateNewDraftHolder draftRequest) {

        LOG.debug("Calling module '{}' to get populated model", intygType);

        String modelAsJson;

        try {
            ModuleApi moduleApi = moduleRegistry.getModuleApi(intygType, draftRequest.getIntygTypeVersion());
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
            return hashUtility.hash(thiz.get().getPersonnummer()).equals(hashUtility.hash(that.get().getPersonnummer()));
        }

        return false;
    }

    private boolean isTheDraftStillADraft(UtkastStatus utkastStatus) {
        return ALL_EDITABLE_DRAFT_STATUSES.contains(utkastStatus);
    }

    private void logCreateDraft(Utkast utkast, LogUser logUser, int nrPrefillElements) {
        LogRequest logRequest = logRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logCreateIntyg(logRequest, logUser);

        monitoringService.logUtkastCreated(utkast.getIntygsId(), utkast.getIntygsTyp(),
            utkast.getEnhetsId(), utkast.getSkapadAv().getHsaId(), nrPrefillElements);
    }

    private void logUpdateOfIntyg(Utkast utkast) {
        LogRequest logRequest = logRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logUpdateIntyg(logRequest);

        // Monitor that changes have been made to draft
        monitoringService.logUtkastEdited(utkast.getIntygsId(), utkast.getIntygsTyp());
    }

    private void sendNotificationWhenDraftChanged(Utkast to, String persistedJson) {
        try {
            ModuleApi moduleApi = getModuleApi(to.getIntygsTyp(), to.getIntygTypeVersion());
            if (moduleApi.shouldNotify(persistedJson, to.getModel())) {
                LOG.debug("*** Detected changes in model, sending notification! ***");
                sendNotification(to, Event.CHANGED);
            }
        } catch (ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, e);
        }
    }

    private Utkast persistNewDraft(CreateNewDraftRequest request, String draftAsJson) {
        Utkast utkast = new Utkast();

        Patient patient = request.getPatient();
        utkast.setPatientPersonnummer(patient.getPersonId());
        utkast.setPatientFornamn(patient.getFornamn());
        utkast.setPatientMellannamn(patient.getMellannamn());
        utkast.setPatientEfternamn(patient.getEfternamn());
        utkast.setTestIntyg(patient.isTestIndicator());

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
            case REVOKED:
                notificationService.sendNotificationForDraftRevoked(utkast);
                break;
        }
    }

    private void generateCertificateEvent(Utkast certificate, EventCode eventCode) {
        String user = certificate.getSkapadAv().getHsaId();
        certificateEventService.createCertificateEvent(
            certificate.getIntygsId(), user, eventCode);
    }

    private HoSPersonal getHosPersonal(Utkast utkast) throws IOException, ModuleException {
        ModuleApi moduleApi = getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion());

        GrundData grundData = moduleApi.getUtlatandeFromJson(utkast.getModel()).getGrundData();
        Vardenhet vardenhet = grundData.getSkapadAv().getVardenhet();

        return IntygConverterUtil.buildHosPersonalFromWebCertUser(webCertUserService.getUser(), vardenhet);
    }

    private Vardenhet getVardenhet(Utkast utkast) {
        final Vardgivare vardgivare = new Vardgivare();
        vardgivare.setVardgivarid(utkast.getVardgivarId());

        final Vardenhet vardenhet = new Vardenhet();
        vardenhet.setEnhetsid(utkast.getEnhetsId());
        vardenhet.setVardgivare(vardgivare);

        return vardenhet;
    }

    private void updateUtkastModel(Utkast utkast, String modelJson) {
        WebCertUser user = webCertUserService.getUser();

        try {
            ModuleApi moduleApi = getModuleApi(utkast.getIntygsTyp(), utkast.getIntygTypeVersion());

            GrundData grundData = moduleApi.getUtlatandeFromJson(modelJson).getGrundData();

            Vardenhet vardenhetFromJson = grundData.getSkapadAv().getVardenhet();
            HoSPersonal hosPerson = IntygConverterUtil.buildHosPersonalFromWebCertUser(user, vardenhetFromJson);
            utkast.setSenastSparadAv(UpdateUserUtil.createVardpersonFromWebCertUser(user));
            utkast.setPatientPersonnummer(grundData.getPatient().getPersonId());
            String updatedInternal = moduleApi.updateBeforeSave(modelJson, hosPerson);
            utkast.setModel(updatedInternal);

            updatePatientNameFromModel(utkast, grundData.getPatient());

        } catch (ModuleException | IOException e) {
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
}
