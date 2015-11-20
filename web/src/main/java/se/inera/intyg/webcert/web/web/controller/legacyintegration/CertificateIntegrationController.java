package se.inera.intyg.webcert.web.web.controller.legacyintegration;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import se.inera.webcert.common.security.authority.UserRole;

/**
 * Controller to enable a landsting user to access certificates directly from a link.
 */
@Path("/basic-certificate")
@Api(value = "/webcert/web/user/basic-certificate", description = "REST API för djupintegration", produces = MediaType.APPLICATION_JSON)
public class CertificateIntegrationController extends LegacyIntygIntegrationController {

    private static final String[] GRANTED_ROLES = new String[] { UserRole.ROLE_LAKARE.name(), UserRole.ROLE_VARDADMINISTRATOR.name() };

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

}
