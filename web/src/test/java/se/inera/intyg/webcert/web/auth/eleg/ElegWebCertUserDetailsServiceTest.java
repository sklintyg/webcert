/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml2.core.NameID;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.infra.integration.pu.model.Person;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.infra.integration.pu.services.PUService;
import se.inera.intyg.infra.security.exception.HsaServiceException;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResponse;
import se.inera.intyg.privatepractitioner.dto.ValidatePrivatePractitionerResultCode;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.integration.pp.services.PPRestService;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.auth.common.BaseSAMLCredentialTest;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingSubscriptionException;
import se.inera.intyg.webcert.web.security.WebCertUserOrigin;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.subscription.SubscriptionService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.HsaId;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.PersonId;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.VardgivareType;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_SAVED_REQUEST_KEY;

/**
 * Created by eriklupander on 2015-06-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElegWebCertUserDetailsServiceTest extends BaseSAMLCredentialTest {

    private static final String LOCAL_ENTITY_ID = "localEntityId";
    private static final String REMOTE_ENTITY_ID = "remoteEntityId";
    private static final String HSA_ID = "191212121212";
    private static final String PERSON_ID = "197705232382";

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
    private ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;
    @Mock
    private ElegAuthenticationMethodResolver elegAuthenticationMethodResolver;
    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private ElegWebCertUserDetailsService testee;
    private final Map<String, String> expectedPreferences = new HashMap<>();

    @BeforeClass
    public static void readSamlAssertions() throws Exception {
        bootstrapSamlAssertions();
    }

    @Before
    public void setupForSuccess() {
        // Setup a servlet request
        final var request = mockHttpServletRequest("/any/path");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        testee.setAuthoritiesResolver(AUTHORITIES_RESOLVER);

        when(ppService.getPrivatePractitioner(any(), any(), any())).thenReturn(buildHosPerson());
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);
        setAvtalServiceMockToReturn(true);
        expectedPreferences.put("some", "setting");
        when(anvandarPreferenceRepository.getAnvandarPreference(anyString())).thenReturn(expectedPreferences);

        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(false));
        when(elegAuthenticationAttributeHelper.getAttribute(any(), any())).thenReturn(PERSON_ID);

        WebCertUserOrigin userOrigin = mock(WebCertUserOrigin.class);
        when(userOrigin.resolveOrigin(any(HttpServletRequest.class))).thenReturn("NORMAL");
        ReflectionTestUtils.setField(testee, "userOrigin", Optional.of(userOrigin));
    }

    @Test
    public void testSuccessfulLogin() {
        WebCertUser user = (WebCertUser) testee
            .loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));

        assertNotNull(user);
        assertFalse(user.isSekretessMarkerad());
        assertEquals(expectedPreferences, user.getAnvandarPreference());

        // WEBCERT-2028
        verify(avtalService, times(1)).userHasApprovedLatestAvtal(anyString());
    }

    @Test
    public void testSuccessfulLoginSekretessMarkerad() {
        reset(puService);
        when(puService.getPerson(any(Personnummer.class))).thenReturn(buildPersonSvar(true));

        WebCertUser user = (WebCertUser) testee
            .loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));

        assertNotNull(user);
        assertTrue(user.isSekretessMarkerad());

        // WEBCERT-2028
        verify(avtalService, times(1)).userHasApprovedLatestAvtal(anyString());
    }

    @Test(expected = HsaServiceException.class)
    public void testLoginPUErrorThrowsException() {
        reset(puService);
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.error());

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = HsaServiceException.class)
    public void testLoginPUNotFoundThrowsException() {
        reset(puService);
        when(puService.getPerson(any(Personnummer.class))).thenReturn(PersonSvar.notFound());

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = PrivatePractitionerAuthorizationException.class)
    public void testNotValidPrivatePractitionerThrowsException() {
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = HsaServiceException.class)
    public void testNotFoundInHSAThrowsException() {
        reset(ppService);
        when(ppService.getPrivatePractitioner(any(), any(), any())).thenReturn(null);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test
    public void shouldNotCheckSubscriptionsWhenNoSubscriptionFeaturesActive() {

        setSubscriptionFeaturesMocksToReturn(false, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);

        final var webcertUser = (WebCertUser) testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare,
            REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));

        assertNotNull(webcertUser);
        verify(subscriptionService, times(0)).isUnregisteredElegUserMissingSubscription(any(String.class));
        verify(subscriptionService, times(0)).checkSubscriptions(any(WebCertUser.class));
    }

    @Test(expected = PrivatePractitionerAuthorizationException.class)
    public void shouldThrowAuthExceptionWhenNoPPAccountAndNoSubscriptionFeatures() {
        setSubscriptionFeaturesMocksToReturn(false, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NO_ACCOUNT);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = PrivatePractitionerAuthorizationException.class)
    public void shouldThrowAuthExceptionWhenNoHospAndNoSubscriptionFeatures() {
        setSubscriptionFeaturesMocksToReturn(false, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test
    public void shouldAdmitUserWhenAdaptationAndHasSubscriptionAndAuthorized() {
        setAvtalServiceMockToReturn(false);
        setCheckSubscriptionElegMockToReturn(true);
        setSubscriptionFeaturesMocksToReturn(true, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);

        final var webcertUser = (WebCertUser) testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare,
            REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));

        assertNotNull(webcertUser);
        verify(subscriptionService, times(1)).checkSubscriptions(any(WebCertUser.class));
    }

    @Test
    public void shouldAdmitUserWhenAdaptationAndApprovedTermsAndAuthorized() {
        setAvtalServiceMockToReturn(true);
        setCheckSubscriptionElegMockToReturn(false);
        setSubscriptionFeaturesMocksToReturn(true, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);

        final var webcertUser = (WebCertUser) testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare,
            REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));

        assertNotNull(webcertUser);
        verify(subscriptionService, times(1)).checkSubscriptions(any(WebCertUser.class));
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowMissingSubscriptionExceptionIfAdaptationAndNoSubscriptionAndNotApprovedTerms() {
        setAvtalServiceMockToReturn(false);
        setCheckSubscriptionElegMockToReturn(false);
        setSubscriptionFeaturesMocksToReturn(true, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowMissingSubscriptionExceptionWhenAdaptationAndNoHospAndNoSubscriptionAndNotApprovedTerms() {
        setAvtalServiceMockToReturn(false);
        setCheckSubscriptionElegMockToReturn(false);
        setSubscriptionFeaturesMocksToReturn(true, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = PrivatePractitionerAuthorizationException.class)
    public void shouldThrowAuthExceptionWhenAdaptationAndNoHospAndHasSubscriptionAndNotApprovedTerms() {
        setAvtalServiceMockToReturn(false);
        setCheckSubscriptionElegMockToReturn(true);
        setSubscriptionFeaturesMocksToReturn(true, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = PrivatePractitionerAuthorizationException.class)
    public void shouldThrowAuthExceptionWhenAdaptationAndNoHospAndNoSubscriptionAndApprovedTerms() {
        setAvtalServiceMockToReturn(true);
        setCheckSubscriptionElegMockToReturn(false);
        setSubscriptionFeaturesMocksToReturn(true, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = PrivatePractitionerAuthorizationException.class)
    public void shouldThrowAuthExceptionWhenAdaptationAndNoAccountAndHasSubscription() {
        setUnauthorizedElegMissingSubscriptionMockToReturn(false); // -> User has subscription
        setSubscriptionFeaturesMocksToReturn(true, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NO_ACCOUNT);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowMissingSubscriptionExceptionWhenAdaptationAndNoAccountAndNoSubscription() {
        setUnauthorizedElegMissingSubscriptionMockToReturn(true); // -> User does not have subscription
        setSubscriptionFeaturesMocksToReturn(true, false);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NO_ACCOUNT);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test
    public void shouldAdmitUserWhenSubscriptionRequiredAndHasSubscriptionAndAuthorized() {
        setCheckSubscriptionElegMockToReturn(true);
        setSubscriptionFeaturesMocksToReturn(false, true);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);

        final var webcertUser = (WebCertUser) testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare,
            REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));

        assertNotNull(webcertUser);
        verify(subscriptionService, times(1)).checkSubscriptions(any(WebCertUser.class));
    }

    @Test
    public void shouldAdmitUserWhenMissingSubscriptionExceptionWhenSubscriptionRequiredAndNoSubscriptionAndAuthorized() {
        setCheckSubscriptionElegMockToReturn(false);
        setSubscriptionFeaturesMocksToReturn(false, true);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.OK);

        final var webcertUser = testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare,
            REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));

        assertNotNull(webcertUser);
        verify(subscriptionService, times(1)).checkSubscriptions(any(WebCertUser.class));
    }

    @Test(expected = PrivatePractitionerAuthorizationException.class)
    public void shouldThrowAuthExceptionWhenSubscriptionRequiredAndHasSubscriptionAndNoAccount() {
        setUnauthorizedElegMissingSubscriptionMockToReturn(false); // -> User has subscription
        setSubscriptionFeaturesMocksToReturn(false, true);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NO_ACCOUNT);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowMissingSubscriptionExceptionWhenSubscriptionRequiredAndNoSubscriptionAndNoAccount() {
        setUnauthorizedElegMissingSubscriptionMockToReturn(true); // -> User does not have subscription
        setSubscriptionFeaturesMocksToReturn(false, true);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NO_ACCOUNT);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = PrivatePractitionerAuthorizationException.class)
    public void shouldThrowAuthExceptionWhenSubscriptionRequiredAndHasSubscriptionAndNoHosp() {
        setCheckSubscriptionElegMockToReturn(true);
        setSubscriptionFeaturesMocksToReturn(false, true);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test(expected = MissingSubscriptionException.class)
    public void shouldThrowMissingSubscriptionExceptionWhenSubscriptionRequiredAndNoSubscriptionAndNoHosp() {
        setCheckSubscriptionElegMockToReturn(false);
        setSubscriptionFeaturesMocksToReturn(false, true);
        setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode.NOT_AUTHORIZED_IN_HOSP);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    private HoSPersonType buildHosPerson() {
        HoSPersonType hoSPersonType = new HoSPersonType();
        HsaId hsaId = new HsaId();
        hsaId.setExtension(HSA_ID);
        hoSPersonType.setHsaId(hsaId);
        PersonId personId = new PersonId();
        personId.setExtension(PERSON_ID);
        hoSPersonType.setPersonId(personId);

        EnhetType vardEnhet = new EnhetType();
        vardEnhet.setEnhetsnamn("enhetsNamn");
        HsaId enhetsId = new HsaId();
        enhetsId.setExtension("enhetsId");
        vardEnhet.setEnhetsId(enhetsId);
        VardgivareType vardgivare = new VardgivareType();
        HsaId vardgivareId = new HsaId();
        enhetsId.setExtension("vardgivareId");
        vardgivare.setVardgivareId(vardgivareId);
        vardgivare.setVardgivarenamn("vardgivareName");
        vardEnhet.setVardgivare(vardgivare);
        hoSPersonType.setEnhet(vardEnhet);

        return hoSPersonType;
    }

    private PersonSvar buildPersonSvar(boolean sekretessMarkerad) {
        Personnummer personnummer = Personnummer.createPersonnummer(PERSON_ID).get();
        Person person = new Person(personnummer, sekretessMarkerad, false, "fornamn", "",
            "Efternamn", "gatan", "12345", "postort");
        return PersonSvar.found(person);
    }

    private MockHttpServletRequest mockHttpServletRequest(String requestURI) {
        MockHttpServletRequest request = new MockHttpServletRequest();

        if ((requestURI != null) && (requestURI.length() > 0)) {
            request.setRequestURI(requestURI);
        }

        SavedRequest savedRequest = new DefaultSavedRequest(request, new PortResolverImpl());
        request.getSession().setAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY, savedRequest);

        return request;
    }

    private void setSubscriptionFeaturesMocksToReturn(boolean adaptation, boolean required) {
        when(subscriptionService.isAnySubscriptionFeatureActive()).thenReturn(adaptation || required);

        if (adaptation || required) {
            when(subscriptionService.isSubscriptionAdaptation()).thenReturn(!required);
            when(subscriptionService.isSubscriptionRequired()).thenReturn(required);
        }
    }

    private void setPPRestServiceMockToReturn(ValidatePrivatePractitionerResultCode resultCode) {
        final var response = new ValidatePrivatePractitionerResponse();
        response.setResultCode(resultCode);
        response.setResultText("Test result text generated by ElegWebcertUserDetailsServiceTest");
        when(ppRestService.validatePrivatePractitioner(any(String.class))).thenReturn(response);
    }

    private void setAvtalServiceMockToReturn(boolean approvedTerms) {
        when(avtalService.userHasApprovedLatestAvtal(any(String.class))).thenReturn(approvedTerms);
    }

    private void setCheckSubscriptionElegMockToReturn(boolean hasSubscription) {
        when(subscriptionService.checkSubscriptions(any(WebCertUser.class))).thenReturn(hasSubscription);
    }

    private void setUnauthorizedElegMissingSubscriptionMockToReturn(boolean missingSubscription) {
        when(subscriptionService.isUnregisteredElegUserMissingSubscription(any(String.class))).thenReturn(missingSubscription);
    }
}
