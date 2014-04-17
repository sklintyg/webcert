package se.inera.webcert.logging;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import se.inera.webcert.hsa.model.SelectableVardenhet;
import se.inera.webcert.hsa.model.WebCertUser;

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

    @Override
    public String convert(ILoggingEvent event) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return "NO USER";
        }

        WebCertUser user = (WebCertUser) auth.getPrincipal();
        StringBuilder sb = new StringBuilder();
        sb.append("UID: ").append(user.getHsaId());

        SelectableVardenhet valdVardenhet = user.getValdVardenhet();
        if (valdVardenhet != null) {
            sb.append(" | CU: ").append(valdVardenhet.getId());
        }

        return sb.toString();
    }

}
