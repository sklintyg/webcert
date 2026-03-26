/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.client.organization;

import static se.inera.intyg.webcert.infra.integration.intygproxyservice.configuration.HsaRestClientConfig.LOG_SESSION_ID_HEADER;
import static se.inera.intyg.webcert.infra.integration.intygproxyservice.configuration.HsaRestClientConfig.LOG_TRACE_ID_HEADER;
import static se.inera.intyg.webcert.infra.integration.intygproxyservice.configuration.HsaRestClientConfig.SESSION_ID_KEY;
import static se.inera.intyg.webcert.infra.integration.intygproxyservice.configuration.HsaRestClientConfig.TRACE_ID_KEY;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitMembersRequestDTO;
import se.inera.intyg.webcert.infra.integration.intygproxyservice.dto.organization.GetHealthCareUnitMembersResponseDTO;

@Service
public class HsaIntygProxyServiceHealthCareUnitMembersClient {

  @Autowired
  @Qualifier("hsaIntygProxyServiceRestClient") private RestClient ipsRestClient;

  @Value("${integration.intygproxyservice.healthcareunitmembers.endpoint}")
  private String healthCareUnitMembersEndpoint;

  public GetHealthCareUnitMembersResponseDTO get(
      GetHealthCareUnitMembersRequestDTO getHealthCareUnitMembersRequestDTO) {
    return ipsRestClient
        .post()
        .uri(healthCareUnitMembersEndpoint)
        .body(getHealthCareUnitMembersRequestDTO)
        .header(LOG_TRACE_ID_HEADER, MDC.get(TRACE_ID_KEY))
        .header(LOG_SESSION_ID_HEADER, MDC.get(SESSION_ID_KEY))
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(GetHealthCareUnitMembersResponseDTO.class);
  }
}
