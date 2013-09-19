package se.inera.webcert.fkstub;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.AnswerToFkType;
import se.inera.webcert.sendmedicalcertificatequestionsponder.v1.QuestionToFkType;

/**
 * @author andreaskaltenbach
 */
public class StubRestApi {

    @Autowired
    private QuestionAnswerStore questionAnswerStore;

    @GET
    @Path("/fragor")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<QuestionToFkType> fragor() {
        return questionAnswerStore.getQuestions().values();
    }

    @GET
    @Path("/fragor/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public QuestionToFkType fraga(@PathParam("id") String id) {
        return questionAnswerStore.getQuestions().get(id);
    }

    @GET
    @Path("/svar")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<AnswerToFkType> svar() {
        return questionAnswerStore.getAnswers().values();
    }

    @GET
    @Path("/svar/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AnswerToFkType svar(@PathParam("id") String id) {
        return questionAnswerStore.getAnswers().get(id);
    }
}
