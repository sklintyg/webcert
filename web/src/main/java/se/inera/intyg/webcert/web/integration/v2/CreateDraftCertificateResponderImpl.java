/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.integration.v2;

import java.util.List;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.common.integration.hsa.services.HsaPersonService;
import se.inera.intyg.common.schemas.clinicalprocess.healthcond.certificate.utils.v2.ResultTypeUtil;
import se.inera.intyg.webcert.persistence.integreradenhet.model.SchemaVersion;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.integration.v2.builder.CreateNewDraftRequestBuilder;
import se.inera.intyg.webcert.web.integration.v2.validator.CreateDraftCertificateValidator;
import se.inera.intyg.webcert.web.integration.validator.ResultValidator;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;
import se.inera.intyg.webcert.web.service.dto.Vardgivare;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v2.*;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultType;
import se.riv.infrastructure.directory.v1.CommissionType;

@SchemaValidation
public class CreateDraftCertificateResponderImpl implements CreateDraftCertificateResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CreateDraftCertificateResponderImpl.class);

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private CreateNewDraftRequestBuilder draftRequestBuilder;

    @Autowired
    private CreateDraftCertificateValidator validator;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public CreateDraftCertificateResponseType createDraftCertificate(String logicalAddress, CreateDraftCertificateType parameters) {

        Intyg utkastsParams = parameters.getIntyg();

        // Validate draft parameters
        ResultValidator resultsValidator = validator.validate(utkastsParams);
        if (resultsValidator.hasErrors()) {
            return createValidationErrorResponse(resultsValidator);
        }

        String invokingUserHsaId = utkastsParams.getSkapadAv().getPersonalId().getExtension();
        String invokingUnitHsaId = utkastsParams.getSkapadAv().getEnhet().getEnhetsId().getExtension();

        LOG.debug("Creating draft for invoker '{}' on unit '{}'", invokingUserHsaId, invokingUnitHsaId);

        // Check if the invoking health personal has MIU rights on care unit
        CommissionType unitMIU = checkMIU(utkastsParams);
        if (unitMIU == null) {
            return createMIUErrorResponse(utkastsParams);
        }

        // Create the draft
        Utkast utkast = createNewDraft(utkastsParams, unitMIU);

        return createSuccessResponse(utkast.getIntygsId(), invokingUnitHsaId);
    }

    private Utkast createNewDraft(Intyg utkastRequest, CommissionType unitMIU) {

        String invokingUserHsaId = utkastRequest.getSkapadAv().getPersonalId().getExtension();
        String invokingUnitHsaId = utkastRequest.getSkapadAv().getEnhet().getEnhetsId().getExtension();
        LOG.debug("Creating draft for invoker '{}' on unit '{}'", invokingUserHsaId, invokingUnitHsaId);

        // Create draft request
        CreateNewDraftRequest draftRequest = draftRequestBuilder.buildCreateNewDraftRequest(utkastRequest, unitMIU);

        // Add the creating vardenhet to registry
        addVardenhetToRegistry(draftRequest);

        // Create draft and return its id
        return utkastService.createNewDraft(draftRequest);
    }

    /**
     * Method checks if invoking person, i.e the health care personal,
     * is entitled to look at the information.
     */
    private CommissionType checkMIU(Intyg utkastType) {

        String invokingUserHsaId = utkastType.getSkapadAv().getPersonalId().getExtension();
        String invokingUnitHsaId = utkastType.getSkapadAv().getEnhet().getEnhetsId().getExtension();

        List<CommissionType> miusOnUnit = hsaPersonService.checkIfPersonHasMIUsOnUnit(invokingUserHsaId, invokingUnitHsaId);

        switch (miusOnUnit.size()) {
        case 0:
            return null;
        case 1:
            return miusOnUnit.get(0);
        default:
            LOG.warn("Found more than one MIU for user '{}' on unit '{}', returning the first one", invokingUserHsaId, invokingUnitHsaId);
            return miusOnUnit.get(0);
        }
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

        String errMsg = String.format("No valid MIU was found for person %s on unit %s, can not create draft!", invokingUserHsaId, invokingUnitHsaId);
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

        Vardenhet vardenhet = utkastsRequest.getVardenhet();
        Vardgivare vardgivare = vardenhet.getVardgivare();

        IntegreradEnhetEntry integreradEnhet = new IntegreradEnhetEntry(vardenhet.getHsaId(),
                vardenhet.getNamn(), vardgivare.getHsaId(), vardgivare.getNamn());

        integreradeEnheterRegistry.putIntegreradEnhet(integreradEnhet, SchemaVersion.V2);
    }

}
