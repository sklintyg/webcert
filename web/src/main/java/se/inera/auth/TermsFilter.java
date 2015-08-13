package se.inera.auth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.inera.auth.common.BaseFakeAuthenticationProvider;
import se.inera.auth.common.UnifiedUserDetailsService;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.service.privatlakaravtal.AvtalService;

@Component(value = "termsFilter")
public class TermsFilter extends OncePerRequestFilter {

    private static final String PRIVATE_PRACTITIONER_TERMS_ACCEPTED = "ppTermsAccepted";
    private String ignoredUrl;

    private static final Logger log = LoggerFactory.getLogger(TermsFilter.class);

    @Autowired
    AvtalService avtalService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("SPRING_SECURITY_CONTEXT") != null) {
            log.info("HttpSession OK");

            if (session.getAttribute(PRIVATE_PRACTITIONER_TERMS_ACCEPTED) == null || !session.getAttribute(PRIVATE_PRACTITIONER_TERMS_ACCEPTED).equals("true")) {
                Object principal = ((SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT")).getAuthentication().getPrincipal();
                if (principal != null && principal instanceof WebCertUser) {
                    WebCertUser webCertUser = (WebCertUser) principal;
                    if (isElegAuthContext(webCertUser)) {

                        boolean avtalApproved = avtalService.userHasApprovedLatestAvtal(webCertUser.getHsaId());
                        if (avtalApproved) {
                            session.setAttribute(PRIVATE_PRACTITIONER_TERMS_ACCEPTED, "true");
                        } else {
                            session.setAttribute(PRIVATE_PRACTITIONER_TERMS_ACCEPTED, "false");

                            // REDIRECT
                            response.sendRedirect("/terms.jsp");
                        }

                    }
                }
            }


        } else {
            log.info("HttpSession NULL");
        }
        filterChain.doFilter(request, response);
    }

    private boolean isElegAuthContext(WebCertUser webCertUser) {
        return webCertUser.getAuthenticationScheme().equals(UnifiedUserDetailsService.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI) || webCertUser.getAuthenticationScheme().equals(BaseFakeAuthenticationProvider.FAKE_AUTHENTICATION_ELEG_CONTEXT_REF);
    }

    public void setIgnoredUrl(String ignoredUrl) {
        this.ignoredUrl = ignoredUrl;
    }
}
