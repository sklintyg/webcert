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

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import se.inera.intyg.webcert.integration.privatepractitioner.model.GetHospInformationRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.model.HospInformation;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitioner;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitionerConfiguration;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitionerValidationRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitionerValidationResponse;
import se.inera.intyg.webcert.integration.privatepractitioner.model.PrivatePractitionerValidationResultCode;
import se.inera.intyg.webcert.integration.privatepractitioner.model.RegisterPrivatePractitionerRequest;

@Slf4j
@Service
@Profile("private-practitioner-service-active")
@RequiredArgsConstructor
public class PrivatePractitionerIntegrationService {

    private final PPSIntegrationService ppsIntegrationService;

    public PrivatePractitionerValidationResponse validatePrivatePractitioner(String personalIdentityNumber) {
        validateIdentifier(personalIdentityNumber);
        final var response = ppsIntegrationService.validatePrivatePractitioner(
            new PrivatePractitionerValidationRequest(personalIdentityNumber));

        if (response == null) {
            throw new RestClientException("Validation failed. Validation response is null.");
        }
        logResult(response);

        return response;
    }

    public PrivatePractitionerConfiguration getPrivatePractitionerConfig() {
        return ppsIntegrationService.getPrivatePractitionerConfig();
    }

    public HospInformation getHospInformation(String personalOrHsaIdIdentityNumber) {
        return ppsIntegrationService.getHospInformation(new GetHospInformationRequest(personalOrHsaIdIdentityNumber));
    }

    public PrivatePractitioner registerPrivatePractitioner(RegisterPrivatePractitionerRequest registrationRequest) {
        return ppsIntegrationService.registerPrivatePractitioner(registrationRequest);
    }

    public PrivatePractitioner getPrivatePractitioner(String personId) {
        validateIdentifier(personId);
        return ppsIntegrationService.getPrivatePractitioner(personId);
    }

    private void validateIdentifier(String personalIdentityNumber) {
        if (Strings.isNullOrEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("No PersonalIdentityNumber available.");
        }
    }

    private void logResult(PrivatePractitionerValidationResponse response) {
        if (PrivatePractitionerValidationResultCode.NO_ACCOUNT.equals(response.resultCode())
            || PrivatePractitionerValidationResultCode.NOT_AUTHORIZED_IN_HOSP.equals(response.resultCode())) {
            log.info(response.resultText());
        }
    }
}

