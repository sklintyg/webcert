package se.inera.intyg.webcert.web.security;

import org.springframework.security.core.AuthenticationException;

/**
 * Created by Magnus Ekstrand on 16/09/15.
 */
public class AuthoritiesException extends AuthenticationException {

    private static final long serialVersionUID = -4951218504958281758L;

    /**
     * Constructs an {@code AuthenticationException} with the specified message and root cause.
     *
     * @param msg the detail message
     * @param t   the root cause
     */
    public AuthoritiesException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Constructs an {@code AuthenticationException} with the specified message and no root cause.
     *
     * @param msg the detail message
     */
    public AuthoritiesException(String msg) {
        super(msg);
    }

}
