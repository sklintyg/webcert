package se.inera.webcert.web.controller.integration;

import static se.inera.certificate.common.enumerations.CertificateTypes.FK7263;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.security.AuthoritiesAssertion;
import se.inera.webcert.security.AuthoritiesException;
import se.inera.webcert.service.intyg.IntygService;
import se.inera.webcert.service.user.WebCertUserService;
import se.inera.webcert.service.user.dto.WebCertUser;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * @author nikpet
 */
@Path("/certificate")
public class LegacyIntygIntegrationController extends AuthoritiesAssertion {

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";

    private static final Logger LOG = LoggerFactory.getLogger(LegacyIntygIntegrationController.class);

    private static final String[] GRANTED_ROLES = new String[] { UserRole.ROLE_LAKARE_UTHOPP.name(), UserRole.ROLE_VARDADMINISTRATOR_UTHOPP.name() };

    private String urlBaseTemplate;

    private String urlFragmentTemplate;

    @Autowired
    private IntygService intygService;

    @Autowired
    private WebCertUserService webCertUserService;

    /**
     * Fetches a certificate from IT and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param uriInfo
     * @param intygId
     *            The id of the certificate to view.
     * @return
     */
    @GET
    @Path("/{intygId}/questions")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId) {

        if (StringUtils.isBlank(intygId)) {
            LOG.error("Path parameter 'intygId' was either whitespace, empty (\"\") or null");
            return Response.serverError().build();
        }

        WebCertUser user = webCertUserService.getUser();

        try {
            // Ensure user has valid role
            assertUserRole(user);
        } catch (AuthoritiesException e) {
            LOG.error(e.getMessage());
            return Response.serverError().build();
        }

        String intygType = FK7263.toString();
        LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygType);

        return buildRedirectResponse(uriInfo, intygType, intygId);
    }

    public void setUrlBaseTemplate(String urlBaseTemplate) {
        this.urlBaseTemplate = urlBaseTemplate;
    }

    public void setUrlFragmentTemplate(String urlFragmentTemplate) {
        this.urlFragmentTemplate = urlFragmentTemplate;
    }


    // - - - - - Default scope - - - - -

    /*
     * Gör inget om användare redan har rollen:
     * - ROLE_LAKARE_UTHOPP eller
     * - ROLE_VARDADMINISTRATOR_UTHOPP
     * 
     * Om användare har rollen:
     * - ROLE_LAKARE eller
     * - ROLE_VARDADMINISTRATOR
     * 
     * så ändra/nedgradera rollen till
     * - ROLE_LAKARE_UTHOPP eller
     * - ROLE_VARDADMINISTRATOR_UTHOPP
     * 
     * För alla andra roller, eller ingen roll,
     * släng ett exception.
     */
    void assertUserRole(WebCertUser user) {

        Map<String, String> userRoles = user.getRoles();

        List<String> gr = Arrays.asList(new String[] { UserRole.ROLE_LAKARE.name(), UserRole.ROLE_VARDADMINISTRATOR.name() });
        for (String role : userRoles.keySet()) {
            if (gr.contains(role)) {
                updateUserRoles(user);
                return;
            }
        }

        // Assert user has a valid role for this request
        webCertUserService.assertUserRoles(GRANTED_ROLES);
    }


    // - - - - - Default scope - - - - -

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<String, Object>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);

        URI location = uriBuilder.replacePath(urlBaseTemplate).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

    private void updateUserRoles(WebCertUser user) {
        boolean isDoctor = user.isLakare();
        String userRole = UserRole.ROLE_VARDADMINISTRATOR_UTHOPP.name();

        if (isDoctor) {
            userRole = UserRole.ROLE_LAKARE_UTHOPP.name();
        }

        LOG.debug("Updating user role to be {}", userRole);
        webCertUserService.updateUserRoles(new String[] { userRole });
    }

}
