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
package se.inera.intyg.webcert.web.auth;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.FAKE_AUTHENTICATION_ELEG_CONTEXT_REF;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_CONTEXT;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_MOBILE_TWO_FACTOR_CONTRACT;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SMARTCARD_PKI;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * This filter should run after security checks.
 *
 * <li>If not logged in, filter is ignored</li>
 * <li>If session attribute
 * {@link TermsFilter#PRIVATE_PRACTITIONER_TERMS_ACCEPTED} is set to true,
 * all is well</li>
 * <li>If the authorization context is
 * {@link URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI} (e.g. privatläkare) then we use
 * the {@link AvtalService} to verify that the user has accepted webcert license and terms</li>
 * <li>If user has not
 * accepted webcert license and terms, we redirect them to /terms.jsp (or eq.) _without_ logging them out</li>
 */
@Component(value = "termsFilter")
public class TermsFilter extends OncePerRequestFilter {

    static final String PRIVATE_PRACTITIONER_TERMS_ACCEPTED = "ppTermsAccepted";
    static final String PRIVATE_PRACTITIONER_TERMS_INPROGRESS = "ppTermsInProgress";

    @Autowired
    private AvtalService avtalService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        if (session.getAttribute(PRIVATE_PRACTITIONER_TERMS_INPROGRESS) == null) {
            session.setAttribute(PRIVATE_PRACTITIONER_TERMS_INPROGRESS, false);
        }
        if (session.getAttribute(PRIVATE_PRACTITIONER_TERMS_ACCEPTED) == null) {
            session.setAttribute(PRIVATE_PRACTITIONER_TERMS_ACCEPTED, false);
        }

        boolean ppTermsAccepted = (boolean) session.getAttribute(PRIVATE_PRACTITIONER_TERMS_ACCEPTED);
        // if we've accepted the terms then in progress is definetly false
        if (ppTermsAccepted) {
            session.setAttribute(PRIVATE_PRACTITIONER_TERMS_INPROGRESS, false);
        }

        boolean ppTermsInprogress = (boolean) session.getAttribute(PRIVATE_PRACTITIONER_TERMS_INPROGRESS);

        if (hasSessionWithSpringContext(session)) {

            if (!ppTermsInprogress && !ppTermsAccepted) {
                Object principal = ((SecurityContextImpl) session.getAttribute(SPRING_SECURITY_CONTEXT)).getAuthentication().getPrincipal();
                if (principal != null && principal instanceof WebCertUser) {
                    WebCertUser webCertUser = (WebCertUser) principal;
                    if (isElegAuthContext(webCertUser)) {

                        boolean avtalApproved = avtalService.userHasApprovedLatestAvtal(webCertUser.getHsaId());
                        if (avtalApproved) {
                            session.setAttribute(PRIVATE_PRACTITIONER_TERMS_ACCEPTED, true);
                            session.setAttribute(PRIVATE_PRACTITIONER_TERMS_INPROGRESS, false);
                            webCertUser.setPrivatLakareAvtalGodkand(true);
                        } else {
                            session.setAttribute(PRIVATE_PRACTITIONER_TERMS_ACCEPTED, false);
                            session.setAttribute(PRIVATE_PRACTITIONER_TERMS_INPROGRESS, true);
                            // REDIRECT. Note that we have gotten IllegalStateExceptions after redirect due to response
                            // already have been commited. Hopefully the return (breaking the filter chain) can mitigate
                            // this.
                            response.sendRedirect("/#/terms");
                            return;
                        }

                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean hasSessionWithSpringContext(HttpSession session) {
        return session != null && session.getAttribute(SPRING_SECURITY_CONTEXT) != null;
    }

    private boolean isElegAuthContext(WebCertUser webCertUser) {
        return webCertUser.getAuthenticationScheme().equals(URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI)
            || webCertUser.getAuthenticationScheme().equals(URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SMARTCARD_PKI)
            || webCertUser.getAuthenticationScheme().equals(URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_MOBILE_TWO_FACTOR_CONTRACT)
            || webCertUser.getAuthenticationScheme().equals(FAKE_AUTHENTICATION_ELEG_CONTEXT_REF);
    }
}
