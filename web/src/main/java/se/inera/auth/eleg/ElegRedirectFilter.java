package se.inera.auth.eleg;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by eriklupander on 2015-09-28.
 */
@Component(value = "elegRedirectFilters")
public class ElegRedirectFilter extends OncePerRequestFilter {

    @Value("${cgi.funktionstjanster.saml.idp.metadata.url}")
    private String elegIdpUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        httpServletResponse.sendRedirect("/saml/login/alias/eleg?idp=" + elegIdpUrl);
        return;
    }
}
