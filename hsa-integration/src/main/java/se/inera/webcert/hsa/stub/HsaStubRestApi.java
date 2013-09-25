package se.inera.webcert.hsa.stub;


import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author johannesc
 */
public class HsaStubRestApi {

    @Autowired
    HsaServiceStub hsaServiceStub;

    @POST
    @Path("/enhet")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUnit(HsaUnitStub unit) {
        hsaServiceStub.addHsaUnit(unit);
        return Response.ok().build();
    }

    @DELETE
    @Path("/enhet/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUnit(@PathParam("id") String id) {
        hsaServiceStub.deleteHsaUnit(id);
        return Response.ok().build();
    }

    @POST
    @Path("/medarbetaruppdrag")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addUnit(PersonStub person) {
        try {
            hsaServiceStub.addPerson(person);
        } catch (RuntimeException e) {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/medarbetaruppdrag/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePerson(@PathParam("id") String id) {
        hsaServiceStub.deletePerson(id);
        return Response.ok().build();
    }

}
