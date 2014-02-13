package se.inera.webcert.modules.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import se.inera.webcert.modules.api.dto.CreateNewIntygModuleRequest;

/**
 * API for communicating with the modules REST services.
 * 
 * @author nikpet
 *
 */
public interface ModuleRestApi {

    /**
     * The module creates a new model using the information supplied in the
     * request.
     * 
     * @param request
     * @return
     */
    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response createModel(CreateNewIntygModuleRequest request);
        
    /**
     * The module validates the model supplied as JSON.
     * 
     * @param draftAsJson The model to be validate as a JSON struct.
     * @return
     */
    @POST
    @Path("/valid-draft")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response validate(String draftAsJson);

}
