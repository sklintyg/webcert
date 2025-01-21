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

package se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3;

import static se.inera.intyg.webcert.web.integration.interactions.createdraftcertificate.v3.CreateDraftCertificateResponseFactory.createErrorResponse;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.dto.ValidateDraftCreationResponse;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.tak.service.TakService;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.integration.util.AuthoritiesHelperUtil;
import se.inera.intyg.webcert.web.integration.validators.ResultValidator;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@Slf4j
@Service
public class CreateDraftCertificateFromWC implements CreateDraftCertificate {

    @Autowired
    private IntygModuleRegistry moduleRegistry;
    @Autowired
    private IntygTextsService intygTextsService;
    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
    @Autowired
    private UtkastService utkastService;
    @Autowired
    private CreateNewDraftRequestBuilder draftRequestBuilder;
    @Lazy
    @Autowired
    private TakService takService;
    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;
    @Autowired
    private CreateDraftCertificateValidator validator;

    @Override
    public CreateDraftCertificateResponseType create(Intyg certificate, IntygUser user) {
        final var resultValidator = validator.validateCertificateErrors(certificate, user);
        if (resultValidator.hasErrors()) {
            return createApplicationErrorResponse(resultValidator);
        }
        final var intygsTyp = moduleRegistry.getModuleIdFromExternalId(certificate.getTypAvIntyg().getCode());
        // Default to use latest version, since there is no info in request specifying version
        final var latestIntygTypeVersion = intygTextsService.getLatestVersion(intygsTyp);

        final var personnummer = Personnummer.createPersonnummer(
                certificate.getPatient().getPersonId().getExtension())
            .orElseThrow(() -> new WebCertServiceException(WebCertServiceErrorCodeEnum.PU_PROBLEM,
                "Failed to create valid personnummer for createDraft request"));

        ModuleApi moduleApi = null;
        try {
            moduleApi = moduleRegistry.getModuleApi(intygsTyp, latestIntygTypeVersion);
        } catch (ModuleNotFoundException e) {
            log.error("Could not get module api", e);
            return createErrorResponse("Internal error. Could not get module api.", ErrorIdType.APPLICATION_ERROR);
        }

        final var intygstypToPreviousIntyg =
            utkastService.checkIfPersonHasExistingIntyg(personnummer, user, null);
        final var validateDraftCreationResponse =
            AuthoritiesHelperUtil.performUniqueAndModuleValidation(user, intygsTyp, intygstypToPreviousIntyg,
                moduleApi);

        if (validateDraftCreationResponse != null
            && validateDraftCreationResponse.getResultCode() == ResultCodeType.ERROR) {
            return createErrorResponse(validateDraftCreationResponse.getMessage(), ErrorIdType.APPLICATION_ERROR);
        }

        final var invokingUnitHsaId = certificate.getSkapadAv().getEnhet().getEnhetsId().getExtension();

        if (authoritiesValidator.given(user, intygsTyp).features(AuthoritiesConstants.FEATURE_TAK_KONTROLL).isVerified()) {
            // Check if invoking health care unit has required TAK
            final var takResult = takService.verifyTakningForCareUnit(invokingUnitHsaId, intygsTyp, SchemaVersion.VERSION_3, user);
            if (!takResult.isValid()) {
                final var error = Joiner.on("; ").join(takResult.getErrorMessages());
                log.warn("Invalid TAK result for unit '{}'. Returning APPLICATION_ERROR: {}", invokingUnitHsaId, error);
                return createErrorResponse(error, ErrorIdType.APPLICATION_ERROR);
            }
        }
        final var utkast = createNewDraft(certificate, latestIntygTypeVersion, user);
        return createSuccessResponse(utkast.getIntygsId(), invokingUnitHsaId, validateDraftCreationResponse);
    }

    private CreateDraftCertificateResponseType createApplicationErrorResponse(ResultValidator resultsValidator) {
        final var errMsgs = resultsValidator.getErrorMessagesAsString();
        log.warn("Intyg did not pass APPLICATION_ERROR check correctly: {}", errMsgs);
        return createErrorResponse(errMsgs, ErrorIdType.APPLICATION_ERROR);
    }

    private Utkast createNewDraft(Intyg utkastRequest, String latestIntygTypeVersion, IntygUser user) {
        log.debug("Creating draft for invoker '{}' on unit '{}'", utkastRequest.getSkapadAv().getPersonalId().getExtension(),
            utkastRequest.getSkapadAv().getEnhet().getEnhetsId().getExtension());
        // Create draft request
        final var draftRequest = draftRequestBuilder.buildCreateNewDraftRequest(utkastRequest, latestIntygTypeVersion, user);
        // Add the creating vardenhet to registry
        addVardenhetToRegistry(draftRequest);
        return utkastService.createNewDraft(draftRequest);
    }

    private void addVardenhetToRegistry(CreateNewDraftRequest utkastsRequest) {
        final var vardenhet = utkastsRequest.getHosPerson().getVardenhet();
        final var vardgivare = vardenhet.getVardgivare();
        final var integreradEnhet = new IntegreradEnhetEntry(vardenhet.getEnhetsid(),
            vardenhet.getEnhetsnamn(), vardgivare.getVardgivarid(), vardgivare.getVardgivarnamn());

        integreradeEnheterRegistry.putIntegreradEnhet(integreradEnhet, false, true);
    }

    private CreateDraftCertificateResponseType createSuccessResponse(String nyttUtkastsId, String invokingUnitHsaId,
        ValidateDraftCreationResponse validateDraftCreationResponse) {
        ResultType result = null;
        if (validateDraftCreationResponse != null
            && validateDraftCreationResponse.getResultCode() == ResultCodeType.INFO) {
            result = ResultTypeUtil.infoResult(validateDraftCreationResponse.getMessage());
        } else {
            result = ResultTypeUtil.okResult();
        }

        final var intygId = new IntygId();
        intygId.setRoot(invokingUnitHsaId);
        intygId.setExtension(nyttUtkastsId);

        final var response = new CreateDraftCertificateResponseType();
        response.setResult(result);
        response.setIntygsId(intygId);
        return response;
    }
}
