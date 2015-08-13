package se.inera.auth.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * Throw when an authenticated user hasn't accepted the license and terms for using webcert.
 *
 * Should be picked up by spring-security and perform redirect to the Terms page where they can be accepted.
 */
public class PrivatePractitionerTermsException extends AuthenticationException {
    public PrivatePractitionerTermsException(String message) {
        super(message);
    }
}
