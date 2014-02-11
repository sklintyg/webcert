package se.inera.webcert.modules.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import se.inera.certificate.integration.rest.dto.CertificateContentHolder;
import se.inera.certificate.model.Utlatande;
import se.inera.webcert.modules.api.dto.CreateNewIntygModuleRequest;

public interface ModuleRestApi {

    /**
     * @param transportXml
     * @return
     */
    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    Response createModel(CreateNewIntygModuleRequest request);
    
    /**
     * @param transportXml
     * @return
     */
    @POST
    @Path("/unmarshall")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    Response unmarshall(String transportXml);

    @POST
    @Path("/marshall")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_XML)
    Response marshall(@HeaderParam("X-Schema-Version") String version, String moduleExternalJson);

    /**
     * @param utlatande
     * @return
     */
    @POST
    @Path("/valid")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    Response validate(Utlatande utlatande);

    /**
     * Converts the external model into a PDF.
     */
    @POST
    @Path("/pdf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/pdf")
    Response pdf(CertificateContentHolder certificateContentHolder);

    /**
     * Convert from module-external format to module-internal format.
     */
    @PUT
    @Path("/internal")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response convertExternalToInternal(CertificateContentHolder certificateContentHolder);

    /**
     * Convert from module-internal format to module-external format.
     * 
     * @param utlatande
     * @return
     */
    @PUT
    @Path("/external")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response convertInternalToExternal(Object utlatande);

}
