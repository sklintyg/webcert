package se.inera.webcert.logging;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Logback converter that returns the id of the current user session.
 *
 * @author nikpet
 */
public class SessionConverter extends ClassicConverter {

    private static final String NO_SESSION = "NO SESSION";

    @Override
    public String convert(ILoggingEvent event) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getSessionId();
        }
        return NO_SESSION;
    }

}
