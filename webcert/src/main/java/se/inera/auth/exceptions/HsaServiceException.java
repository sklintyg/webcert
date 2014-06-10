package se.inera.auth.exceptions;

import org.springframework.security.core.AuthenticationException;

public class HsaServiceException extends AuthenticationException {

    public HsaServiceException(String hsaId, Throwable t) {
        super("Building user HSA-ID " + hsaId + " failed", t);
    }
}
