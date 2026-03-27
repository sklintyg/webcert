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
package se.inera.intyg.webcert.infra.integration.intygproxyservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.infra.pu.integration.api.model.Person;
import se.inera.intyg.webcert.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.webcert.infra.pu.integration.api.model.PersonSvar.Status;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.client.GetPersonsIntygProxyServiceClient;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.dto.PersonResponseDTO;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.dto.PersonsRequestDTO;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.dto.PersonsResponseDTO;
import se.inera.intyg.webcert.infra.pu.integration.intygproxyservice.service.GetPersonsIntegrationService;

@ExtendWith(MockitoExtension.class)
class GetPersonsIntegrationServiceTest {

  private static final String PERSON_ID_1 = "191212121212";
  private static final String PERSON_ID_2 = "191212121213";
  private static final String PERSON_ID_3 = "191212121214";
  @Mock GetPersonsIntygProxyServiceClient getPersonsIntygProxyServiceClient;
  @InjectMocks GetPersonsIntegrationService getPersonsIntegrationService;

  @Test
  void shallReturnEmptyMapIfPersonIdsIsNull() {
    final var result = getPersonsIntegrationService.get(null);
    assertTrue(result.isEmpty());
  }

  @Test
  void shallReturnEmptyMapIfPersonIdsIsEmpty() {
    final var result = getPersonsIntegrationService.get(Collections.emptyList());
    assertTrue(result.isEmpty());
  }

  @Test
  void shallReturnMapWithPersonSvarFound() {
    final var person1 = mock(Person.class);
    final var person2 = mock(Person.class);
    final var person3 = mock(Person.class);

    final var personalIdentityNumber1 = Personnummer.createPersonnummer(PERSON_ID_1).orElseThrow();
    final var personalIdentityNumber2 = Personnummer.createPersonnummer(PERSON_ID_2).orElseThrow();
    final var personalIdentityNumber3 = Personnummer.createPersonnummer(PERSON_ID_3).orElseThrow();

    final var expectedResult =
        Map.of(
            personalIdentityNumber1, PersonSvar.found(person1),
            personalIdentityNumber2, PersonSvar.found(person2),
            personalIdentityNumber3, PersonSvar.found(person3));

    final var personIds =
        List.of(personalIdentityNumber1, personalIdentityNumber2, personalIdentityNumber3);

    doReturn(personalIdentityNumber1).when(person1).personnummer();
    doReturn(personalIdentityNumber2).when(person2).personnummer();
    doReturn(personalIdentityNumber3).when(person3).personnummer();

    final var personsResponse =
        PersonsResponseDTO.builder()
            .persons(
                List.of(
                    PersonResponseDTO.builder().person(person1).status(Status.FOUND).build(),
                    PersonResponseDTO.builder().person(person2).status(Status.FOUND).build(),
                    PersonResponseDTO.builder().person(person3).status(Status.FOUND).build()))
            .build();

    doReturn(personsResponse)
        .when(getPersonsIntygProxyServiceClient)
        .get(
            PersonsRequestDTO.builder()
                .personIds(List.of(PERSON_ID_1, PERSON_ID_2, PERSON_ID_3))
                .queryCache(true)
                .build());

    final var actualResponse = getPersonsIntegrationService.get(personIds);
    assertEquals(expectedResult, actualResponse);
  }

  @Test
  void shallReturnMapWithPersonSvarFoundNotFoundAndError() {
    final var person1 = mock(Person.class);
    final var person2 = mock(Person.class);
    final var person3 = mock(Person.class);

    final var personalIdentityNumber1 = Personnummer.createPersonnummer(PERSON_ID_1).orElseThrow();
    final var personalIdentityNumber2 = Personnummer.createPersonnummer(PERSON_ID_2).orElseThrow();
    final var personalIdentityNumber3 = Personnummer.createPersonnummer(PERSON_ID_3).orElseThrow();

    final var expectedResult =
        Map.of(
            personalIdentityNumber1, PersonSvar.found(person1),
            personalIdentityNumber2, PersonSvar.notFound(),
            personalIdentityNumber3, PersonSvar.error());

    final var personIds =
        List.of(personalIdentityNumber1, personalIdentityNumber2, personalIdentityNumber3);

    doReturn(personalIdentityNumber1).when(person1).personnummer();
    doReturn(personalIdentityNumber2).when(person2).personnummer();
    doReturn(personalIdentityNumber3).when(person3).personnummer();

    final var personsResponse =
        PersonsResponseDTO.builder()
            .persons(
                List.of(
                    PersonResponseDTO.builder().person(person1).status(Status.FOUND).build(),
                    PersonResponseDTO.builder().person(person2).status(Status.NOT_FOUND).build(),
                    PersonResponseDTO.builder().person(person3).status(Status.ERROR).build()))
            .build();

    doReturn(personsResponse)
        .when(getPersonsIntygProxyServiceClient)
        .get(
            PersonsRequestDTO.builder()
                .personIds(List.of(PERSON_ID_1, PERSON_ID_2, PERSON_ID_3))
                .queryCache(true)
                .build());

    final var actualResponse = getPersonsIntegrationService.get(personIds);
    assertEquals(expectedResult, actualResponse);
  }
}
