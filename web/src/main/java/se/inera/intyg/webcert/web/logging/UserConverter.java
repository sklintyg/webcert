package se.inera.intyg.webcert.web.logging;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback converter that returns information about the current user.
 * User info is retrieved from the Spring Security context. If no context
 * is available a NO USER is returned.
 *
 * @author nikpet
 */
public class UserConverter extends ClassicConverter {

    private static final String NO_USER = "NO USER";

    @Override
    public String convert(ILoggingEvent event) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return NO_USER;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof WebCertUser) {
            WebCertUser user = (WebCertUser) auth.getPrincipal();
            return user.getHsaId();
        }

        return NO_USER;
    }

}
