package se.inera.webcert.integration.test;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;

/**
 * Created by Pehr Assarsson on 9/24/13.
 */
@Transactional
public class QuestionResource {

    @Autowired
    private FragaSvarRepository fragasvarRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insertQuestion(FragaSvar question) {
        fragasvarRepository.save(question);

        return Response.ok().build();
    }

    @DELETE
    @Path("/{externReferens}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response deleteQuestion(@PathParam("externReferens") String externReferens) {
        System.out.println("ext ref: " + externReferens);
        FragaSvar fraga = fragasvarRepository.findByExternReferens( externReferens);
        if (fraga == null) {
            System.out.println("Fraga IS NULL !!!!!!");
        }
        System.out.println("fraga!! : " + fraga.getFrageText());
        fragasvarRepository.delete(fraga);
        return Response.ok().build();
    }
}
