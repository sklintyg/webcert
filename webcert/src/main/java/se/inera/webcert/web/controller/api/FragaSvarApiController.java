package se.inera.webcert.web.controller.api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.service.dto.Lakare;
import se.inera.webcert.service.fragasvar.FragaSvarService;
import se.inera.webcert.service.fragasvar.dto.QueryFragaSvarParameter;
import se.inera.webcert.service.fragasvar.dto.QueryFragaSvarResponse;
import se.inera.webcert.web.controller.AbstractApiController;

@Path("/fragasvar")
public class FragaSvarApiController extends AbstractApiController {

    @Autowired
    private FragaSvarService fragaSvarService;

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response query(@QueryParam("") QueryFragaSvarParameter queryParam) {
        QueryFragaSvarResponse result = fragaSvarService.filterFragaSvar(queryParam);
        return Response.ok(result).build();
    }
   
    @GET
    @Path("/lakare")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getFragaSvarLakareByEnhet(@QueryParam("enhetsId") String enhetsId) {
        List<Lakare> lakare = fragaSvarService.getFragaSvarHsaIdByEnhet(enhetsId);
        return Response.ok(lakare).build();
    }
}
