package se.inera.intyg.webcert.web.web.controller.legacyintegration;

import static se.inera.intyg.common.support.common.enumerations.CertificateTypes.FK7263;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.intyg.webcert.web.web.controller.integration.BaseIntegrationController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * @author nikpet
 */
@Path("/certificate")
@Api(value = "webcert web user certificate (Fråga/Svar uthopp)", description = "REST API för fråga/svar via uthoppslänk, landstingspersonal", produces = MediaType.APPLICATION_JSON)
public class LegacyIntygIntegrationController extends BaseIntegrationController {

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";

    private static final Logger LOG = LoggerFactory.getLogger(LegacyIntygIntegrationController.class);

    private static final String[] GRANTED_ROLES = new String[] { UserRole.ROLE_LAKARE_UTHOPP.name(), UserRole.ROLE_TANDLAKARE_UTHOPP.name(), UserRole.ROLE_VARDADMINISTRATOR_UTHOPP.name() };

    private String urlFragmentTemplate;

    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

    /**
     * Fetches a certificate from IT and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param intygId
     *            The id of the certificate to view.
     */
    @GET
    @Path("/{intygId}/questions")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId) {

        boolean ok = super.validateRedirectToIntyg(intygId);
        if (!ok) {
            return Response.serverError().build();
        }

        String intygType = FK7263.toString();
        LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygType);

        return buildRedirectResponse(uriInfo, intygType, intygId);
    }

    public void setUrlFragmentTemplate(String urlFragmentTemplate) {
        this.urlFragmentTemplate = urlFragmentTemplate;
    }

    // - - - - - Default scope - - - - -

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);

        URI location = uriBuilder.replacePath(getUrlBaseTemplate()).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

}
