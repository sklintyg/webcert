/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.oidc.jwt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MissingClaimException;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.webcert.web.service.jwt.JwtValidationService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class JwtAuthenticationFilterTest {

    private static final String USER_ID = "hsa-123";

    @Mock
    private FeaturesHelper featuresHelper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtValidationService jwtValidationService;

    @InjectMocks
    private JwtAuthenticationFilter testee = new JwtAuthenticationFilter(new AntPathRequestMatcher("/"));

    private HttpServletRequest req = mock(HttpServletRequest.class);

    private HttpServletResponse resp = mock(HttpServletResponse.class);

    private Claims claims = mock(Claims.class);
    private Jws<Claims> jws = mock(Jws.class);

    @Before
    public void init() {
        when(featuresHelper.isFeatureActive(anyString())).thenReturn(true);
        when(req.getMethod()).thenReturn("POST");
        when(req.getParameter("access_token")).thenReturn("access-token");
        when(authenticationManager.authenticate(any(JwtAuthenticationToken.class)))
                .thenReturn(new ExpiringUsernameAuthenticationToken(mock(WebCertUser.class), mock(WebCertUser.class)));
    }

    @Test
    public void testOkWithPlainEmployeeHsaIdParameter() {
        when(claims.get("employeeHsaId")).thenReturn(USER_ID);
        when(jws.getBody()).thenReturn(claims);
        when(jwtValidationService.validateJwsToken(anyString())).thenReturn(jws);
        testee.attemptAuthentication(req, resp);
    }

    @Test
    public void testOkWithArrayListEmployeeHsaIdParameter() {
        when(claims.get("employeeHsaId")).thenReturn(Arrays.asList(USER_ID));
        when(jws.getBody()).thenReturn(claims);
        when(jwtValidationService.validateJwsToken(anyString())).thenReturn(jws);
        testee.attemptAuthentication(req, resp);
    }

    @Test
    public void testOkWithBearerHeader() {
        when(req.getParameter("access_token")).thenReturn(null);
        when(req.getHeader("Authorization")).thenReturn("Bearer: access-token");
        when(claims.get("employeeHsaId")).thenReturn(USER_ID);
        when(jws.getBody()).thenReturn(claims);
        when(jwtValidationService.validateJwsToken(anyString())).thenReturn(jws);
        testee.attemptAuthentication(req, resp);
    }

    @Test(expected = IncorrectClaimException.class)
    public void testReturnsUnsupportedEmployeeHsaIdClass() {
        when(claims.get("employeeHsaId")).thenReturn(123);
        when(jws.getBody()).thenReturn(claims);
        when(jwtValidationService.validateJwsToken(anyString())).thenReturn(jws);
        testee.attemptAuthentication(req, resp);
    }

    @Test(expected = IncorrectClaimException.class)
    public void testReturnsEmptyArrayListAsEmployeeHsaId() {
        when(claims.get("employeeHsaId")).thenReturn(new ArrayList<>());
        when(jws.getBody()).thenReturn(claims);
        when(jwtValidationService.validateJwsToken(anyString())).thenReturn(jws);
        testee.attemptAuthentication(req, resp);
    }

    @Test(expected = MissingClaimException.class)
    public void testReturnsNullEmployeeHsaIdClass() {
        when(claims.get("employeeHsaId")).thenReturn(null);
        when(jws.getBody()).thenReturn(claims);
        when(jwtValidationService.validateJwsToken(anyString())).thenReturn(jws);
        testee.attemptAuthentication(req, resp);
    }

    @Test(expected = MissingClaimException.class)
    public void testReturnsEmptyStringEmployeeHsaIdClass() {
        when(claims.get("employeeHsaId")).thenReturn("");
        when(jws.getBody()).thenReturn(claims);
        when(jwtValidationService.validateJwsToken(anyString())).thenReturn(jws);
        testee.attemptAuthentication(req, resp);
    }

    @Test(expected = AuthenticationServiceException.class)
    public void testPostContainsNoTokenInHeaderOrBody() {
        when(req.getParameter("access_token")).thenReturn(null);
        testee.attemptAuthentication(req, resp);
    }

    @Test(expected = AuthenticationServiceException.class)
    public void testThrowsExceptionIfFeatureNotActive() {
        when(featuresHelper.isFeatureActive(anyString())).thenReturn(false);
        testee.attemptAuthentication(req, resp);
    }

    @Test(expected = AuthenticationServiceException.class)
    public void testThrowsExceptionIfUnsupportedMethod() {
        when(req.getMethod()).thenReturn("GET");
        testee.attemptAuthentication(req, resp);
    }

    @Test(expected = AuthenticationServiceException.class)
    public void testThrowsExceptionIfNotParameterNorBearerHeader() {
        when(req.getParameter("access_token")).thenReturn(null);
        when(req.getHeader("Authorization")).thenReturn("Basic");
        testee.attemptAuthentication(req, resp);
    }

}
