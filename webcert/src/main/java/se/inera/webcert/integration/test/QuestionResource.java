package se.inera.webcert.integration.test;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

/**
 * Created by Pehr Assarsson on 9/24/13.
 */
@Transactional
public class QuestionResource {

    @Autowired
    private CrudRepository<FragaSvar, Long> fragasvarRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response insertQuestion(FragaSvar question) {
        fragasvarRepository.save(question);

        return Response.ok().build();
    }
}
