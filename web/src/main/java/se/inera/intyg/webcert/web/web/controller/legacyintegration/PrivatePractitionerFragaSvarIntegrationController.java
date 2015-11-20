package se.inera.intyg.webcert.web.web.controller.legacyintegration;

import static se.inera.certificate.common.enumerations.CertificateTypes.FK7263;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.common.security.authority.UserRole;
import se.inera.intyg.webcert.web.web.controller.integration.BaseIntegrationController;
import io.swagger.annotations.Api;

/**
 * Created by eriklupander on 2015-10-08.
 */
@Path("/pp-certificate")
@Api(value = "webcert web user pp-certificate (Fråga/Svar, uthopp privatläkare)", description = "REST API för fråga/svar via uthoppslänk, privatläkare", produces = MediaType.APPLICATION_JSON)
public class PrivatePractitionerFragaSvarIntegrationController extends BaseIntegrationController {

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";

    private static final Logger LOG = LoggerFactory.getLogger(LegacyIntygIntegrationController.class);

    private String urlFragmentTemplate;

    public void setUrlFragmentTemplate(String urlFragmentTemplate) {
        this.urlFragmentTemplate = urlFragmentTemplate;
    }

    /**
     * Fetches a certificate from IT and then performs a redirect to the view that displays
     * the questions for the cert. Can be used for FK7263 certificates.
     *
     * @param uriInfo
     * @param intygId
     *            The id of the certificate to view.
     * @return
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

    // - - - - - Default scope - - - - -

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<String, Object>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);

        URI location = uriBuilder.replacePath(getUrlBaseTemplate()).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Response.Status.TEMPORARY_REDIRECT).location(location).build();
    }




    @Override
    protected String[] getGrantedRoles() {
        return new String[]{UserRole.ROLE_PRIVATLAKARE.name()};
    }

}
