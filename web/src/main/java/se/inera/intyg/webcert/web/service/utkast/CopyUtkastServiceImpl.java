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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.schemas.contract.Personnummer;
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
import se.inera.intyg.webcert.web.service.relation.RelationService;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.dto.CopyUtkastBuilderResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCompletionCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateRenewalCopyResponse;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateReplacementCopyResponse;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RelationItem;

import java.util.Optional;

@Service
public class CopyUtkastServiceImpl implements CopyUtkastService {

    private static final Logger LOG = LoggerFactory.getLogger(CopyUtkastServiceImpl.class);

    @Autowired
    private IntygService intygService;

    @Autowired
    private RelationService relationService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private PUService personUppgiftsService;

    @Autowired
    @Qualifier("copyCompletionUtkastBuilder")
    private CopyUtkastBuilder<CreateCompletionCopyRequest> copyCompletionUtkastBuilder;

    @Autowired
    @Qualifier("createCopyUtkastBuilder")
    private CopyUtkastBuilder<CreateCopyRequest> createCopyUtkastBuilder;

    @Autowired
    @Qualifier("createRenewalUtkastBuilder")
    private CopyUtkastBuilder<CreateRenewalCopyRequest> createRenewalUtkastBuilder;

    @Autowired
    @Qualifier("createReplacementUtkastBuilder")
    private CopyUtkastBuilder<CreateReplacementCopyRequest> createReplacementUtkastBuilder;

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

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.utkast.CopyUtkastService#createCopy(se.inera.intyg.webcert.web.service.utkast.
     * dto.
     * CreateNewDraftCopyRequest)
     */
    @Override
    @Transactional("jpaTransactionManager")
    public CreateNewDraftCopyResponse createCopy(CreateNewDraftCopyRequest copyRequest) {

        String originalIntygId = copyRequest.getOriginalIntygId();

        LOG.debug("Creating copy of intyg '{}'", originalIntygId);

        try {

            if (intygService.isRevoked(originalIntygId, copyRequest.getTyp(), copyRequest.isCoherentJournaling())) {
                LOG.debug("Cannot create copy of certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Original certificate is revoked");
            }

            verifyNotReplaced(originalIntygId, "create copy");

            CopyUtkastBuilderResponse builderResponse;

            builderResponse = buildCopyUtkastBuilderResponse(copyRequest, originalIntygId);

            Utkast savedUtkast = saveAndNotify(originalIntygId, builderResponse);

            monitoringService.logIntygCopied(savedUtkast.getIntygsId(), originalIntygId);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            return new CreateNewDraftCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygsId());

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

            Utkast savedUtkast = saveAndNotify(originalIntygId, builderResponse);

            monitoringService.logIntygCopiedCompletion(savedUtkast.getIntygsId(), originalIntygId);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

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
            if (intygService.isRevoked(copyRequest.getOriginalIntygId(), copyRequest.getTyp(), coherentJournaling)) {
                LOG.debug("Cannot renew certificate with id '{}', the certificate is revoked", originalIntygId);
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Original certificate is revoked");
            }
            verifyNotReplaced(copyRequest.getOriginalIntygId(), "create renewal");

            CopyUtkastBuilderResponse builderResponse =
                    buildRenewalUtkastBuilderResponse(copyRequest, originalIntygId, true, coherentJournaling);

            Utkast savedUtkast = saveAndNotify(originalIntygId, builderResponse, user);

            monitoringService.logIntygCopiedRenewal(savedUtkast.getIntygsId(), originalIntygId);

            if (copyRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

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

            CopyUtkastBuilderResponse builderResponse = buildReplacementUtkastBuilderResponse(replacementRequest, originalIntygId);

            Utkast savedUtkast = saveAndNotify(originalIntygId, builderResponse);

            monitoringService.logIntygCopiedReplacement(savedUtkast.getIntygsId(), originalIntygId);

            if (replacementRequest.isDjupintegrerad()) {
                checkIntegreradEnhet(builderResponse);
            }

            return new CreateReplacementCopyResponse(savedUtkast.getIntygsTyp(), savedUtkast.getIntygsId(), originalIntygId);

        } catch (ModuleException | ModuleNotFoundException me) {
            LOG.error("Module exception occured when trying to make a copy of " + originalIntygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.MODULE_PROBLEM, me);
        }
    }

    private void verifyNotReplaced(String originalIntygId, String operation) {
        final Optional<RelationItem> replacedByRelation = relationService.getReplacedByRelation(originalIntygId);
        if (replacedByRelation.isPresent()) {
            String errorString = String.format("Cannot %s for certificate id '%s', the certificate is replaced by certificate '%s'",
                    operation, originalIntygId, replacedByRelation.get().getIntygsId());
            LOG.debug(errorString);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE_REPLACED,
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

    private CopyUtkastBuilderResponse buildCopyUtkastBuilderResponse(CreateNewDraftCopyRequest copyRequest, String originalIntygId)
            throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(copyRequest);

        CopyUtkastBuilderResponse builderResponse;
        if (utkastRepository.exists(originalIntygId)) {
            builderResponse = createCopyUtkastBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, false,
                    copyRequest.isCoherentJournaling(), false);
        } else {
            builderResponse = createCopyUtkastBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, false,
                    copyRequest.isCoherentJournaling(), false);
        }

        return builderResponse;
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
            boolean addRelation, boolean coherentJournaling) throws ModuleNotFoundException, ModuleException {

        Person patientDetails = updatePatientDetails(copyRequest);

        CopyUtkastBuilderResponse builderResponse;
        if (utkastRepository.exists(originalIntygId)) {
            builderResponse = createRenewalUtkastBuilder.populateCopyUtkastFromOrignalUtkast(copyRequest, patientDetails, addRelation,
                    coherentJournaling, false);
        } else {
            builderResponse = createRenewalUtkastBuilder.populateCopyUtkastFromSignedIntyg(copyRequest, patientDetails, addRelation,
                    coherentJournaling, false);
        }

        return builderResponse;
    }

    private CopyUtkastBuilderResponse buildReplacementUtkastBuilderResponse(CreateReplacementCopyRequest replacementCopyRequest,
            String originalIntygId)
            throws ModuleNotFoundException, ModuleException {

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

    private Person updatePatientDetails(CreateCopyRequest copyRequest) {
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

    private Person copyPatientDetailsFromRequest(CreateCopyRequest copyRequest) {
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

    private Person refreshPatientDetailsFromPUService(CreateCopyRequest copyRequest) {

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
