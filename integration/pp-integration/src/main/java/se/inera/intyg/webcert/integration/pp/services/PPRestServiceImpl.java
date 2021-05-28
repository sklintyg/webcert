/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.integration.pp.services;

import com.google.common.base.Strings;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResponse;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode;

public class PPRestServiceImpl implements PPRestService {

    private static final Logger LOG = LoggerFactory.getLogger(PPRestServiceImpl.class);

    @Value("${privatepractitioner.internalapi.validate.url}")
    private String internalApiValidateUrl;

    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {
        restTemplate = new RestTemplate();
    }

    @Override
    public ValidatePrivatePractitionerResponse validatePrivatePractitioner(String personalIdentityNumber) {
        LOG.debug("Validating person information from Privatläkarportalen.");
        validateIdentifier(personalIdentityNumber);

        return doValidatePrivatePractitioner(personalIdentityNumber);
    }

    private ValidatePrivatePractitionerResponse doValidatePrivatePractitioner(String personalIdentityNumber) {
        final var url = internalApiValidateUrl + personalIdentityNumber;
        final var response = restTemplate.getForObject(url, ValidatePrivatePractitionerResponse.class);
        if (response == null) {
            throw new RestClientException("Validation failed. Validation response is null.");
        }

        logResult(response);

        return response;
    }

    private void logResult(ValidatePrivatePractitionerResponse response) {
        if (response.getResultCode() == ValidatePrivatePractitionerResultCode.ERROR_NO_ACCOUNT
            || response.getResultCode() == ValidatePrivatePractitionerResultCode.ERROR_NOT_AUTHORIZED_IN_HOSP) {
            LOG.error(response.getResultText());
        }

        if (response.getResultCode() == ValidatePrivatePractitionerResultCode.INFO) {
            LOG.info(response.getResultText());
        }
    }

    private void validateIdentifier(String personalIdentityNumber) {
        if (Strings.isNullOrEmpty(personalIdentityNumber)) {
            throw new IllegalArgumentException("PersonalIdentityNumber är inte satt.");
        }
    }

}
