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

package se.inera.intyg.webcert.web.auth.eleg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.ROLE_PRIVATLAKARE_OBEHORIG;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_ADDRESS;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_CITY;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_FAMILY_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_FIRST_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PERSON_ID;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_ZIP_CODE;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.web.service.subscription.SubscriptionService;

@ExtendWith(MockitoExtension.class)
class UnauthorizedPrivatePractitionerServiceTest {

    @Mock
    private PUService puService;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private HashUtility hashUtility;
    @Mock
    private CommonAuthoritiesResolver authoritiesResolver;
    @InjectMocks
    private UnauthorizedPrivatePractitionerService unauthorizedPrivatePractitionerService;

    @Test
    void shouldReturnUserWithRolePrivatlakareObehorig() {
        final var expected = unauthorizedPrivatePractitioner();

        when(authoritiesResolver.getRole(ROLE_PRIVATLAKARE_OBEHORIG)).thenReturn(expected);
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.found(drKranstege()));

        final var actual = unauthorizedPrivatePractitionerService.createUnauthorizedWebCertUser(DR_KRANSTEGE_PERSON_ID);
        assertEquals(expected, actual.getRoles().values().stream().findFirst().orElse(null));
    }

    @Test
    void shouldReturnUserWithPersonId() {
        when(authoritiesResolver.getRole(ROLE_PRIVATLAKARE_OBEHORIG)).thenReturn(unauthorizedPrivatePractitioner());
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.found(drKranstege()));

        final var actual = unauthorizedPrivatePractitionerService.createUnauthorizedWebCertUser(DR_KRANSTEGE_PERSON_ID);
        assertEquals(DR_KRANSTEGE_PERSON_ID, actual.getPersonId());
    }

    @Test
    void shouldReturnUserWithFullNameWhenNoMiddleName() {
        when(authoritiesResolver.getRole(ROLE_PRIVATLAKARE_OBEHORIG)).thenReturn(unauthorizedPrivatePractitioner());
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.found(drKranstege()));

        final var actual = unauthorizedPrivatePractitionerService.createUnauthorizedWebCertUser(DR_KRANSTEGE_PERSON_ID);
        assertEquals(DR_KRANSTEGE_NAME, actual.getNamn());
    }

    @Test
    void shouldThrowExceptionIfPersonIdIsNotCorrect() {
        when(authoritiesResolver.getRole(ROLE_PRIVATLAKARE_OBEHORIG)).thenReturn(unauthorizedPrivatePractitioner());

        assertThrows(WebCertServiceException.class, () -> unauthorizedPrivatePractitionerService.createUnauthorizedWebCertUser("ABCD1234"));
    }

    @Test
    void shouldThrowExceptionIfPUNotFound() {
        when(authoritiesResolver.getRole(ROLE_PRIVATLAKARE_OBEHORIG)).thenReturn(unauthorizedPrivatePractitioner());
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.notFound());

        assertThrows(WebCertServiceException.class,
            () -> unauthorizedPrivatePractitionerService.createUnauthorizedWebCertUser(DR_KRANSTEGE_PERSON_ID)
        );
    }

    @Test
    void shouldThrowExceptionIfPUError() {
        when(authoritiesResolver.getRole(ROLE_PRIVATLAKARE_OBEHORIG)).thenReturn(unauthorizedPrivatePractitioner());
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.error());

        assertThrows(WebCertServiceException.class,
            () -> unauthorizedPrivatePractitionerService.createUnauthorizedWebCertUser(DR_KRANSTEGE_PERSON_ID)
        );
    }

    private static Person drKranstege() {
        return new Person(
            Personnummer.createPersonnummer(DR_KRANSTEGE_PERSON_ID).orElseThrow(),
            false,
            false,
            DR_KRANSTEGE_FIRST_NAME,
            null,
            DR_KRANSTEGE_FAMILY_NAME,
            DR_KRANSTEGE_ADDRESS,
            DR_KRANSTEGE_ZIP_CODE,
            DR_KRANSTEGE_CITY
        );
    }

    private static Role unauthorizedPrivatePractitioner() {
        final var expected = new Role();
        expected.setName(ROLE_PRIVATLAKARE_OBEHORIG);
        expected.setDesc("Privatläkare obehörig");
        expected.setPrivileges(List.of());
        return expected;
    }
}