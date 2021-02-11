package se.inera.intyg.webcert.web.web.filter;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *  This filter is used to set the character encoding to UTF-8 when handling POST requests to /visa/intyg/utf/{certId}.
 *  {@link se.inera.intyg.webcert.web.web.controller.integration.IntygIntegrationController}
 *
 *  It solves an issue where saving a request to redis corrupts the request data if characters such as åäö are present.
 *  This filter does not alter the original endpoint /visa/intyg/{certId}.
 *  @author robertdanielsson
 */

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component(value = "defaultCharacterEncodingFilter")
public class DefaultCharacterEncodingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        if(request.getCharacterEncoding() == null){
            request.setCharacterEncoding("UTF-8");
        }

        filterChain.doFilter(request, response);
    }
}