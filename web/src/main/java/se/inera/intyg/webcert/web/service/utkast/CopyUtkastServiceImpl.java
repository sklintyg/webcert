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

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.UtkastStatus;
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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

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
    private PUService personUppgiftsService;

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

        try {
            if (intygService.isRevoked(copyRequest.getOriginalIntygId(), copyRequest.getTyp(), false)) {
                LOG.debug("Cannot create completion copy of certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Original certificate is revoked");
            }
            CopyUtkastBuilderResponse builderResponse = buildCompletionUtkastBuilderResponse(copyRequest, originalIntygId, true);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            Utkast savedUtkast = saveAndNotify(originalIntygId, builderResponse);

            monitoringService.logIntygCopiedCompletion(savedUtkast.getIntygsId(), originalIntygId);

            return new CreateCompletionCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygsId(), originalIntygId);

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

            CopyUtkastBuilderResponse builderResponse = buildRenewalUtkastBuilderResponse(copyRequest, originalIntygId, coherentJournaling);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            Utkast savedUtkast = saveAndNotify(originalIntygId, builderResponse, user);

            monitoringService.logIntygCopiedRenewal(savedUtkast.getIntygsId(), originalIntygId);

            return new CreateRenewalCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    @Override
    @Transactional("jpaTransactionManager")
    public CreateReplacementCopyResponse createReplacementCopy(CreateReplacementCopyRequest replacementRequest) {

        String originalIntygId = replacementRequest.getOriginalIntygId();

        LOG.debug("Creating replacement copy of intyg '{}'", originalIntygId);

        try {
            if (intygService.isRevoked(replacementRequest.getOriginalIntygId(), replacementRequest.getTyp(),
                    replacementRequest.isCoherentJournaling())) {
                LOG.debug("Cannot create replacement certificate for id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                        "Can not create replacement copy - Original certificate is revoked");
            }
            verifyNotReplaced(replacementRequest.getOriginalIntygId(), "create replacement");
            verifyNotComplementedWithSigned(replacementRequest.getOriginalIntygId(), "create replacement");

            CopyUtkastBuilderResponse builderResponse = buildReplacementUtkastBuilderResponse(replacementRequest, originalIntygId);

            if (replacementRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            Utkast savedUtkast = saveAndNotify(originalIntygId, builderResponse);

            monitoringService.logIntygCopiedReplacement(savedUtkast.getIntygsId(), originalIntygId);

            return new CreateReplacementCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygsId(), originalIntygId);

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

            String intygsTyp = copyRequest.getTyp();
            if (authoritiesValidator.given(user, intygsTyp)
                    .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG, AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG,
                            AuthoritiesConstants.FEATURE_UNIKT_UTKAST_INOM_VG).isVerified()) {

                Personnummer personnummer = copyRequest.containsNyttPatientPersonnummer() ? copyRequest.getNyttPatientPersonnummer()
                        : copyRequest.getPatient().getPersonId();

                Map<String, Map<String, Boolean>> intygstypToStringToBoolean = utkastService.checkIfPersonHasExistingIntyg(
                        personnummer, user);

                Boolean utkastExists = intygstypToStringToBoolean.get("utkast").get(intygsTyp);
                Boolean intygExists = intygstypToStringToBoolean.get("intyg").get(intygsTyp);

                if (utkastExists != null && utkastExists) {
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
                    } else if (intygExists && authoritiesValidator.given(user, intygsTyp)
                            .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG_INOM_VG).isVerified()) {
                        throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                                "Certificates of this type must be unique within this caregiver.");
                    }
                }
            }

            verifyNotReplacedWithSigned(copyRequest.getOriginalIntygId(), "create utkast from template");

            CopyUtkastBuilderResponse builderResponse = buildUtkastFromTemplateBuilderResponse(copyRequest, originalIntygId, true,
                    coherentJournaling);

            Utkast savedUtkast = saveAndNotify(originalIntygId, builderResponse, user);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            return new CreateUtkastFromTemplateResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    // Duplicate in IntygServiceImpl, refactor.
    private void verifyNotReplacedWithSigned(String originalIntygId, String operation, UtkastStatus... unallowedStates) {
        final Optional<WebcertCertificateRelation> replacedByRelation = certificateRelationService.getNewestRelationOfType(originalIntygId,
                RelationKod.ERSATT, Arrays.asList(UtkastStatus.SIGNED));
        if (replacedByRelation.isPresent()) {
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
        if (replacedByRelation.isPresent()) {
            String errorString = String.format("Cannot %s for certificate id '%s', the certificate is replaced by certificate '%s'",
                    operation, originalIntygId, replacedByRelation.get().getIntygsId());
            LOG.debug(errorString);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE_REPLACED,
                    errorString);
        }
    }

    // INTYG-3620
    private void verifyNotComplementedWithSigned(String originalIntygId, String operation) {
        Optional<WebcertCertificateRelation> complementedByRelation = certificateRelationService.getNewestRelationOfType(originalIntygId,
                RelationKod.KOMPLT, Arrays.asList(UtkastStatus.SIGNED));
        if (complementedByRelation.isPresent()) {
            String errorString = String.format("Cannot %s for certificate id '%s', the certificate is complemented by certificate '%s'",
                    operation, originalIntygId, complementedByRelation.get().getIntygsId());
            LOG.debug(errorString);
            // INVALID_STATE_COMPLEMENT is needed to provide specific error message in frontend, in intyg list view
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE_COMPLEMENT,
                    errorString);
        }
    }

    private Utkast saveAndNotify(String originalIntygId, CopyUtkastBuilderResponse builderResponse, WebCertUser user) {
        Utkast savedUtkast = utkastRepository.save(builderResponse.getUtkastCopy());

        // notify
        String reference = user.getParameters() != null ? user.getParameters().getReference() : null;
        notificationService.sendNotificationForDraftCreated(savedUtkast, reference);

        LOG.debug("Notification sent: utkast with id '{}' was created as a copy.", savedUtkast.getIntygsId());

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(savedUtkast);
        logService.logCreateIntyg(logRequest);
        return savedUtkast;
    }

    private Utkast saveAndNotify(String originalIntygId, CopyUtkastBuilderResponse builderResponse) {
        WebCertUser user = userService.getUser();
        return saveAndNotify(originalIntygId, builderResponse, user);
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
            builderResponse = createRenewalUtkastBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, false,
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

        Personnummer patientPersonnummer = copyRequest.getPatient().getPersonId();

        if (copyRequest.containsNyttPatientPersonnummer()) {
            patientPersonnummer = copyRequest.getNyttPatientPersonnummer();
            LOG.debug("Request contained a new personnummer to use for the copy");
        }

        LOG.debug("Refreshing person data to use for the copy");

        PersonSvar personSvar = personUppgiftsService.getPerson(patientPersonnummer);

        if (PersonSvar.Status.ERROR.equals(personSvar.getStatus())) {
            LOG.error("An error occured when using '{}' to lookup person data");
            return null;
        } else if (PersonSvar.Status.NOT_FOUND.equals(personSvar.getStatus())) {
            LOG.error("No person data was found using '{}' to lookup person data", patientPersonnummer);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND, "No person data found using '"
                    + patientPersonnummer + "'");
        }

        return personSvar.getPerson();
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
