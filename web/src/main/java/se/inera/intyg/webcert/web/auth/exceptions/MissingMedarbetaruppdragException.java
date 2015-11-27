package se.inera.intyg.webcert.web.auth.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * @author andreaskaltenbach
 */
public class MissingMedarbetaruppdragException extends AuthenticationException {

    private static final long serialVersionUID = -4203262608624378942L;

    public MissingMedarbetaruppdragException(String hsaId) {
        super("User with HSA-ID " + hsaId + " does not have any medarbetaruppdrag with 'Vård och behandling'");
    }
}
