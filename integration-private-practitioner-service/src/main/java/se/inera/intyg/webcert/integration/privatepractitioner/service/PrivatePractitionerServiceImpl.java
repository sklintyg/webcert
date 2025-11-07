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

import static se.inera.intyg.webcert.logging.MdcLogConstants.SESSION_ID_KEY;
import static se.inera.intyg.webcert.logging.MdcLogConstants.TRACE_ID_KEY;

import com.google.common.base.Strings;
import se.inera.intyg.webcert.integration.privatepractitioner.model.GetPrivatePractitionerResponseDTO;
import se.inera.intyg.webcert.integration.privatepractitioner.model.HoSPersonDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerRequest;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerResponse;
import se.inera.intyg.webcert.integration.privatepractitioner.model.ValidatePrivatePractitionerResultCode;
import se.inera.intyg.webcert.logging.MdcHelper;

@Profile("private-practitioner-service-active")
@Service("privatePractitionerService")
public class PrivatePractitionerServiceImpl implements PrivatePractitionerService {

  private static final Logger LOG = LoggerFactory.getLogger(PrivatePractitionerServiceImpl.class);

  private RestClient ppsRestClient;

  public PrivatePractitionerServiceImpl(RestClient ppsRestClient) {
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

  @Override
  public HoSPersonDTO getPrivatePractitioner(String personalOrHsaIdIdentityNumber) {

    final var response = ppsRestClient.get()
        .uri("") //FIXME: add endpoint
        .accept(MediaType.APPLICATION_JSON)
        .header(MdcHelper.LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
        .header(MdcHelper.LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
        .retrieve()
        .body(GetPrivatePractitionerResponseDTO.class);

    if (response == null) {
      throw new RestClientException("Get Private Practitioner failed. Response is null.");
    }

    return response.getHoSPerson();
  }
}

