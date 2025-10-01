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
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.common.enumerations.EventCode;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.analytics.service.CertificateAnalyticsMessageFactory;
import se.inera.intyg.webcert.integration.analytics.service.PublishCertificateAnalyticsMessage;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.event.CertificateEventService;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactory;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.AbstractCreateCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.util.UtkastServiceHelper;

@Service
public class CopyUtkastServiceImpl implements CopyUtkastService {

    public static final String CREATE_REPLACEMENT = "create replacement";
    public static final String ORIGINAL_CERTIFICATE_IS_REVOKED = "Original certificate is revoked";
    public static final String CREATE_RENEWAL = "create renewal";
    private static final Logger LOG = LoggerFactory.getLogger(CopyUtkastServiceImpl.class);
    @Autowired
    private IntygService intygService;

    @Autowired
    private CertificateRelationService certificateRelationService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private PUService puService;

    @Autowired
    @Qualifier("copyCompletionUtkastBuilder")
    private CopyUtkastBuilder<CreateCompletionCopyRequest> copyCompletionUtkastBuilder;

    @Autowired
    @Qualifier("createRenewalUtkastBuilder")
    private CopyUtkastBuilder<CreateRenewalCopyRequest> createRenewalUtkastBuilder;

    @Autowired
    @Qualifier("createReplacementUtkastBuilder")
    private CopyUtkastBuilder<CreateReplacementCopyRequest> createReplacementUtkastBuilder;

    @Autowired
    @Qualifier("createUtkastFromTemplateBuilder")
    private CopyUtkastBuilder<CreateUtkastFromTemplateRequest> createUtkastFromTemplateBuilder;

    @Autowired
    @Qualifier("createUtkastCopyBuilder")
    private CopyUtkastBuilder<CreateUtkastFromTemplateRequest> createUtkastCopyBuilder;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CertificateEventService certificateEventService;

    @Autowired
    private LogService logService;

    @Autowired
    private LogRequestFactory logRequestFactory;

    @Autowired
    private MonitoringLogService monitoringService;

    @Autowired
    private WebCertUserService userService;

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private ReferensService referensService;

    @Autowired
    private UtkastServiceHelper utkastServiceHelper;

    @Autowired
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    @Autowired
    private DraftAccessServiceHelper draftAccessServiceHelper;

    @Autowired
    private LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private HashUtility hashUtility;

    @Autowired
    private CertificateAnalyticsMessageFactory certificateAnalyticsMessageFactory;

    @Autowired
    private PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;

    @Override
    public CreateCompletionCopyResponse createCompletion(CreateCompletionCopyRequest copyRequest) {
        String originalIntygId = copyRequest.getOriginalIntygId();

        LOG.debug("Creating completion to intyg '{}'", originalIntygId);
        WebCertUser user = userService.getUser();

        try {
            final Utlatande utlatande = utkastServiceHelper.getUtlatande(copyRequest.getOriginalIntygId(),
                copyRequest.getOriginalIntygTyp(),
                false);

            certificateAccessServiceHelper.validateAccessToAnswerComplementQuestion(utlatande, true);

            addTestIntygFlagIfNecessaryToCopyRequest(copyRequest, utlatande.getGrundData().isTestIntyg());

            if (intygService.isRevoked(copyRequest.getOriginalIntygId(), copyRequest.getTyp())) {
                LOG.debug("Cannot create completion copy of certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, ORIGINAL_CERTIFICATE_IS_REVOKED);
            }
            UtkastBuilderResponse builderResponse = buildCompletionUtkastBuilderResponse(copyRequest, originalIntygId, true);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            Utkast savedUtkast = saveAndNotify(builderResponse, user, EventCode.KOMPLETTERAR, originalIntygId);

            monitoringService.logIntygCopiedCompletion(savedUtkast.getIntygsId(), originalIntygId);

            return new CreateCompletionCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygTypeVersion(),
                savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error(getErrorMessageWhenCopyingFails(originalIntygId));
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    @Override
    public CreateRenewalCopyResponse createRenewalCopy(CreateRenewalCopyRequest copyRequest) {
        String originalIntygId = copyRequest.getOriginalIntygId();

        LOG.debug("Creating renewal for intyg '{}'", originalIntygId);

        try {
            final Utlatande utlatande = utkastServiceHelper.getUtlatande(copyRequest.getOriginalIntygId(),
                copyRequest.getOriginalIntygTyp(),
                false);

            certificateAccessServiceHelper.validateAccessToRenew(utlatande);

            addTestIntygFlagIfNecessaryToCopyRequest(copyRequest, utlatande.getGrundData().isTestIntyg());

            if (intygService.isRevoked(copyRequest.getOriginalIntygId(), copyRequest.getOriginalIntygTyp())) {
                LOG.debug("Cannot renew certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, ORIGINAL_CERTIFICATE_IS_REVOKED);
            }

            verifyNotReplacedWithSigned(copyRequest.getOriginalIntygId(), CREATE_RENEWAL);
            verifyNotComplementedWithSigned(copyRequest.getOriginalIntygId(), CREATE_RENEWAL);
            verifySigned(utlatande, CREATE_RENEWAL);

            UtkastBuilderResponse builderResponse = buildRenewalUtkastBuilderResponse(copyRequest, originalIntygId);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            final var user = userService.getUser();
            Utkast savedUtkast = saveAndNotify(builderResponse, user, EventCode.FORLANGER, originalIntygId);

            monitoringService.logIntygCopiedRenewal(savedUtkast.getIntygsId(), originalIntygId);

            final var analyticsMessage = certificateAnalyticsMessageFactory.renew(savedUtkast);
            publishCertificateAnalyticsMessage.publishEvent(analyticsMessage);

            return new CreateRenewalCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygTypeVersion(), savedUtkast.getIntygsId(),
                originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error(getErrorMessageWhenCopyingFails(originalIntygId));
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    @Override
    @Transactional
    public CreateReplacementCopyResponse createReplacementCopy(CreateReplacementCopyRequest replacementRequest) {

        String originalIntygId = replacementRequest.getOriginalIntygId();
        WebCertUser user = userService.getUser();

        LOG.debug("Creating replacement copy of intyg '{}'", originalIntygId);

        try {
            final Utlatande utlatande = utkastServiceHelper.getUtlatande(replacementRequest.getOriginalIntygId(),
                replacementRequest.getOriginalIntygTyp(),
                false);

            certificateAccessServiceHelper.validateAccessToReplace(utlatande);

            addTestIntygFlagIfNecessaryToCopyRequest(replacementRequest, utlatande.getGrundData().isTestIntyg());

            if (intygService.isRevoked(replacementRequest.getOriginalIntygId(), replacementRequest.getTyp())) {
                LOG.debug("Cannot create replacement certificate for id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Can not create replacement copy - Original certificate is revoked");
            }
            verifyNotReplaced(originalIntygId, CREATE_REPLACEMENT);
            verifyNotComplementedWithSigned(originalIntygId, CREATE_REPLACEMENT);
            verifySigned(utlatande, CREATE_REPLACEMENT);

            UtkastBuilderResponse builderResponse = buildReplacementUtkastBuilderResponse(replacementRequest, originalIntygId);

            if (replacementRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            Utkast savedUtkast = saveAndNotify(builderResponse, user, EventCode.ERSATTER, originalIntygId);

            monitoringService.logIntygCopiedReplacement(savedUtkast.getIntygsId(), originalIntygId);

            final var analyticsMessage = certificateAnalyticsMessageFactory.replace(savedUtkast);
            publishCertificateAnalyticsMessage.publishEvent(analyticsMessage);

            return new CreateReplacementCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygTypeVersion(),
                savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error(getErrorMessageWhenCopyingFails(originalIntygId));
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    /**
     * First, check if the original certificate was issued as a test intyg. If not, check
     * if the patient currently have the testIndicator-flag.
     *
     * @param copyRequest Request to add the TestIntyg-flag for.
     * @param isSourceATestIntyg If the source intyg is a test intyg.
     */
    private void addTestIntygFlagIfNecessaryToCopyRequest(AbstractCreateCopyRequest copyRequest, boolean isSourceATestIntyg) {
        if (isSourceATestIntyg) {
            copyRequest.setTestIntyg(isSourceATestIntyg);
        } else if (patientDetailsResolver.isTestIndicator(copyRequest.getPatient().getPersonId())) {
            copyRequest.setTestIntyg(true);
            copyRequest.getPatient().setTestIndicator(true);
        }
    }

    @Override
    public CreateUtkastFromTemplateResponse createUtkastFromSignedTemplate(CreateUtkastFromTemplateRequest templateRequest) {
        String originalIntygId = templateRequest.getOriginalIntygId();

        LOG.debug("Creating utkast from template (certificate). Certificate = '{}'", originalIntygId);

        try {
            if (intygService.isRevoked(templateRequest.getOriginalIntygId(), templateRequest.getOriginalIntygTyp())) {
                LOG.debug("Cannot create utkast from template. The certificate is revoked. Certificate id = '{}'", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, ORIGINAL_CERTIFICATE_IS_REVOKED);
            }

            // Update patient details here instead of later in the buildUtkastFromTemplateBuilderResponse method.
            // We need to validate access logic early and then depend on patient information available.
            Person patientDetails = updatePatientDetails(templateRequest, false);

            final Utlatande utlatande = utkastServiceHelper.getUtlatande(templateRequest.getOriginalIntygId(),
                templateRequest.getOriginalIntygTyp(),
                false);

            certificateAccessServiceHelper.validateAllowCreateDraftFromSignedTemplate(utlatande);

            addTestIntygFlagIfNecessaryToCopyRequest(templateRequest, utlatande.getGrundData().isTestIntyg());

            verifyNotReplacedWithSigned(templateRequest.getOriginalIntygId(), "create utkast from template");

            UtkastBuilderResponse builderResponse = buildUtkastFromSignedTemplateBuilderResponse(templateRequest, patientDetails,
                true);

            final var user = userService.getUser();
            Utkast savedUtkast = saveAndNotify(builderResponse, user, EventCode.SKAPATFRAN, originalIntygId);

            monitoringService.logUtkastCreatedTemplateManual(savedUtkast.getIntygsId(), savedUtkast.getIntygsTyp(),
                savedUtkast.getSkapadAv().getHsaId(), savedUtkast.getEnhetsId(), originalIntygId, templateRequest.getOriginalIntygTyp());

            if (templateRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            return new CreateUtkastFromTemplateResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygTypeVersion(),
                savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error(getErrorMessageWhenCopyingFails(originalIntygId));
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    @Override
    public CreateUtkastFromTemplateResponse createUtkastCopy(CreateUtkastFromTemplateRequest copyRequest) {
        String originalIntygId = copyRequest.getOriginalIntygId();

        LOG.debug("Creating utkast from utkast certificate '{}'", originalIntygId);

        try {
            if (intygService.isRevoked(copyRequest.getOriginalIntygId(), copyRequest.getOriginalIntygTyp())) {
                LOG.debug("Cannot create utkast from utkast certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, ORIGINAL_CERTIFICATE_IS_REVOKED);
            }

            Utkast utkast = utkastService.getDraft(copyRequest.getOriginalIntygId(), copyRequest.getOriginalIntygTyp(), false);

            lockedDraftAccessServiceHelper.validateAccessToCopy(utkast);

            addTestIntygFlagIfNecessaryToCopyRequest(copyRequest, utkast.isTestIntyg());

            // Validate draft locked
            if (!UtkastStatus.DRAFT_LOCKED.equals(utkast.getStatus())) {
                final String message = "Copy failed due to wrong utkast status. Expected '%s' but was '%s'";
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    String.format(message, UtkastStatus.DRAFT_LOCKED.name(), utkast.getStatus().name()));
            }

            if (utkast.getAterkalladDatum() != null) {
                LOG.debug("Cannot create utkast from utkast certificate with id '{}', the utkast is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, ORIGINAL_CERTIFICATE_IS_REVOKED);
            }

            verifyNoDraftCopy(copyRequest.getOriginalIntygId(), "create utkast copy");

            UtkastBuilderResponse builderResponse = buildUtkastCopyBuilderResponse(copyRequest, originalIntygId);

            final var user = userService.getUser();
            Utkast savedUtkast = saveAndNotify(builderResponse, user, EventCode.KOPIERATFRAN, originalIntygId);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            return new CreateUtkastFromTemplateResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygTypeVersion(),
                savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error(getErrorMessageWhenCopyingFails(originalIntygId));
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    private String getErrorMessageWhenCopyingFails(String originalIntygId) {
        return "Module exception occured when trying to make a copy of " + originalIntygId;
    }

    private void verifySigned(final Utlatande utlatande, final String operation) {
        if (utlatande.getGrundData().getSigneringsdatum() == null) {
            final String message = MessageFormat.format("Certificate {0} is not signed, cannot {1} an unsigned certificate",
                utlatande.getId(), operation);
            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }
    }

    private void verifyNoDraftCopy(String originalIntygId, String operation) {
        final Optional<WebcertCertificateRelation> copiedByRelation = certificateRelationService.getNewestRelationOfType(originalIntygId,
            RelationKod.KOPIA, Arrays.asList(UtkastStatus.DRAFT_COMPLETE, UtkastStatus.DRAFT_INCOMPLETE));
        if (copiedByRelation.isPresent()) {
            String errorString = String.format("Cannot %s for certificate id '%s', copy already exist with id '%s'",
                operation, originalIntygId, copiedByRelation.get().getIntygsId());
            LOG.debug(errorString);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                errorString);
        }
    }

    // Duplicate in IntygServiceImpl, refactor.
    private void verifyNotReplacedWithSigned(String originalIntygId, String operation) {
        final Optional<WebcertCertificateRelation> replacedByRelation = certificateRelationService.getNewestRelationOfType(originalIntygId,
            RelationKod.ERSATT, Collections.singletonList(UtkastStatus.SIGNED));
        if (replacedByRelation.isPresent() && !replacedByRelation.get().isMakulerat()) {
            String errorString = String.format("Cannot %s for certificate id '%s', the certificate is replaced by certificate '%s'",
                operation, originalIntygId, replacedByRelation.get().getIntygsId());
            LOG.debug(errorString);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE_REPLACED,
                errorString);
        }
    }

    private void verifyNotReplaced(String originalIntygId, String operation) {
        final Optional<WebcertCertificateRelation> replacedByRelation = certificateRelationService.getNewestRelationOfType(originalIntygId,
            RelationKod.ERSATT, Arrays.asList(UtkastStatus.values()));
        if (replacedByRelation.isPresent() && !replacedByRelation.get().isMakulerat()) {
            String errorString = String.format("Cannot %s for certificate id '%s', the certificate is replaced by certificate '%s'",
                operation, originalIntygId, replacedByRelation.get().getIntygsId());
            LOG.debug(errorString);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE_REPLACED,
                errorString);
        }
    }

    private void verifyNotComplementedWithSigned(String originalIntygId, String operation) {
        Optional<WebcertCertificateRelation> complementedByRelation = certificateRelationService.getNewestRelationOfType(originalIntygId,
            RelationKod.KOMPLT, Arrays.asList(UtkastStatus.SIGNED));
        if (complementedByRelation.isPresent() && !complementedByRelation.get().isMakulerat()) {
            String errorString = String.format("Cannot %s for certificate id '%s', the certificate is complemented by certificate '%s'",
                operation, originalIntygId, complementedByRelation.get().getIntygsId());
            LOG.debug(errorString);
            // COMPLEMENT_INTYG_EXISTS is needed to provide specific error message in frontend, in intyg list view
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.COMPLEMENT_INTYG_EXISTS,
                errorString);
        }
    }

    private Utkast saveAndNotify(UtkastBuilderResponse builderResponse, WebCertUser user, EventCode eventCode,
        String originalCertificateId) {
        builderResponse.getUtkast().setSkapad(LocalDateTime.now());

        Utkast savedUtkast = utkastRepository.save(builderResponse.getUtkast());

        if (user.getParameters() != null && !Strings.isNullOrEmpty(user.getParameters().getReference())) {
            referensService.saveReferens(savedUtkast.getIntygsId(), user.getParameters().getReference());
        }
        notificationService.sendNotificationForDraftCreated(savedUtkast);
        publishCertificateAnalyticsMessage.publishEvent(
            certificateAnalyticsMessageFactory.createFromTemplate(savedUtkast)
        );

        certificateEventService.createCertificateEventFromCopyUtkast(savedUtkast, user.getHsaId(), eventCode, originalCertificateId);

        LOG.debug("Notification sent: utkast with id '{}' was created as a copy.", savedUtkast.getIntygsId());

        LogRequest logRequest = logRequestFactory.createLogRequestFromUtkast(savedUtkast);
        logService.logCreateIntyg(logRequest);
        return savedUtkast;
    }

    private UtkastBuilderResponse buildCompletionUtkastBuilderResponse(CreateCompletionCopyRequest copyRequest, String originalIntygId,
        boolean addRelation) throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(copyRequest);

        UtkastBuilderResponse builderResponse;
        if (utkastRepository.existsById(originalIntygId)) {
            builderResponse = copyCompletionUtkastBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, addRelation);
        } else {
            builderResponse = copyCompletionUtkastBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, addRelation);
        }

        return builderResponse;
    }

    private UtkastBuilderResponse buildRenewalUtkastBuilderResponse(CreateRenewalCopyRequest copyRequest, String originalIntygId)
        throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(copyRequest);

        UtkastBuilderResponse builderResponse;
        if (utkastRepository.existsById(originalIntygId)) {
            builderResponse = createRenewalUtkastBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, true);
        } else {
            builderResponse = createRenewalUtkastBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, false);
        }

        return builderResponse;
    }

    private UtkastBuilderResponse buildUtkastFromSignedTemplateBuilderResponse(CreateUtkastFromTemplateRequest copyRequest,
        Person patientDetails, boolean addRelation)
        throws ModuleNotFoundException, ModuleException {

        return createUtkastFromTemplateBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, addRelation);
    }

    private UtkastBuilderResponse buildUtkastCopyBuilderResponse(CreateUtkastFromTemplateRequest copyRequest,
        String originalIntygId) throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(copyRequest);

        UtkastBuilderResponse builderResponse;
        if (utkastRepository.existsById(originalIntygId)) {
            builderResponse = createUtkastCopyBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, true);
        } else {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Certificates not found.");
        }

        return builderResponse;
    }

    private UtkastBuilderResponse buildReplacementUtkastBuilderResponse(CreateReplacementCopyRequest replacementCopyRequest,
        String originalIntygId) throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(replacementCopyRequest);

        UtkastBuilderResponse builderResponse;
        if (utkastRepository.existsById(originalIntygId)) {
            builderResponse = createReplacementUtkastBuilder.populateCopyUtkastFromOrignalUtkast(replacementCopyRequest, patientDetails,
                true);
        } else {
            builderResponse = createReplacementUtkastBuilder.populateCopyUtkastFromSignedIntyg(replacementCopyRequest, patientDetails,
                true);
        }

        return builderResponse;
    }

    private Person updatePatientDetails(AbstractCreateCopyRequest copyRequest) {
        return updatePatientDetails(copyRequest, true);
    }

    private Person updatePatientDetails(AbstractCreateCopyRequest copyRequest, boolean isCopyRequest) {
        // I djupintegration version 1 (fk7263) kommer inte patientinformation med i copyRequest.
        // I djupintegration version 3 (nya fkintygen) är patientinformation i copyRequest obligatorisk.
        if (copyRequest.isDjupintegrerad()) {
            if (isCopyRequest && !hasRequiredPatientDetails(copyRequest.getPatient())) {
                return null;
            }
            return copyPatientDetailsFromRequest(copyRequest);
        } else {
            return refreshPatientDetailsFromPUService(copyRequest);
        }
    }

    private boolean hasRequiredPatientDetails(Patient patient) {
        // Vid kopiering i djupintegration är alla patient parametrar utom mellannamn obligatoriska
        return patient != null
            && !Strings.nullToEmpty(patient.getFornamn()).trim().isEmpty()
            && !Strings.nullToEmpty(patient.getEfternamn()).trim().isEmpty()
            && !Strings.nullToEmpty(patient.getPostadress()).trim().isEmpty()
            && !Strings.nullToEmpty(patient.getPostnummer()).trim().isEmpty()
            && !Strings.nullToEmpty(patient.getPostort()).trim().isEmpty();
    }

    private Person copyPatientDetailsFromRequest(AbstractCreateCopyRequest copyRequest) {
        return new Person(
            copyRequest.getPatient().getPersonId(),
            false,
            false,
            copyRequest.getPatient().getFornamn(),
            copyRequest.getPatient().getMellannamn(),
            copyRequest.getPatient().getEfternamn(),
            copyRequest.getPatient().getPostadress(),
            copyRequest.getPatient().getPostnummer(),
            copyRequest.getPatient().getPostort(),
            copyRequest.getPatient().isTestIndicator());
    }

    private Person refreshPatientDetailsFromPUService(AbstractCreateCopyRequest copyRequest) {
        Personnummer personnummer;

        if (copyRequest.containsNyttPatientPersonnummer()) {
            LOG.debug("Request contained a new personnummer to use for the copy");
            personnummer = copyRequest.getNyttPatientPersonnummer();
        } else {
            personnummer = copyRequest.getPatient().getPersonId();
        }

        LOG.debug("Refreshing person data to use for the copy");
        PersonSvar personSvar = getPersonSvar(personnummer);

        if (PersonSvar.Status.ERROR.equals(personSvar.getStatus())) {
            LOG.error("An error occured when using '{}' to lookup person data", hashUtility.hash(personnummer.getPersonnummer()));
            return null;
        } else if (PersonSvar.Status.NOT_FOUND.equals(personSvar.getStatus())) {
            LOG.error("No person data was found using '{}' to lookup person data", hashUtility.hash(personnummer.getPersonnummer()));
            throw new WebCertServiceException(
                WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                "No person data found using '" + hashUtility.hash(personnummer.getPersonnummer()) + "'");
        }

        return personSvar.getPerson();
    }

    private PersonSvar getPersonSvar(Personnummer personnummer) {
        if (personnummer == null) {
            String errMsg = "No personnummer present. Unable to make a call to PUService";
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MISSING_PARAMETER, errMsg);
        }

        return puService.getPerson(personnummer);
    }

    private void checkIntegreradEnhet(UtkastBuilderResponse builderResponse) {
        String orginalEnhetsId = builderResponse.getOrginalEnhetsId();
        Utkast utkastCopy = builderResponse.getUtkast();

        IntegreradEnhetEntry newEntry = new IntegreradEnhetEntry(utkastCopy.getEnhetsId(), utkastCopy.getEnhetsNamn(),
            utkastCopy.getVardgivarId(),
            utkastCopy.getVardgivarNamn());

        integreradeEnheterRegistry.addIfSameVardgivareButDifferentUnits(orginalEnhetsId, newEntry, utkastCopy.getIntygsTyp());
    }
}
