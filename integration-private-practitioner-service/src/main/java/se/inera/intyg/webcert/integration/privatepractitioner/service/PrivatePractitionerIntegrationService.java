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
package se.inera.intyg.webcert.integration.privatepractitioner.service;

import static se.inera.intyg.webcert.integration.privatepractitioner.config.PrivatePractitionerRestClientConfig.CONFIG_PATH;
import static se.inera.intyg.webcert.integration.privatepractitioner.config.PrivatePractitionerRestClientConfig.HOSP_INFO_PATH;
import static se.inera.intyg.webcert.integration.privatepractitioner.config.PrivatePractitionerRestClientConfig.VALIDATE_PATH;
import static se.inera.intyg.webcert.logging.MdcLogConstants.SESSION_ID_KEY;
import static se.inera.intyg.webcert.logging.MdcLogConstants.TRACE_ID_KEY;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import se.inera.intyg.webcert.integration.privatepractitioner.model.HospInformation;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitioner;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitionerConfig;
import se.inera.intyg.webcert.integration.privatepractitioner.model.RegisterPrivatePractitionerRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerResponse;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerResultCode;
import se.inera.intyg.webcert.logging.MdcHelper;

@Slf4j
@Service
@Profile("private-practitioner-service-active")
@RequiredArgsConstructor
public class PrivatePractitionerIntegrationService {

    private final RestClient ppsRestClient;
    private final RegisterPrivatePractitionerClient registerPrivatePractitionerClient;

    public ValidatePrivatePractitionerResponse validatePrivatePractitioner(String personalIdentityNumber) {
        validateIdentifier(personalIdentityNumber);

        return doValidatePrivatePractitioner(personalIdentityNumber);
    }

    private ValidatePrivatePractitionerResponse doValidatePrivatePractitioner(String personalIdentityNumber) {
        final var request = new ValidatePrivatePractitionerRequest(personalIdentityNumber);

        final var response = ppsRestClient
            .post()
            .uri(VALIDATE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(request)
            .retrieve()
            .body(ValidatePrivatePractitionerResponse.class);

        if (response == null) {
            throw new RestClientException("Validation failed. Validation response is null.");
        }

        logResult(response);

        return response;
    }

    private void logResult(ValidatePrivatePractitionerResponse response) {
        if (ValidatePrivatePractitionerResultCode.NO_ACCOUNT.equals(response.resultCode())
            || ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP.equals(response.resultCode())) {
            log.info(response.resultText());
        }
    }

    private void validateIdentifier(String personalIdentityNumber) {
        if (Strings.isNullOrEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("No PersonalIdentityNumber available.");
        }
    }

    public PrivatePractitionerConfig getPrivatePractitionerConfig() {
        return ppsRestClient
            .get()
            .uri(CONFIG_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(PrivatePractitionerConfig.class);
    }

    public HospInformation getHospInformation(String personalOrHsaIdIdentityNumber) {
        final var response = ppsRestClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path(HOSP_INFO_PATH)
                .build(personalOrHsaIdIdentityNumber))
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(HospInformation.class);

        if (response == null) {
            throw new RestClientException("Get HOSP Information failed. Response is null.");
        }

        return response;
    }

    public PrivatePractitioner registerPrivatePractitioner(RegisterPrivatePractitionerRequest registrationRequest) {
        return registerPrivatePractitionerClient.registerPrivatePractitioner(registrationRequest);
    }
}

