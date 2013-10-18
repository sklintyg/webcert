package se.inera.webcert.hsa.stub;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.webcert.hsa.model.Vardgivare;

/**
 * @author johannesc
 */
public class HsaStubRestApi {

    @Autowired
    HsaServiceStub hsaServiceStub;

    @POST
    @Path("/vardgivare")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUnit(List<Vardgivare> vardgivare) {
        hsaServiceStub.getVardgivare().addAll(vardgivare);
        return Response.ok().build();
    }

    @POST
    @Path("/medarbetaruppdrag")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUnit(Medarbetaruppdrag medarbetaruppdrag) {
        hsaServiceStub.getMedarbetaruppdrag().add(medarbetaruppdrag);
        return Response.ok().build();
    }

    @DELETE
    @Path("/medarbetaruppdrag/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMedarbetaruppdrag(@PathParam("id") String id) {
        hsaServiceStub.deleteMedarbetareuppdrag(id);
        return Response.ok().build();
    }

    @DELETE
    @Path("/vardgivare/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteVardgivare(@PathParam("id") String id) {
        hsaServiceStub.deleteVardgivare(id);
        return Response.ok().build();
    }

    @GET
    @Path("/vardgivare")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Vardgivare> getVardgivare() {
        return hsaServiceStub.getVardgivare();
    }

    @GET
    @Path("/medarbetaruppdrag")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Medarbetaruppdrag> getMedarbetaruppdrag() {
        return hsaServiceStub.getMedarbetaruppdrag();
    }
}
