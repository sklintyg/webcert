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
package se.inera.intyg.webcert.web.auth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import se.inera.intyg.infra.integration.hsa.model.Vardenhet;
import se.inera.intyg.infra.integration.hsa.model.Vardgivare;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_CONTEXT;

/**
 * Created by eriklupander on 2016-09-13.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationEnhetFilterTest {

    private static final String INTYG_ID = "intyg-123";
    private static final String EMPLOYEE_HSA_ID = "person-456";
    private static final String ENHET_ID = "enhet-789";
    private static final String ENHET_ANNAN = "annan-enhet";
    private static final String VARDGIVAR_ID = "vg-1";
    private static final String VARDGIVAR_ID_2 = "vg-2";

    private IntegrationEnhetFilter testee;

    @Mock
    HttpServletRequest req;

    @Mock
    HttpServletResponse resp;

    @Mock
    FilterChain filterChain;

    @Mock
    HttpSession session;

    @Mock
    SecurityContextImpl securityContextImpl;

    @Mock
    Authentication authentication;

    @org.junit.Before
    public void init() {
        testee = new IntegrationEnhetFilter();
        when(req.getRequestURL()).thenReturn(new StringBuffer().append("http://localhost:9088/visa/intyg/" + INTYG_ID));
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute(SPRING_SECURITY_CONTEXT)).thenReturn(securityContextImpl);
        when(securityContextImpl.getAuthentication()).thenReturn(authentication);
    }

    @Test
    public void testRedirectsWhenNoEnhetIsSpecified() throws ServletException, IOException {
        when(authentication.getPrincipal()).thenReturn(new WebCertUser(buildIntygUserWithMultipleVgAndVe()));
        when(req.getQueryString()).thenReturn("fornamn=Fornamnet");
        testee.doFilterInternal(req, resp, filterChain);

        String expectedRedirectUrl = "/web/dashboard?destination=" + URLEncoder.encode("http://localhost:9088/visa/intyg/" + INTYG_ID + "?fornamn=Fornamnet", "UTF-8") + "#/integration-enhetsval";
        verify(resp, times(1)).sendRedirect(expectedRedirectUrl);
    }

    @Test
    public void testRedirectsWhenNoQueryStringIsSpecified() throws ServletException, IOException {
        when(authentication.getPrincipal()).thenReturn(new WebCertUser(buildIntygUserWithMultipleVgAndVe()));
        when(req.getQueryString()).thenReturn("");
        testee.doFilterInternal(req, resp, filterChain);

        String expectedRedirectUrl = "/web/dashboard?destination=" + URLEncoder.encode("http://localhost:9088/visa/intyg/" + INTYG_ID, "UTF-8") + "#/integration-enhetsval";
        verify(resp, times(1)).sendRedirect(expectedRedirectUrl);
    }

    @Test
    public void testRedirectsToErrorPageWhenEnhetNotAvailableForUser() throws ServletException, IOException {
        when(authentication.getPrincipal()).thenReturn(new WebCertUser(buildIntygUserWithMultipleVgAndVe()));
        when(req.getQueryString()).thenReturn("fornamn=Fornamnet&enhet=" + ENHET_ANNAN);
        testee.doFilterInternal(req, resp, filterChain);

        String expectedRedirectUrl = "/error.jsp?reason=login.medarbetaruppdrag";
        verify(resp, times(1)).sendRedirect(expectedRedirectUrl);
    }

    @Test
    public void testContinuesFilterChainWhenValidEnhetIsSpecified() throws ServletException, IOException {
        when(authentication.getPrincipal()).thenReturn(new WebCertUser(buildIntygUserWithMultipleVgAndVe()));
        when(req.getQueryString()).thenReturn("fornamn=Fornamnet&enhet=" + ENHET_ID + "-0");
        testee.doFilterInternal(req, resp, filterChain);

        verify(resp, times(0)).sendRedirect(any());
        verify(filterChain, times(1)).doFilter(req, resp);
    }

    @Test
    public void testContinuesFilterChainWhenNoEnhetIsSuppliedButUserHasExactlyOneSelectableEnhet() throws IOException, ServletException {
        when(authentication.getPrincipal()).thenReturn(new WebCertUser(buildIntygUserWithSingleEnhet()));
        when(req.getQueryString()).thenReturn("");
        testee.doFilterInternal(req, resp, filterChain);

        verify(resp, times(0)).sendRedirect(any());
        verify(filterChain, times(1)).doFilter(req, resp);
    }

    @Test
    public void testRedirectsToSelectionPageWhenNoEnhetIsSuppliedButUserHasMultipleEnhetsToChooseFrom() throws IOException, ServletException {
        when(authentication.getPrincipal()).thenReturn(new WebCertUser(buildIntygUserWithMultipleVgAndVe()));
        when(req.getQueryString()).thenReturn("");
        testee.doFilterInternal(req, resp, filterChain);

        verify(resp, times(1)).sendRedirect(any());
        verify(filterChain, times(0)).doFilter(req, resp);
    }

    private IntygUser buildIntygUserWithMultipleVgAndVe() {
        IntygUser intygUser = new IntygUser(EMPLOYEE_HSA_ID);
        intygUser.setVardgivare(Arrays.asList(
                buildVardgivare(VARDGIVAR_ID, "vårdgivaren 1", 2),
                buildVardgivare(VARDGIVAR_ID_2, "vårdgivaren 2", 2)
        ));
        return intygUser;
    }

    private IntygUser buildIntygUserWithSingleEnhet() {
        IntygUser intygUser = new IntygUser(EMPLOYEE_HSA_ID);
        intygUser.setVardgivare(Collections.singletonList(buildVardgivare(VARDGIVAR_ID, "vårdgivaren 1", 1)));
        return intygUser;
    }

    private Vardgivare buildVardgivare(String vardgivarId, String vardgivarNamn, int antalEnheter) {
        Vardgivare vg = new Vardgivare(vardgivarId, vardgivarNamn);
        for (int a = 0; a < antalEnheter; a++) {
            vg.getVardenheter().add(buildVardenhet(ENHET_ID + "-" + a, "Vårdenhet 123-" + a));
        }

        return vg;
    }

    private Vardenhet buildVardenhet(String enhetId, String enhetNamn) {
        return new Vardenhet(enhetId, enhetNamn);
    }

}
