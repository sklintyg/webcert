package se.inera.webcert.web.controller.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.service.FragaSvarService;
import se.inera.webcert.web.controller.api.dto.QueryFragaSvarParameter;
import se.inera.webcert.web.controller.api.dto.QueryFragaSvarResponse;
import se.inera.webcert.web.service.WebCertUserService;

public class FragaSvarApiController {

    /**
     * Helper service to get current user.
     */
    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private WebCertUserService webCertUserService;

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<FragaSvar> list() {
        WebCertUser user = webCertUserService.getWebCertUser();
        return fragaSvarService.getFragaSvar(user.getVardenheterIds());
    }

    @PUT
    @Path("/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response query(final QueryFragaSvarParameter queryParam) {
        QueryFragaSvarResponse result = new QueryFragaSvarResponse();
        result.setTotalCount(fragaSvarService.getFragaSvarByFilterCount(queryParam.getFilter()));
        
        List<FragaSvar> queryResults = fragaSvarService.getFragaSvarByFilter(queryParam.getFilter(),
                queryParam.getStartFrom(), queryParam.getPageSize());
        result.setResults(queryResults);
        
        return Response.ok(result).build();
    }
    
    @PUT
    @Path("/query/paging")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response queryNoCount(final QueryFragaSvarParameter queryParam) {
        QueryFragaSvarResponse result = new QueryFragaSvarResponse();
        result.setTotalCount(-1);
        
        List<FragaSvar> queryResults = fragaSvarService.getFragaSvarByFilter(queryParam.getFilter(),
                queryParam.getStartFrom(), queryParam.getPageSize());
        result.setResults(queryResults);
        
        return Response.ok(result).build();
    }
}
