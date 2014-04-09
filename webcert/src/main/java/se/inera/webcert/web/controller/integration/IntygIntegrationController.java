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

@Path("/intyg")
public class IntygIntegrationController extends AbstractIntegrationController {

    private static Logger LOG = LoggerFactory.getLogger(IntygIntegrationController.class);

    @Autowired
    private IntygService intygService;

    @GET
    @Path("/view/{intygId}")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId) {

        IntygContentHolder intygData = intygService.fetchExternalIntygData(intygId);

        String intygType = intygData.getMetaData().getType();

        LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygType);

        return buildRedirectResponse(uriInfo, intygType, intygId);
    }

}
