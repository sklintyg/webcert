package se.inera.intyg.webcert.web.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.InvalidateRequestService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component(value = "sessionInvalidateFilter")
public class SessionInvalidateFilter extends OncePerRequestFilter {
    @Autowired
    private WebCertUserService userService;
    private static final Logger LOG = LoggerFactory.getLogger(UnitSelectedAssuranceFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        WebCertUser user = getUser();

        if(request.getHeader("launchId") != null && user != null) {
            LOG.info("launchId was present in header");
            final var launchId = request.getHeader("launchId");
            if(!launchId.equals(user.getParameters().getLaunchId())) {
                LOG.error("launchId :" + launchId + " does not match with current session - session is invalidated");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            else{
                LOG.error("filter chain is continued");
                filterChain.doFilter(request,response);
            }
        }
        else {
            LOG.info("filter chain is continued");
            filterChain.doFilter(request,response);
        }
    }
    private WebCertUser getUser() {
        if (userService.hasAuthenticationContext()) {
            return userService.getUser();
        } else {
            return null;
        }
    }
}
