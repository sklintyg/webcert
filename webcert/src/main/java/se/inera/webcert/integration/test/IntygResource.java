package se.inera.webcert.integration.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import se.inera.webcert.persistence.intyg.model.Intyg;
import se.inera.webcert.persistence.intyg.repository.IntygRepository;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Transactional
public class IntygResource {

    @Autowired
    private IntygRepository intygRepository;

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDraft(@PathParam("id") String id) {
        Intyg intyg = intygRepository.findOne(id);
        if (intyg != null) {
            intygRepository.delete(intyg);
        }
        return Response.ok().build();
    }
}
