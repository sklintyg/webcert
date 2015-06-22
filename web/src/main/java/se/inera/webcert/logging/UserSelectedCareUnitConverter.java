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
public class UserSelectedCareUnitConverter extends ClassicConverter {

    private static final String NO_UNIT_SELECTED = "NO UNIT";

    @Override
    public String convert(ILoggingEvent event) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return NO_UNIT_SELECTED;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof WebCertUser) {
            WebCertUser user = (WebCertUser) auth.getPrincipal();
            SelectableVardenhet valdVardenhet = user.getValdVardenhet();
            if (valdVardenhet != null) {
                return valdVardenhet.getId();
            }
        }

        return NO_UNIT_SELECTED;
    }

}
