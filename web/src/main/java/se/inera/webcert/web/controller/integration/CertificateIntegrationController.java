package se.inera.webcert.web.controller.integration;

import javax.ws.rs.Path;

import se.inera.webcert.common.security.authority.UserRole;

/**
 * Controller to enable a landsting user to access certificates directly from a link.
 */
@Path("/basic-certificate")
public class CertificateIntegrationController extends LegacyIntygIntegrationController {

    private static final String[] GRANTED_ROLES = new String[] { UserRole.ROLE_LAKARE.name(), UserRole.ROLE_VARDADMINISTRATOR.name() };

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

}
