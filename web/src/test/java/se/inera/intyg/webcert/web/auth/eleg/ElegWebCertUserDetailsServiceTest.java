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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.infra.pu.integration.api.services.PUService;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.exception.HsaServiceException;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResponse;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.integration.pp.services.PPRestService;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.auth.common.AuthConstants;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;
import se.inera.intyg.webcert.web.security.WebCertUserOrigin;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.subscription.SubscriptionService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.HsaId;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.PersonId;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.VardgivareType;

@RunWith(MockitoJUnitRunner.class)
public class ElegWebCertUserDetailsServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final String HSA_ID = "191212121212";
    private static final String PERSON_ID = "197705232382";
    private static final String ELEG_AUTH_SCHEME = "http://id.elegnamnden.se/loa/1.0/loa3";
    private static final AuthenticationMethod AUTH_METHOD = AuthenticationMethod.MOBILT_BANK_ID;
    private final Map<String, String> expectedPreferences = new HashMap<>();
    @Mock
    private PPService ppService;
    @Mock
    private PPRestService ppRestService;
    @Mock
    private AvtalService avtalService;
    @Mock
    private PUService puService;
    @Mock
    private AnvandarPreferenceRepository anvandarPreferenceRepository;
    @Mock
    private ElegAuthenticationMethodResolver elegAuthenticationMethodResolver;
    @Mock
    private SubscriptionService subscriptionService;
    @InjectMocks
    private ElegWebCertUserDetailsService elegWebCertUserDetailsService;

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

    @Before
    public void setupForSuccess() {
        final var request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        elegWebCertUserDetailsService.setAuthoritiesResolver(AUTHORITIES_RESOLVER);

        when(ppService.getPrivatePractitioner(any(), any(), any())).thenReturn(buildHosPerson());
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);
        expectedPreferences.put("some", "setting");
        when(anvandarPreferenceRepository.getAnvandarPreference(anyString())).thenReturn(expectedPreferences);

        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(false));

        final var userOrigin = mock(WebCertUserOrigin.class);
        when(userOrigin.resolveOrigin(any(HttpServletRequest.class))).thenReturn("NORMAL");
        ReflectionTestUtils.setField(elegWebCertUserDetailsService, "userOrigin", Optional.of(userOrigin));
    }

    @Test
    public void shouldSetFakePropertiesWhenFakeLogin() {
        setCheckSubscriptionElegMockToReturn(false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);

        final var webcertUser = elegWebCertUserDetailsService.buildFakeUserPrincipal(PERSON_ID);
        assertEquals(AuthConstants.FAKE_AUTHENTICATION_ELEG_CONTEXT_REF, webcertUser.getAuthenticationScheme());
        assertEquals(AuthenticationMethod.FAKE, webcertUser.getAuthenticationMethod());
    }

    @Test
    public void testSuccessfulLogin() {
        final var user = elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
        assertNotNull(user);
        assertFalse(user.isSekretessMarkerad());
        assertEquals(expectedPreferences, user.getAnvandarPreference());
    }

    @Test
    public void shallSetFirstnameAndLastnameFromFullstandigtName() {
        final var user = elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
        assertEquals("Test", user.getFornamn());
        assertEquals("Testsson", user.getEfternamn());
    }

    @Test
    public void testSuccessfulLoginSekretessMarkerad() {
        reset(puService);
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(true));

        final var user = elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
        assertNotNull(user);
        assertTrue(user.isSekretessMarkerad());
    }

    @Test(expected = HsaServiceException.class)
    public void testLoginPUErrorThrowsException() {
        reset(puService);
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.error());

        elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
    }

    @Test(expected = HsaServiceException.class)
    public void testLoginPUNotFoundThrowsException() {
        reset(puService);
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.notFound());

        elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
    }

    @Test(expected = HsaServiceException.class)
    public void testNotFoundInHSAThrowsException() {
        reset(ppService);
        when(ppService.getPrivatePractitioner(any(), any(), any())).thenReturn(null);

        elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
    }

    @Test
    public void shouldAdmitUserWhenHasSubscriptionAndAuthorizedInHosp() {
        setCheckSubscriptionElegMockToReturn(true);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);

        final var webcertUser = elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
        assertNotNull(webcertUser);
        verify(subscriptionService, times(1)).checkSubscriptions(any(WebCertUser.class));
    }

    @Test
    public void shouldAdmitUserWhenMissingSubscriptionAndAuthorizedInHosp() {
        setCheckSubscriptionElegMockToReturn(false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);

        final var webcertUser = elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
        assertNotNull(webcertUser);
        verify(subscriptionService, times(1)).checkSubscriptions(any(WebCertUser.class));
    }

    @Test(expected = PrivatePractitionerAuthorizationException.class)
    public void shouldThrowAuthExceptionWhenHasSubscriptionAndNoAccount() {
        setUnauthorizedElegMissingSubscriptionMockToReturn(false); // -> User has subscription
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NO_ACCOUNT);

        elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowMissingSubscriptionExceptionWhenNoSubscriptionAndNoAccount() {
        setUnauthorizedElegMissingSubscriptionMockToReturn(true); // -> User does not have subscription
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NO_ACCOUNT);

        elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowMissingSubscriptionExceptionWhenNotNotAuthorizedInHospAndNoSubscription() {
        setCheckSubscriptionElegMockToReturn(false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP);

        elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
    }

    @Test(expected = PrivatePractitionerAuthorizationException.class)
    public void shouldThrowsPrivatePractitionerAuthExceptionWhenNotNotAuthorizedInHospAndHasSubscription() {
        setCheckSubscriptionElegMockToReturn(true);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP);

        elegWebCertUserDetailsService.buildUserPrincipal(HSA_ID, ELEG_AUTH_SCHEME, AUTH_METHOD);
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

    private void setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode resultCode) {
        final var response = new ValidatePrivatePractitionerResponse();
        response.setResultCode(resultCode);
        response.setResultText("Test result text generated by ElegWebcertUserDetailsServiceTest");
        when(ppRestService.validatePrivatePractitioner(any(String.class))).thenReturn(response);
    }

    private void setCheckSubscriptionElegMockToReturn(boolean hasSubscription) {
        when(subscriptionService.checkSubscriptions(any(WebCertUser.class))).thenReturn(hasSubscription);
    }

    private void setUnauthorizedElegMissingSubscriptionMockToReturn(boolean missingSubscription) {
        when(subscriptionService.isUnregisteredElegUserMissingSubscription(any(String.class))).thenReturn(missingSubscription);
    }
}
