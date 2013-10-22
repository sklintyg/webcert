package se.inera.auth.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * @author andreaskaltenbach
 */
public class MissingMedarbetaruppdragException extends AuthenticationException {

    public MissingMedarbetaruppdragException(String hsaId) {
        super("User with HSA-ID " + hsaId + " does not have any medarbetaruppdrag with 'Vård och behandling'");
    }
}
