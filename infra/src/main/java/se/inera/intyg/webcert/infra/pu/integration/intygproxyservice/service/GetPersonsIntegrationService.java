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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.client.GetPersonsIntygProxyServiceClient;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.dto.PersonResponseDTO;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.dto.PersonsRequestDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetPersonsIntegrationService {

  private final GetPersonsIntygProxyServiceClient getPersonIntygProxyServiceClient;

  public Map<Personnummer, PersonSvar> get(List<Personnummer> personIds) {
    if (personIds == null || personIds.isEmpty()) {
      log.warn("Returning empty map since personIds is null or empty '{}'", personIds);
      return Collections.emptyMap();
    }

    final var personsResponse =
        getPersonIntygProxyServiceClient.get(
            PersonsRequestDTO.builder()
                .personIds(personIds.stream().map(Personnummer::getPersonnummer).toList())
                .queryCache(true)
                .build());

    return personsResponse.getPersons().stream()
        .collect(
            Collectors.toMap(
                personResponse -> personResponse.getPerson().personnummer(),
                this::mapToPersonResponse));
  }

  private PersonSvar mapToPersonResponse(PersonResponseDTO response) {
    return switch (response.getStatus()) {
      case FOUND -> PersonSvar.found(response.getPerson());
      case NOT_FOUND -> PersonSvar.notFound();
      case ERROR -> PersonSvar.error();
    };
  }
}
