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

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.GetHospInformationRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.HospInformation;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitioner;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerConfiguration;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerDetailsRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerValidationRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.dto.PrivatePractitionerValidationResponse;
import se.inera.intyg.webcert.logging.MdcHelper;

@Service
@RequiredArgsConstructor
@Profile("private-practitioner-service-active")
public class PPSIntegrationService {

    private final RestClient ppsRestClient;

    public HospInformation getHospInformation(GetHospInformationRequest personalOrHsaIdIdentityNumber) {
        return ppsRestClient
            .post()
            .uri(HOSP_INFO_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(personalOrHsaIdIdentityNumber)
            .retrieve()
            .body(HospInformation.class);
    }

    public PrivatePractitionerConfiguration getPrivatePractitionerConfig() {
        return ppsRestClient
            .get()
            .uri(CONFIG_PATH)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(PrivatePractitionerConfiguration.class);
    }

    public PrivatePractitioner registerPrivatePractitioner(PrivatePractitionerDetailsRequest registrationRequest) {
        return ppsRestClient
            .post()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(registrationRequest)
            .retrieve()
            .body(PrivatePractitioner.class);
    }

    public PrivatePractitionerValidationResponse validatePrivatePractitioner(
        PrivatePractitionerValidationRequest privatePractitionerValidationRequest) {
        return ppsRestClient
            .post()
            .uri(VALIDATE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .body(privatePractitionerValidationRequest)
            .retrieve()
            .body(PrivatePractitionerValidationResponse.class);
    }

    public PrivatePractitioner getPrivatePractitioner(String personId) {
        return ppsRestClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("personOrHsaId", personId)
                .build()
            )
            .accept(MediaType.APPLICATION_JSON)
            .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
            .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
            .retrieve()
            .body(PrivatePractitioner.class);
    }
}
