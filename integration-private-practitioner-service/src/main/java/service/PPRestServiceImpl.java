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
package service;

import com.google.common.base.Strings;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import dto.ValidatePrivatePractitionerRequest;
import dto.ValidatePrivatePractitionerResponse;
import dto.ValidatePrivatePractitionerResultCode;


@Service("ppRestServiceImpl")
public class PPRestServiceImpl implements PPRestService {

  private static final Logger LOG = LoggerFactory.getLogger(PPRestServiceImpl.class);

  private RestClient ppsRestClient;

  public PPRestServiceImpl(RestClient ppsRestClient) {
    this.ppsRestClient = ppsRestClient;
  }

  @Override
  public ValidatePrivatePractitionerResponse validatePrivatePractitioner(String personalIdentityNumber) {
    validateIdentifier(personalIdentityNumber);

    return doValidatePrivatePractitioner(personalIdentityNumber);
  }

  private ValidatePrivatePractitionerResponse doValidatePrivatePractitioner(String personalIdentityNumber) {
    final var request = new ValidatePrivatePractitionerRequest(personalIdentityNumber);

    final var response = ppsRestClient.post()
        .contentType(MediaType.APPLICATION_JSON)
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
    if (response.getResultCode() == ValidatePrivatePractitionerResultCode.NO_ACCOUNT
        || response.getResultCode() == ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP) {
      LOG.info(response.getResultText());
    }
  }

  private void validateIdentifier(String personalIdentityNumber) {
    if (Strings.isNullOrEmpty(personalIdentityNumber)) {
      throw new IllegalArgumentException("No PersonalIdentityNumber available.");
    }
  }

}

