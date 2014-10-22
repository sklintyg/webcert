package se.inera.webcert.web.controller.moduleapi;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.service.feature.WebcertFeature;
import se.inera.webcert.service.fragasvar.FragaSvarService;
import se.inera.webcert.web.controller.AbstractApiController;
import se.inera.webcert.web.controller.moduleapi.dto.CreateQuestionParameter;

@Path("/fragasvar")
public class FragaSvarModuleApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarModuleApiController.class);

    @Autowired
    private FragaSvarService fragaSvarService;

    @GET
    @Path("/{intygId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public List<FragaSvar> fragaSvarForIntyg(@PathParam("intygId") String intygId) {
        abortIfFragaSvarNotActive(); 
        return fragaSvarService.getFragaSvar(intygId);
    }

    @PUT
    @Path("/{fragasvarId}/answer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response answer(@PathParam("fragasvarId") final Long frageSvarId, String svarsText) {
        abortIfFragaSvarNotActive();
        LOG.debug("Set answer for question {}", frageSvarId);
        FragaSvar fragaSvarResponse = fragaSvarService.saveSvar(frageSvarId, svarsText);
        return Response.ok(fragaSvarResponse).build();
    }

    @PUT
    @Path("/{fragasvarId}/setDispatchState")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response setDispatchState(@PathParam("fragasvarId") final Long frageSvarId, Boolean isDispatched) {
        abortIfFragaSvarNotActive();
        LOG.debug("Set DispatchState for question {}, isDispatched: {}", frageSvarId, isDispatched);
        FragaSvar fragaSvarResponse = fragaSvarService.setDispatchState(frageSvarId, isDispatched);
        return Response.ok(fragaSvarResponse).build();
    }


    @POST
    @Path("/{intygId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response createQuestion(@PathParam("intygId") final String intygId, CreateQuestionParameter parameter) {
        abortIfFragaSvarNotActive();
        LOG.debug("New question for cert {} with subject {}", intygId, parameter.getAmne());
        FragaSvar fragaSvarResponse = fragaSvarService.saveNewQuestion(intygId, parameter.getAmne(), parameter.getFrageText());
        return Response.ok(fragaSvarResponse).build();
    }

    @GET
    @Path("/close/{fragasvarId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public FragaSvar closeAsHandled(@PathParam("fragasvarId") Long fragasvarId) {
        abortIfFragaSvarNotActive();
        return fragaSvarService.closeQuestionAsHandled(fragasvarId);
    }

    @GET
    @Path("/open/{fragasvarId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public FragaSvar openAsUnhandled(@PathParam("fragasvarId") Long fragasvarId) {
        abortIfFragaSvarNotActive();
        return fragaSvarService.openQuestionAsUnhandled(fragasvarId);
    }
    
    private void abortIfFragaSvarNotActive() {
        abortIfWebcertFeatureIsNotAvailable(WebcertFeature.HANTERA_FRAGOR);
    }
}
