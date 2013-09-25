package se.inera.webcert.integration.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Pehr Assarsson on 9/24/13.
 */
@Path("/question")
@Transactional
public class QuestionResource {

    @Autowired
    private CrudRepository<FragaSvar, Long> fragasvarRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response insertQuestion(FragaSvar question) {
        fragasvarRepository.save(question);

        return Response.ok().build();
    }
}
