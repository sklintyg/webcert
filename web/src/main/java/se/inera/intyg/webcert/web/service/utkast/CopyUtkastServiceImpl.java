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

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.AbstractCreateCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateUtkastFromTemplateResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;

@Service
public class CopyUtkastServiceImpl implements CopyUtkastService {

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
    private LogService logService;

    @Autowired
    private MonitoringLogService monitoringService;

    @Autowired
    private WebCertUserService userService;

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private ReferensService referensService;

    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.utkast.CopyUtkastService#createCopy(se.inera.intyg.webcert.web.service.utkast.
     * dto.
     * CreateNewDraftCopyRequest)
     */
    @Override
    public CreateCompletionCopyResponse createCompletion(CreateCompletionCopyRequest copyRequest) {
        String originalIntygId = copyRequest.getOriginalIntygId();

        LOG.debug("Creating completion to intyg '{}'", originalIntygId);
        WebCertUser user = userService.getUser();

        try {
            if (intygService.isRevoked(copyRequest.getOriginalIntygId(), copyRequest.getTyp(), false)) {
                LOG.debug("Cannot create completion copy of certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Original certificate is revoked");
            }
            CopyUtkastBuilderResponse builderResponse = buildCompletionUtkastBuilderResponse(copyRequest, originalIntygId, true);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            Utkast savedUtkast = saveAndNotify(builderResponse, user);

            monitoringService.logIntygCopiedCompletion(savedUtkast.getIntygsId(), originalIntygId);

            return new CreateCompletionCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygTypeVersion(),
                    savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.utkast.CopyUtkastService#createCopy(se.inera.intyg.webcert.web.service.utkast.
     * dto.
     * CreateRenewalCopyRequest)
     */
    @Override
    public CreateRenewalCopyResponse createRenewalCopy(CreateRenewalCopyRequest copyRequest) {
        String originalIntygId = copyRequest.getOriginalIntygId();

        LOG.debug("Creating renewal for intyg '{}'", originalIntygId);

        WebCertUser user = userService.getUser();
        boolean coherentJournaling = user != null && user.getParameters() != null && user.getParameters().isSjf();

        try {
            if (intygService.isRevoked(copyRequest.getOriginalIntygId(), copyRequest.getOriginalIntygTyp(), coherentJournaling)) {
                LOG.debug("Cannot renew certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Original certificate is revoked");
            }
            verifyNotReplacedWithSigned(copyRequest.getOriginalIntygId(), "create renewal");
            verifyNotComplementedWithSigned(copyRequest.getOriginalIntygId(), "create renewal");
            verifySigned(originalIntygId, copyRequest.getOriginalIntygTyp(), "create renewal");

            CopyUtkastBuilderResponse builderResponse = buildRenewalUtkastBuilderResponse(copyRequest, originalIntygId, coherentJournaling);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            Utkast savedUtkast = saveAndNotify(builderResponse, user);

            monitoringService.logIntygCopiedRenewal(savedUtkast.getIntygsId(), originalIntygId);

            return new CreateRenewalCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygTypeVersion(), savedUtkast.getIntygsId(),
                    originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
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
            if (intygService.isRevoked(replacementRequest.getOriginalIntygId(), replacementRequest.getTyp(),
                    replacementRequest.isCoherentJournaling())) {
                LOG.debug("Cannot create replacement certificate for id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                        "Can not create replacement copy - Original certificate is revoked");
            }
            verifyNotReplaced(originalIntygId, "create replacement");
            verifyNotComplementedWithSigned(originalIntygId, "create replacement");
            verifySigned(originalIntygId, replacementRequest.getOriginalIntygTyp(), "create replacement");

            CopyUtkastBuilderResponse builderResponse = buildReplacementUtkastBuilderResponse(replacementRequest, originalIntygId);

            if (replacementRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            Utkast savedUtkast = saveAndNotify(builderResponse, user);

            monitoringService.logIntygCopiedReplacement(savedUtkast.getIntygsId(), originalIntygId);

            return new CreateReplacementCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygTypeVersion(),
                    savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    @Override
    public CreateUtkastFromTemplateResponse createUtkastFromTemplate(CreateUtkastFromTemplateRequest copyRequest) {
        String originalIntygId = copyRequest.getOriginalIntygId();

        LOG.debug("Creating utkast from template certificate '{}'", originalIntygId);

        WebCertUser user = userService.getUser();
        boolean coherentJournaling = user != null && user.getParameters() != null && user.getParameters().isSjf();

        try {
            if (intygService.isRevoked(copyRequest.getOriginalIntygId(), copyRequest.getOriginalIntygTyp(), coherentJournaling)) {
                LOG.debug("Cannot create utkast from template certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Original certificate is revoked");
            }

            verifyUniktIntyg(user, copyRequest);

            verifyNotReplacedWithSigned(copyRequest.getOriginalIntygId(), "create utkast from template");

            CopyUtkastBuilderResponse builderResponse = buildUtkastFromTemplateBuilderResponse(copyRequest, originalIntygId, true,
                    coherentJournaling);

            Utkast savedUtkast = saveAndNotify(builderResponse, user);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            return new CreateUtkastFromTemplateResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygTypeVersion(),
                    savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    @Override
    public CreateUtkastFromTemplateResponse createUtkastCopy(CreateUtkastFromTemplateRequest copyRequest) {
        String originalIntygId = copyRequest.getOriginalIntygId();

        LOG.debug("Creating utkast from utkast certificate '{}'", originalIntygId);

        WebCertUser user = userService.getUser();
        boolean coherentJournaling = user != null && user.getParameters() != null && user.getParameters().isSjf();

        try {
            if (intygService.isRevoked(copyRequest.getOriginalIntygId(), copyRequest.getOriginalIntygTyp(), coherentJournaling)) {
                LOG.debug("Cannot create utkast from utkast certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Original certificate is revoked");
            }

            Utkast utkast = utkastService.getDraft(copyRequest.getOriginalIntygId(), copyRequest.getOriginalIntygTyp());

            // Validate draft locked
            if (!UtkastStatus.DRAFT_LOCKED.equals(utkast.getStatus())) {
                LOG.info("User is not allowed to copy intyg with status: {}", utkast.getStatus());
                final String message = "Copy failed due to wrong utkast status";
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
            }

            if (utkast.getAterkalladDatum() != null) {
                LOG.debug("Cannot create utkast from utkast certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Original certificate is revoked");
            }

            verifyNoDraftCopy(copyRequest.getOriginalIntygId(), "create utkast copy");

            verifyUniktIntyg(user, copyRequest);

            CopyUtkastBuilderResponse builderResponse = buildUtkastCopyBuilderResponse(copyRequest, originalIntygId, coherentJournaling);

            Utkast savedUtkast = saveAndNotify(builderResponse, user);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            return new CreateUtkastFromTemplateResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygTypeVersion(),
                    savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    private void verifySigned(final String originalIntygId, final String originalIntygTyp, final String operation) {
        final Utkast utkast = utkastRepository.findByIntygsIdAndIntygsTyp(originalIntygId, originalIntygTyp);
        if (utkast == null || UtkastServiceImpl.isUtkast(utkast) || utkast.getSignatur() == null) {
            final String message = MessageFormat.format("Certificate {0} is not signed, cannot {1} an unsigned certificate",
                    originalIntygId, operation);

            LOG.debug(message);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, message);
        }
    }

    private void verifyUniktIntyg(WebCertUser user, CreateUtkastFromTemplateRequest copyRequest) {
        String intygsTyp = copyRequest.getTyp();

        if (authoritiesValidator.given(user, intygsTyp)
                .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG, AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG,
                        AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG).isVerified()) {

            Personnummer personnummer = copyRequest.containsNyttPatientPersonnummer() ? copyRequest.getNyttPatientPersonnummer()
                    : copyRequest.getPatient().getPersonId();

            Map<String, Map<String, PreviousIntyg>> intygstypToStringToPreviousIntyg = utkastService.checkIfPersonHasExistingIntyg(
                    personnummer, user);

            PreviousIntyg utkastExists = intygstypToStringToPreviousIntyg.get("utkast").get(intygsTyp);
            PreviousIntyg intygExists = intygstypToStringToPreviousIntyg.get("intyg").get(intygsTyp);

            if (utkastExists != null && utkastExists.isSameVardgivare()) {
                if (authoritiesValidator.given(user, intygsTyp).features(AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG)
                        .isVerified()) {
                    throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                            "Drafts of this type must be unique within this caregiver.");
                }
            }

            if (intygExists != null) {
                if (authoritiesValidator.given(user, intygsTyp).features(AuthoritiesConstants.FEATURE_UNIKT_INTYG).isVerified()) {
                    throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                            "Certificates of this type must be globally unique.");
                } else if (intygExists.isSameVardgivare() && authoritiesValidator.given(user, intygsTyp)
                        .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG).isVerified()) {
                    throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                            "Certificates of this type must be unique within this caregiver.");
                }
            }
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
        if (complementedByRelation.isPresent()) {
            String errorString = String.format("Cannot %s for certificate id '%s', the certificate is complemented by certificate '%s'",
                    operation, originalIntygId, complementedByRelation.get().getIntygsId());
            LOG.debug(errorString);
            // COMPLEMENT_INTYG_EXISTS is needed to provide specific error message in frontend, in intyg list view
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.COMPLEMENT_INTYG_EXISTS,
                    errorString);
        }
    }

    private Utkast saveAndNotify(CopyUtkastBuilderResponse builderResponse, WebCertUser user) {
        builderResponse.getUtkastCopy().setSkapad(LocalDateTime.now());

        Utkast savedUtkast = utkastRepository.save(builderResponse.getUtkastCopy());

        if (user.getParameters() != null && !Strings.isNullOrEmpty(user.getParameters().getReference())) {
            referensService.saveReferens(savedUtkast.getIntygsId(), user.getParameters().getReference());
        }
        notificationService.sendNotificationForDraftCreated(savedUtkast);

        LOG.debug("Notification sent: utkast with id '{}' was created as a copy.", savedUtkast.getIntygsId());

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(savedUtkast);
        logService.logCreateIntyg(logRequest);
        return savedUtkast;
    }

    private CopyUtkastBuilderResponse buildCompletionUtkastBuilderResponse(CreateCompletionCopyRequest copyRequest, String originalIntygId,
            boolean addRelation) throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(copyRequest);

        CopyUtkastBuilderResponse builderResponse;
        if (utkastRepository.exists(originalIntygId)) {
            builderResponse = copyCompletionUtkastBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, addRelation,
                    false, false);
        } else {
            builderResponse = copyCompletionUtkastBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, addRelation,
                    false, false);
        }

        return builderResponse;
    }

    private CopyUtkastBuilderResponse buildRenewalUtkastBuilderResponse(CreateRenewalCopyRequest copyRequest, String originalIntygId,
            boolean coherentJournaling) throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(copyRequest);

        CopyUtkastBuilderResponse builderResponse;
        if (utkastRepository.exists(originalIntygId)) {
            builderResponse = createRenewalUtkastBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, true,
                    coherentJournaling, false);
        } else {
            builderResponse = createRenewalUtkastBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, false,
                    coherentJournaling, false);
        }

        return builderResponse;
    }

    private CopyUtkastBuilderResponse buildUtkastFromTemplateBuilderResponse(CreateUtkastFromTemplateRequest copyRequest,
            String originalIntygId, boolean addRelation, boolean coherentJournaling) throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(copyRequest);

        CopyUtkastBuilderResponse builderResponse;
        if (utkastRepository.exists(originalIntygId)) {
            builderResponse = createUtkastFromTemplateBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, addRelation,
                    coherentJournaling, false);
        } else {
            builderResponse = createUtkastFromTemplateBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, addRelation,
                    coherentJournaling, false);
        }

        return builderResponse;
    }

    private CopyUtkastBuilderResponse buildUtkastCopyBuilderResponse(CreateUtkastFromTemplateRequest copyRequest,
             String originalIntygId, boolean coherentJournaling) throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(copyRequest);

        CopyUtkastBuilderResponse builderResponse;
        if (utkastRepository.exists(originalIntygId)) {
            builderResponse = createUtkastCopyBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, true,
                    coherentJournaling, false);
        } else {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "Certificates not found.");
        }

        return builderResponse;
    }

    private CopyUtkastBuilderResponse buildReplacementUtkastBuilderResponse(CreateReplacementCopyRequest replacementCopyRequest,
            String originalIntygId) throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(replacementCopyRequest);

        CopyUtkastBuilderResponse builderResponse;
        if (utkastRepository.exists(originalIntygId)) {
            builderResponse = createReplacementUtkastBuilder.populateCopyUtkastFromOrignalUtkast(replacementCopyRequest, patientDetails,
                    true, replacementCopyRequest.isCoherentJournaling(), true);
        } else {
            builderResponse = createReplacementUtkastBuilder.populateCopyUtkastFromSignedIntyg(replacementCopyRequest, patientDetails, true,
                    replacementCopyRequest.isCoherentJournaling(), true);
        }

        return builderResponse;
    }

    private Person updatePatientDetails(AbstractCreateCopyRequest copyRequest) {
        // I djupintegration version 1 (fk7263) kommer inte patientinformation med i copyrequest.
        // I djupintegration version 3 (nya fkintygen) är patientinformation i copyrequest obligatorisk.
        if (copyRequest.isDjupintegrerad()) {
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
        if (!hasRequiredPatientDetails(copyRequest.getPatient())) {
            return null;
        }
        return new Person(
                copyRequest.getPatient().getPersonId(),
                false,
                false,
                copyRequest.getPatient().getFornamn(),
                copyRequest.getPatient().getMellannamn(),
                copyRequest.getPatient().getEfternamn(),
                copyRequest.getPatient().getPostadress(),
                copyRequest.getPatient().getPostnummer(),
                copyRequest.getPatient().getPostort());
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
            LOG.error("An error occured when using '{}' to lookup person data", personnummer.getPersonnummerHash());
            return null;
        } else if (PersonSvar.Status.NOT_FOUND.equals(personSvar.getStatus())) {
            LOG.error("No person data was found using '{}' to lookup person data", personnummer.getPersonnummerHash());
            throw new WebCertServiceException(
                    WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "No person data found using '" + personnummer.getPersonnummerHash() + "'");
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

    private void checkIntegreradEnhet(CopyUtkastBuilderResponse builderResponse) {

        String orginalEnhetsId = builderResponse.getOrginalEnhetsId();
        Utkast utkastCopy = builderResponse.getUtkastCopy();

        IntegreradEnhetEntry newEntry = new IntegreradEnhetEntry(utkastCopy.getEnhetsId(), utkastCopy.getEnhetsNamn(),
                utkastCopy.getVardgivarId(),
                utkastCopy.getVardgivarNamn());

        integreradeEnheterRegistry.addIfSameVardgivareButDifferentUnits(orginalEnhetsId, newEntry, utkastCopy.getIntygsTyp());
    }
}
