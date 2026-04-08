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
import static se.inera.intyg.webcert.web.privatepractitioner.TestData.DR_KRANSTEGE;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_FAMILY_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_FIRST_NAME;
import static se.inera.intyg.webcert.web.privatepractitioner.TestDataConstants.DR_KRANSTEGE_PERSON_ID;

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
import se.inera.intyg.webcert.integration.privatepractitioner.service.PrivatePractitionerIntegrationService;
import se.inera.intyg.webcert.logging.HashUtility;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;

@ExtendWith(MockitoExtension.class)
class AuthorizedPrivatePractitionerServiceTest extends AuthoritiesConfigurationTestSetup {

  private static final String ELEG_AUTH_SCHEME = "http://id.elegnamnden.se/loa/1.0/loa3";
  private static final AuthenticationMethod AUTH_METHOD = AuthenticationMethod.MOBILT_BANK_ID;
  private final Map<String, String> expectedPreferences = new HashMap<>();

  @Mock private CommonAuthoritiesResolver commonAuthoritiesResolver;
  @Mock private PrivatePractitionerIntegrationService privatePractitionerIntegrationService;
  @Mock private PUService puService;
  @Mock private AnvandarPreferenceRepository anvandarPreferenceRepository;
  @Mock private HashUtility hashUtility;
  @InjectMocks private AuthorizedPrivatePractitionerService authorizedPrivatePractitionerService;

  @BeforeEach
  void setupForSuccess() {
    final var request = mock(HttpServletRequest.class);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    lenient()
        .when(commonAuthoritiesResolver.getRole(ROLE_PRIVATLAKARE))
        .thenReturn(authorizedPrivatePractitioner());

    lenient()
        .when(privatePractitionerIntegrationService.getPrivatePractitioner(DR_KRANSTEGE_PERSON_ID))
        .thenReturn(DR_KRANSTEGE);
    expectedPreferences.put("some", "setting");
    lenient()
        .when(anvandarPreferenceRepository.getAnvandarPreference(anyString()))
        .thenReturn(expectedPreferences);

    lenient().when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(false));
  }

  @Test
  void testSuccessfulLogin() {
    final var user =
        authorizedPrivatePractitionerService.create(
            DR_KRANSTEGE_PERSON_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD);
    assertNotNull(user);
    assertFalse(user.isSekretessMarkerad());
    assertEquals(expectedPreferences, user.getAnvandarPreference());
  }

  @Test
  void shallSetFirstnameAndLastnameFromFullstandigtName() {
    final var user =
        authorizedPrivatePractitionerService.create(
            DR_KRANSTEGE_PERSON_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD);
    assertEquals(DR_KRANSTEGE_FIRST_NAME, user.getFornamn());
    assertEquals(DR_KRANSTEGE_FAMILY_NAME, user.getEfternamn());
  }

  @Test
  void testSuccessfulLoginSekretessMarkerad() {
    reset(puService);
    when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(true));

    final var user =
        authorizedPrivatePractitionerService.create(
            DR_KRANSTEGE_PERSON_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD);
    assertNotNull(user);
    assertTrue(user.isSekretessMarkerad());
  }

  @Test
  void testLoginPUErrorThrowsException() {
    reset(puService);
    when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.error());

    assertThrows(
        WebCertServiceException.class,
        () ->
            authorizedPrivatePractitionerService.create(
                DR_KRANSTEGE_PERSON_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD));
  }

  @Test
  void testLoginPUNotFoundThrowsException() {
    reset(puService);
    when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.notFound());

    assertThrows(
        WebCertServiceException.class,
        () ->
            authorizedPrivatePractitionerService.create(
                DR_KRANSTEGE_PERSON_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD));
  }

  @Test
  void testNotFoundInHSAThrowsException() {
    reset(privatePractitionerIntegrationService);
    when(privatePractitionerIntegrationService.getPrivatePractitioner(DR_KRANSTEGE_PERSON_ID))
        .thenReturn(null);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            authorizedPrivatePractitionerService.create(
                DR_KRANSTEGE_PERSON_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD));
  }

  @Test
  void shouldAdmitUserWhenHasSubscriptionAndAuthorizedInHosp() {
    final var webcertUser =
        authorizedPrivatePractitionerService.create(
            DR_KRANSTEGE_PERSON_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD);
    assertNotNull(webcertUser);
  }

  @Test
  void shouldAdmitUserWhenMissingSubscriptionAndAuthorizedInHosp() {
    final var webcertUser =
        authorizedPrivatePractitionerService.create(
            DR_KRANSTEGE_PERSON_ID, "origin", ELEG_AUTH_SCHEME, AUTH_METHOD);
    assertNotNull(webcertUser);
  }

  private PersonSvar buildPersonSvar(boolean sekretessMarkerad) {
    final var personnummer = Personnummer.createPersonnummer(DR_KRANSTEGE_PERSON_ID).orElseThrow();
    final var person =
        new Person(
            personnummer,
            sekretessMarkerad,
            false,
            DR_KRANSTEGE_FIRST_NAME,
            "",
            DR_KRANSTEGE_FAMILY_NAME,
            "gatan",
            "12345",
            "postort",
            false);
    return PersonSvar.found(person);
  }

  private static Role authorizedPrivatePractitioner() {
    final var expected = new Role();
    expected.setName(ROLE_PRIVATLAKARE);
    expected.setDesc("Privatläkare");
    expected.setPrivileges(List.of());
    return expected;
  }
}
