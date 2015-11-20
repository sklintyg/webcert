package se.inera.intyg.webcert.web.web.controller.moduleapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DiagnosParameter;

/**
 * Controller exposing diagnosis services to be used by modules.
 *
 * @author npet
 *
 */
@Path("/diagnos")
@Api(value = "diagnos", description = "REST API - moduleapi - diagnos", produces = MediaType.APPLICATION_JSON)
public class DiagnosModuleApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosModuleApiController.class);

    @Autowired
    private DiagnosService diagnosService;

    /**
     * Search for diagnosises using a code fragment. The fragment "A04" will return all
     * diagnosises whose code starts with this fragment. The number of results returned
     * by the service can be limited by setting the 'NbrOfResults' parameter to a positive
     * number.
     *
     * @param parameter
     *            A parameter object.
     */
    @POST
    @Path("/kod/sok")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response searchDiagnosisByCode(DiagnosParameter parameter) {

        LOG.debug("Searching for diagnosises using code fragment: {}", parameter.getCodeFragment());

        DiagnosResponse diagnosResponse = diagnosService.searchDiagnosisByCode(parameter.getCodeFragment(), parameter.getCodeSystem(), parameter.getNbrOfResults());
        return Response.ok(diagnosResponse).build();
    }

    /**
     * Search for diagnosises using a description fragment. The number of results returned
     * by the service can be limited by setting the 'NbrOfResults' parameter to a positive
     * number.
     */
    @POST
    @Path("/beskrivning/sok")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response searchDiagnosisByDescription(DiagnosParameter parameter) {

        LOG.debug("Searching for diagnosises using description fragment: {}", parameter.getDescriptionSearchString());

        DiagnosResponse diagnosResponse = diagnosService.searchDiagnosisByDescription(parameter.getDescriptionSearchString(), parameter.getCodeSystem(), parameter.getNbrOfResults());
        return Response.ok(diagnosResponse).build();
    }
}
