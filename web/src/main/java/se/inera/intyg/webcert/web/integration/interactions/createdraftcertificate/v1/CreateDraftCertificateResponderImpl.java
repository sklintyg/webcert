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
package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v1;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import se.inera.intyg.common.fk7263.schemas.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
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
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.CreateDraftCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v1.Utlatande;
import se.riv.clinicalprocess.healthcond.certificate.types.v1.UtlatandeId;
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultType;

import java.util.Map;

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

    @Lazy
    @Autowired
    private TakService takService;

    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    @Override
    public CreateDraftCertificateResponseType createDraftCertificate(String logicalAddress, CreateDraftCertificateType parameters) {

        Utlatande utkastsParams = parameters.getUtlatande();

        // Redo this: Build a full Vårdgivare -> Vårdenhet -> Mottagning tree and then check.
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

        ResultValidator appErrorsValidator = validator.validateApplicationErrors(utkastsParams, user);
        if (appErrorsValidator.hasErrors()) {
            return createApplicationErrorResponse(appErrorsValidator);
        }

        LOG.debug("Creating draft for invoker '{}' on unit '{}'", invokingUserHsaId, invokingUnitHsaId);

        // Check if the invoking health personal has MIU rights on care unit
        if (!checkMIU(user, invokingUnitHsaId)) {
            return createMIUErrorResponse(utkastsParams);
        }

        user.changeValdVardenhet(invokingUnitHsaId);

        String intygsTyp = utkastsParams.getTypAvUtlatande().getCode().toLowerCase();
        Personnummer personnummer = Personnummer.createValidatedPersonnummerWithDash(
                utkastsParams.getPatient().getPersonId().getExtension()).orElseThrow(() ->
                new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                        "Failed to create valid personnummer for createDraft reques"));

        final SekretessStatus sekretessStatus = patientDetailsResolver.getSekretessStatus(personnummer);

        if (AuthoritiesHelperUtil.mayNotCreateUtkastForSekretessMarkerad(sekretessStatus, user, intygsTyp)) {
            return createErrorResponse("Intygstypen " + intygsTyp + " kan inte utfärdas för patienter med sekretessmarkering",
                    ErrorIdType.APPLICATION_ERROR);
        }

        Map<String, Map<String, Boolean>> intygstypToBoolean = utkastService.checkIfPersonHasExistingIntyg(personnummer, user);
        String uniqueErrorString = AuthoritiesHelperUtil.validateMustBeUnique(user, intygsTyp, intygstypToBoolean);

        if (!uniqueErrorString.isEmpty()) {
            return createErrorResponse(uniqueErrorString, ErrorIdType.APPLICATION_ERROR);
        }

        if (authoritiesValidator.given(user, intygsTyp).features(AuthoritiesConstants.FEATURE_TAK_KONTROLL).isVerified()) {
            // Check if invoking health care unit has required TAK
            SchemaVersion schemaVersion = integreradeEnheterRegistry.getSchemaVersion(invokingUnitHsaId, intygsTyp)
                    .orElse(SchemaVersion.VERSION_1);
            TakResult takResult = takService.verifyTakningForCareUnit(invokingUnitHsaId, intygsTyp, schemaVersion, user);
            if (!takResult.isValid()) {
                String error = Joiner.on("; ").join(takResult.getErrorMessages());
                return createErrorResponse(error, ErrorIdType.APPLICATION_ERROR);
            }
        }

        // Create the draft
        Utkast utkast = createNewDraft(utkastsParams, user);

        return createSuccessResponse(utkast.getIntygsId());
    }

    private Utkast createNewDraft(Utlatande utlatandeRequest, IntygUser user) {

        String invokingUserHsaId = utlatandeRequest.getSkapadAv().getPersonalId().getExtension();
        String invokingUnitHsaId = utlatandeRequest.getSkapadAv().getEnhet().getEnhetsId().getExtension();
        LOG.debug("Creating draft for invoker '{}' on unit '{}'", invokingUserHsaId, invokingUnitHsaId);

        // Create draft request
        CreateNewDraftRequest draftRequest = draftRequestBuilder.buildCreateNewDraftRequest(utlatandeRequest, user);

        // Add the creating vardenhet to registry
        addVardenhetToRegistry(draftRequest);

        // Create draft and return its id
        return utkastService.createNewDraft(draftRequest);
    }

    /**
     * Method checks if invoking person, i.e the health care personal,
     * is entitled to look at the information.
     */
    private boolean checkMIU(IntygUser user, String invokingUnitHsaId) {
        return HoSPersonHelper.findVardenhetEllerMottagning(user, invokingUnitHsaId).isPresent();
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
    private CreateDraftCertificateResponseType createMIUErrorResponse(Utlatande utlatandeType) {

        String invokingUserHsaId = utlatandeType.getSkapadAv().getPersonalId().getExtension();
        String invokingUnitHsaId = utlatandeType.getSkapadAv().getEnhet().getEnhetsId().getExtension();

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
        LOG.warn("Utlatande did not validate correctly: {}", errMsgs);
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
    private CreateDraftCertificateResponseType createSuccessResponse(String nyttUtkastsId) {
        ResultType result = ResultTypeUtil.okResult();

        UtlatandeId utlId = new UtlatandeId();
        utlId.setRoot("utlatandeId");
        utlId.setExtension(nyttUtkastsId);

        CreateDraftCertificateResponseType response = new CreateDraftCertificateResponseType();
        response.setResult(result);
        response.setUtlatandeId(utlId);
        return response;
    }

    private void addVardenhetToRegistry(CreateNewDraftRequest utkastsRequest) {

        Vardenhet vardenhet = utkastsRequest.getHosPerson().getVardenhet();
        Vardgivare vardgivare = vardenhet.getVardgivare();

        IntegreradEnhetEntry integreradEnhet = new IntegreradEnhetEntry(vardenhet.getEnhetsid(),
                vardenhet.getEnhetsnamn(), vardgivare.getVardgivarid(), vardgivare.getVardgivarnamn());

        integreradeEnheterRegistry.putIntegreradEnhet(integreradEnhet, true, false);
    }

}
