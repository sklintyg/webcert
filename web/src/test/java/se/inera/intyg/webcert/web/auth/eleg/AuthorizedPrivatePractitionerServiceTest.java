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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static se.inera.intyg.infra.security.common.model.AuthoritiesConstants.ROLE_PRIVATLAKARE;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.HsaId;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.PersonId;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.VardgivareType;

@ExtendWith(MockitoExtension.class)
class AuthorizedPrivatePractitionerServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final String HSA_ID = "191212121212";
    private static final String PERSON_ID = "197705232382";
    private static final String ELEG_AUTH_SCHEME = "http://id.elegnamnden.se/loa/1.0/loa3";
    private static final AuthenticationMethod AUTH_METHOD = AuthenticationMethod.MOBILT_BANK_ID;
    private final Map<String, String> expectedPreferences = new HashMap<>();

    @Mock
    private CommonAuthoritiesResolver commonAuthoritiesResolver;
    @Mock
    private PPService ppService;
    @Mock
    private PUService puService;
    @Mock
    private AnvandarPreferenceRepository anvandarPreferenceRepository;
    @Mock
    private HashUtility hashUtility;
    @InjectMocks
    private AuthorizedPrivatePractitionerService authorizedPrivatePractitionerService;

    @BeforeEach
    void setupForSuccess() {
        final var request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        lenient().when(commonAuthoritiesResolver.getRole(ROLE_PRIVATLAKARE)).thenReturn(authorizedPrivatePractitioner());

        lenient().when(ppService.getPrivatePractitioner(any(), any(), any())).thenReturn(buildHosPerson());
        expectedPreferences.put("some", "setting");
        lenient().when(anvandarPreferenceRepository.getAnvandarPreference(anyString())).thenReturn(expectedPreferences);

        lenient().when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(false));
    }

    @Test
    void testSuccessfulLogin() {
        final var user = authorizedPrivatePractitionerService.createAuthorizedPrivatePractitioner(HSA_ID, "origin", ELEG_AUTH_SCHEME,
            AUTH_METHOD);
        assertNotNull(user);
        assertFalse(user.isSekretessMarkerad());
        assertEquals(expectedPreferences, user.getAnvandarPreference());
    }

    @Test
    void shallSetFirstnameAndLastnameFromFullstandigtName() {
        final var user = authorizedPrivatePractitionerService.createAuthorizedPrivatePractitioner(HSA_ID, "origin", ELEG_AUTH_SCHEME,
            AUTH_METHOD);
        assertEquals("Test", user.getFornamn());
        assertEquals("Testsson", user.getEfternamn());
    }

    @Test
    void testSuccessfulLoginSekretessMarkerad() {
        reset(puService);
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(true));

        final var user = authorizedPrivatePractitionerService.createAuthorizedPrivatePractitioner(HSA_ID, "origin", ELEG_AUTH_SCHEME,
            AUTH_METHOD);
        assertNotNull(user);
        assertTrue(user.isSekretessMarkerad());
    }

    @Test
    void testLoginPUErrorThrowsException() {
        reset(puService);
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.error());

        assertThrows(WebCertServiceException.class,
            () -> authorizedPrivatePractitionerService.createAuthorizedPrivatePractitioner(HSA_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD)
        );
    }

    @Test
    void testLoginPUNotFoundThrowsException() {
        reset(puService);
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.notFound());

        assertThrows(WebCertServiceException.class,
            () -> authorizedPrivatePractitionerService.createAuthorizedPrivatePractitioner(HSA_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD)
        );
    }

    @Test
    void testNotFoundInHSAThrowsException() {
        reset(ppService);
        when(ppService.getPrivatePractitioner(any(), any(), any())).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
            () -> authorizedPrivatePractitionerService.createAuthorizedPrivatePractitioner(HSA_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD)
        );
    }

    @Test
    void shouldAdmitUserWhenHasSubscriptionAndAuthorizedInHosp() {
        final var webcertUser = authorizedPrivatePractitionerService.createAuthorizedPrivatePractitioner(HSA_ID, "origin", ELEG_AUTH_SCHEME,
            AUTH_METHOD);
        assertNotNull(webcertUser);
    }

    @Test
    void shouldAdmitUserWhenMissingSubscriptionAndAuthorizedInHosp() {
        final var webcertUser = authorizedPrivatePractitionerService.createAuthorizedPrivatePractitioner(HSA_ID, "origin", ELEG_AUTH_SCHEME,
            AUTH_METHOD);
        assertNotNull(webcertUser);
    }

    private HoSPersonType buildHosPerson() {
        final var hoSPersonType = new HoSPersonType();
        final var hsaId = new HsaId();
        hsaId.setExtension(HSA_ID);
        hoSPersonType.setHsaId(hsaId);
        final var personId = new PersonId();
        personId.setExtension(PERSON_ID);
        hoSPersonType.setPersonId(personId);
        hoSPersonType.setFullstandigtNamn("Test Testsson");

        final var vardEnhet = getEnhetType();
        hoSPersonType.setEnhet(vardEnhet);

        return hoSPersonType;
    }

    private PersonSvar buildPersonSvar(boolean sekretessMarkerad) {
        final var personnummer = Personnummer.createPersonnummer(PERSON_ID).orElseThrow();
        final var person = new Person(personnummer, sekretessMarkerad, false, "fornamn", "",
            "Efternamn", "gatan", "12345", "postort", false);
        return PersonSvar.found(person);
    }

    private static Role authorizedPrivatePractitioner() {
        final var expected = new Role();
        expected.setName(ROLE_PRIVATLAKARE);
        expected.setDesc("Privatläkare");
        expected.setPrivileges(List.of());
        return expected;
    }

    private static EnhetType getEnhetType() {
        final var vardEnhet = new EnhetType();
        vardEnhet.setEnhetsnamn("enhetsNamn");
        final var enhetsId = new HsaId();
        enhetsId.setExtension("enhetsId");
        vardEnhet.setEnhetsId(enhetsId);
        final var vardgivare = new VardgivareType();
        final var vardgivareId = new HsaId();
        enhetsId.setExtension("vardgivareId");
        vardgivare.setVardgivareId(vardgivareId);
        vardgivare.setVardgivarenamn("vardgivareName");
        vardEnhet.setVardgivare(vardgivare);
        return vardEnhet;
    }
}
