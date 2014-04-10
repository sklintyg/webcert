package se.inera.webcert.web.controller.integration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.service.IntygService;
import se.inera.webcert.service.dto.IntygContentHolder;
import se.inera.webcert.web.controller.AbstractIntegrationController;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 * 
 * @author nikpet
 *
 */
@Path("/intyg")
public class IntygIntegrationController extends AbstractIntegrationController {

    private static Logger LOG = LoggerFactory.getLogger(IntygIntegrationController.class);

    @Autowired
    private IntygService intygService;

    /**
     * Fetches a certificate from IT and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     * 
     * @param uriInfo
     * @param intygId The id of the certificate to view.
     * @return
     */
    @GET
    @Path("/view/{intygId}")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId) {

        IntygContentHolder intygData = intygService.fetchExternalIntygData(intygId);

        String intygType = intygData.getMetaData().getType();

        LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygType);

        return buildRedirectResponse(uriInfo, intygType, intygId);
    }

}
