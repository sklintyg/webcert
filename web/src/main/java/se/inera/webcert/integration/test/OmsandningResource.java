package se.inera.webcert.integration.test;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import se.inera.webcert.persistence.utkast.repository.OmsandningRepository;

@Transactional
public class OmsandningResource {

    @Autowired
    private OmsandningRepository omsandningRepository;

    /**
     * Deletes all Omsandningar.
     *
     * @return
     */
    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteOmsandningar() {
        omsandningRepository.deleteAll();
        return Response.ok().build();
    }
}
