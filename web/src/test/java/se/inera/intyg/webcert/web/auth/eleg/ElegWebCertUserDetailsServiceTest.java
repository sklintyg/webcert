/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml2.core.NameID;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.inera.intyg.infra.integration.hsa.services.HsaPersonService;
import se.inera.intyg.infra.security.exception.HsaServiceException;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.web.auth.common.BaseSAMLCredentialTest;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.HsaId;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.PersonId;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.VardgivareType;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private HsaPersonService hsaPersonService;

    @Mock
    private PPService ppService;

    @Mock
    private WebcertFeatureService webcertFeatureService;

    @Mock
    private AvtalService avtalService;

    @Mock
    private ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;

    @Mock
    private ElegAuthenticationMethodResolver elegAuthenticationMethodResolver;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private ElegWebCertUserDetailsService testee;

    @BeforeClass
    public static void readSamlAssertions() throws Exception {
        bootstrapSamlAssertions();
    }

    @Before
    public void setupForSuccess() {
        // Setup a servlet request
        MockHttpServletRequest request = mockHttpServletRequest("/any/path");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(hsaPersonService.getHsaPersonInfo(anyString())).thenReturn(Collections.emptyList());
        //AUTHORITIES_RESOLVER.setHsaPersonService(hsaPersonService);
        testee.setAuthoritiesResolver(AUTHORITIES_RESOLVER);

        //when(authoritiesResolver.getRole(anyString())).thenReturn(role);
        when(ppService.getPrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(buildHosPerson());
        when(ppService.validatePrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(true);
        when(webcertFeatureService.getActiveFeatures()).thenReturn(new HashSet<String>());
        when(avtalService.userHasApprovedLatestAvtal(anyString())).thenReturn(true);
    }

    @Test
    public void testSuccessfulLogin() {
        Object o = testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
        assertNotNull(o);

        // WEBCERT-2028
        verify(avtalService, times(1)).userHasApprovedLatestAvtal(anyString());
    }

    @Test
    public void testNotValidPrivatePractitionerThrowsException() {
        reset(ppService);
        when(ppService.validatePrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(false);

        thrown.expect(PrivatePractitionerAuthorizationException.class);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test
    public void testNotFoundInHSAThrowsException() {
        reset(ppService);
        when(ppService.validatePrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(true);
        when(ppService.getPrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(null);

        thrown.expect(HsaServiceException.class);

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

    private MockHttpServletRequest mockHttpServletRequest(String requestURI) {
        MockHttpServletRequest request = new MockHttpServletRequest();

        if ((requestURI != null) && (requestURI.length() > 0)) {
            request.setRequestURI(requestURI);
        }

        SavedRequest savedRequest = new DefaultSavedRequest(request, new PortResolverImpl());
        request.getSession().setAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY, savedRequest);

        return request;
    }


}
