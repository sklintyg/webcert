package se.inera.intyg.webcert.web.auth.exceptions;

import org.springframework.security.core.AuthenticationException;

public class HsaServiceException extends AuthenticationException {

    private static final long serialVersionUID = -1932413969496764652L;

    public HsaServiceException(String hsaId, Throwable t) {
        super("Building user HSA-ID " + hsaId + " failed", t);
    }
}
