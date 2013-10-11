package se.inera.webcert.integration.test;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
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

    @GET
    @Path("/extern/{externReferens}")
    @Produces(MediaType.APPLICATION_JSON)
    public FragaSvar getCertificateByExternReferens(@PathParam("externReferens") String externReferens) {
        return fragasvarRepository.findByExternReferens(externReferens);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public FragaSvar getCertificate(@PathParam("id") Long id) {
        return fragasvarRepository.findOne(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertQuestion(FragaSvar question) {
        fragasvarRepository.save(question);
        return Response.ok(question).build();
    }

    @DELETE
    @Path("/extern/{externReferens}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuestionByExternReferens(@PathParam("externReferens") String externReferens) {
        FragaSvar fraga = fragasvarRepository.findByExternReferens(externReferens);
        fragasvarRepository.delete(fraga);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuestion(@PathParam("id") Long id) {
        fragasvarRepository.delete(id);
        return Response.ok().build();
    }
}
