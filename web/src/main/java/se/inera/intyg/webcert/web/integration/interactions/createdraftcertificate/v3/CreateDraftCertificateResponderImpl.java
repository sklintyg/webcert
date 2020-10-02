/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import com.google.common.base.Joiner;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.tak.model.TakResult;
import se.inera.intyg.webcert.integration.tak.service.TakService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.integration.util.AuthoritiesHelperUtil;
import se.inera.intyg.webcert.web.integration.util.HoSPersonHelper;
import se.inera.intyg.webcert.web.integration.validators.ResultValidator;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@SchemaValidation
public class CreateDraftCertificateResponderImpl implements CreateDraftCertificateResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CreateDraftCertificateResponderImpl.class);

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private CreateNewDraftRequestBuilder draftRequestBuilder;

    @Autowired
    private CreateDraftCertificateValidator validator;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private WebcertUserDetailsService webcertUserDetailsService;

    @Autowired
    private PatientDetailsResolver patientDetailsResolver;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private IntygTextsService intygTextsService;

    @Lazy
    @Autowired
    private TakService takService;

    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    @Override
    public CreateDraftCertificateResponseType createDraftCertificate(String logicalAddress, CreateDraftCertificateType parameters) {

        Intyg utkastsParams = parameters.getIntyg();

        String invokingUserHsaId = utkastsParams.getSkapadAv().getPersonalId().getExtension();
        String invokingUnitHsaId = utkastsParams.getSkapadAv().getEnhet().getEnhetsId().getExtension();

        IntygUser user;
        try {
            user = webcertUserDetailsService.loadUserByHsaId(invokingUserHsaId);

        } catch (Exception e) {
            return createMIUErrorResponse(utkastsParams);
        }

        // Validate draft parameters
        ResultValidator resultsValidator = validator.validate(utkastsParams);
        if (resultsValidator.hasErrors()) {
            return createValidationErrorResponse(resultsValidator);
        }

        // Check if the invoking health personal has MIU rights on care unit
        if (!HoSPersonHelper.findVardenhetEllerMottagning(user, invokingUnitHsaId).isPresent()) {
            return createMIUErrorResponse(utkastsParams);
        }

        user.changeValdVardenhet(invokingUnitHsaId);
        // Make sure pilots and features are loaded!
        webcertUserDetailsService.decorateIntygUserWithAvailableFeatures(user);

        ResultValidator appErrorsValidator = validator.validateApplicationErrors(utkastsParams, user);
        if (appErrorsValidator.hasErrors()) {
            return createApplicationErrorResponse(appErrorsValidator);
        }

        LOG.debug("Creating draft for invoker '{}' on unit '{}'", utkastsParams.getSkapadAv().getPersonalId().getExtension(),
            invokingUnitHsaId);

        String intygsTyp = moduleRegistry.getModuleIdFromExternalId(utkastsParams.getTypAvIntyg().getCode());
        // Default to use latest version, since there is no info in request specifying version
        String latestIntygTypeVersion = intygTextsService.getLatestVersion(intygsTyp);

        Personnummer personnummer = Personnummer.createPersonnummer(
            utkastsParams.getPatient().getPersonId().getExtension())
            .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                "Failed to create valid personnummer for createDraft request"));
        SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personnummer);

        if (AuthoritiesHelperUtil.mayNotCreateUtkastForSekretessMarkerad(sekretessStatus, user, intygsTyp)) {
            return createErrorResponse("Intygstypen " + intygsTyp.toUpperCase()
                + " kan inte utfärdas för patienter med skyddade personuppgifter", ErrorIdType.APPLICATION_ERROR);
        }

        Map<String, Map<String, PreviousIntyg>> intygstypToPreviousIntyg =
                utkastService.checkIfPersonHasExistingIntyg(personnummer, user, null);
        Optional<WebCertServiceErrorCodeEnum> utkastUnique = AuthoritiesHelperUtil.validateUtkastMustBeUnique(user, intygsTyp,
            intygstypToPreviousIntyg);
        Optional<WebCertServiceErrorCodeEnum> intygUnique = AuthoritiesHelperUtil.validateIntygMustBeUnique(user, intygsTyp,
            intygstypToPreviousIntyg, LocalDateTime.now());
        WebCertServiceErrorCodeEnum uniqueErrorCode = utkastUnique.orElse(intygUnique.orElse(null));

        if (uniqueErrorCode != null) {
            String uniqueErrorString;
            switch (uniqueErrorCode) {
                case UTKAST_FROM_SAME_VARDGIVARE_EXISTS:
                    uniqueErrorString = "Draft of this type must be unique within caregiver.";
                    break;
                case INTYG_FROM_SAME_VARDGIVARE_EXISTS:
                    uniqueErrorString = "Certificates of this type must be unique within this caregiver.";
                    break;
                case INTYG_FROM_OTHER_VARDGIVARE_EXISTS:
                    uniqueErrorString = "Certificates of this type must be globally unique.";
                    break;
                default:
                    uniqueErrorString = "Unexpected error occurred.";
                    break;
            }
            return createErrorResponse(uniqueErrorString, ErrorIdType.APPLICATION_ERROR);
        }

        if (authoritiesValidator.given(user, intygsTyp).features(AuthoritiesConstants.FEATURE_TAK_KONTROLL).isVerified()) {
            // Check if invoking health care unit has required TAK
            TakResult takResult = takService.verifyTakningForCareUnit(invokingUnitHsaId, intygsTyp, SchemaVersion.VERSION_3, user);
            if (!takResult.isValid()) {
                String error = Joiner.on("; ").join(takResult.getErrorMessages());
                LOG.warn("Invalid TAK result for unit '{}'. Returning APPLICATION_ERROR: {}", invokingUnitHsaId, error);
                return createErrorResponse(error, ErrorIdType.APPLICATION_ERROR);
            }
        }

        // Standard draft creation
        Utkast utkast = createNewDraft(utkastsParams, latestIntygTypeVersion, user);

        return createSuccessResponse(utkast.getIntygsId(), invokingUnitHsaId);
    }

    private Utkast createNewDraft(Intyg utkastRequest, String latestIntygTypeVersion, IntygUser user) {

        LOG.debug("Creating draft for invoker '{}' on unit '{}'", utkastRequest.getSkapadAv().getPersonalId().getExtension(),
            utkastRequest.getSkapadAv().getEnhet().getEnhetsId().getExtension());

        // Create draft request
        CreateNewDraftRequest draftRequest = draftRequestBuilder.buildCreateNewDraftRequest(utkastRequest, latestIntygTypeVersion, user);

        // Add the creating vardenhet to registry
        addVardenhetToRegistry(draftRequest);

        return utkastService.createNewDraft(draftRequest);
    }

    /**
     * The response sent back to caller when an error is raised.
     */
    private CreateDraftCertificateResponseType createErrorResponse(String errorMsg, ErrorIdType errorType) {
        ResultType result = ResultTypeUtil.errorResult(errorType, errorMsg);

        CreateDraftCertificateResponseType response = new CreateDraftCertificateResponseType();
        response.setResult(result);
        return response;
    }

    /**
     * Builds a specific MIU error response.
     */
    private CreateDraftCertificateResponseType createMIUErrorResponse(Intyg utkastType) {

        String invokingUserHsaId = utkastType.getSkapadAv().getPersonalId().getExtension();
        String invokingUnitHsaId = utkastType.getSkapadAv().getEnhet().getEnhetsId().getExtension();

        monitoringLogService.logMissingMedarbetarUppdrag(invokingUserHsaId, invokingUnitHsaId);

        String errMsg = String.format("No valid MIU was found for person %s on unit %s, can not create draft!", invokingUserHsaId,
            invokingUnitHsaId);
        return createErrorResponse(errMsg, ErrorIdType.VALIDATION_ERROR);
    }

    /**
     * Builds a specific validation error response.
     */
    private CreateDraftCertificateResponseType createValidationErrorResponse(ResultValidator resultsValidator) {
        String errMsgs = resultsValidator.getErrorMessagesAsString();
        LOG.warn("Intyg did not validate correctly: {}", errMsgs);
        return createErrorResponse(errMsgs, ErrorIdType.VALIDATION_ERROR);
    }

    /**
     * Builds a specific application error response.
     */
    private CreateDraftCertificateResponseType createApplicationErrorResponse(ResultValidator resultsValidator) {
        String errMsgs = resultsValidator.getErrorMessagesAsString();
        LOG.warn("Intyg did not pass APPLICATION_ERROR check correctly: {}", errMsgs);
        return createErrorResponse(errMsgs, ErrorIdType.APPLICATION_ERROR);
    }

    /**
     * The response sent back to caller when creating a certificate draft succeeded.
     */
    private CreateDraftCertificateResponseType createSuccessResponse(String nyttUtkastsId, String invokingUnitHsaId) {
        ResultType result = ResultTypeUtil.okResult();

        IntygId intygId = new IntygId();
        intygId.setRoot(invokingUnitHsaId);
        intygId.setExtension(nyttUtkastsId);

        CreateDraftCertificateResponseType response = new CreateDraftCertificateResponseType();
        response.setResult(result);
        response.setIntygsId(intygId);
        return response;
    }

    private void addVardenhetToRegistry(CreateNewDraftRequest utkastsRequest) {

        Vardenhet vardenhet = utkastsRequest.getHosPerson().getVardenhet();
        Vardgivare vardgivare = vardenhet.getVardgivare();

        IntegreradEnhetEntry integreradEnhet = new IntegreradEnhetEntry(vardenhet.getEnhetsid(),
            vardenhet.getEnhetsnamn(), vardgivare.getVardgivarid(), vardgivare.getVardgivarnamn());

        integreradeEnheterRegistry.putIntegreradEnhet(integreradEnhet, false, true);
    }
}
