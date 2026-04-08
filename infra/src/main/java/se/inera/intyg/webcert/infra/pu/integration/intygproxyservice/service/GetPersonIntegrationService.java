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
package se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.client.GetPersonIntygProxyServiceClient;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.dto.PersonRequestDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetPersonIntegrationService {

  private final GetPersonIntygProxyServiceClient getPersonIntygProxyServiceClient;

  public PersonSvar get(Personnummer personId) {
    if (personId == null) {
      log.warn("Returning notFound since personId is null");
      return PersonSvar.notFound();
    }

    final var personResponse =
        getPersonIntygProxyServiceClient.get(
            PersonRequestDTO.builder()
                .personId(personId.getPersonnummer())
                .queryCache(true)
                .build());

    return switch (personResponse.getStatus()) {
      case FOUND -> PersonSvar.found(personResponse.getPerson());
      case NOT_FOUND -> PersonSvar.notFound();
      case ERROR -> PersonSvar.error();
    };
  }
}
