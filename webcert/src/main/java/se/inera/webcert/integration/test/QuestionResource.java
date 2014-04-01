package se.inera.webcert.integration.test;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.fragasvar.repository.FragaSvarRepository;

/**
 * Bean for inserting questions directly into the database
 * 
 * Created by Pehr Assarsson on 9/24/13.
 */
@Transactional
public class QuestionResource {

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;
    
    @Autowired
    public void setTxManager(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager); 
    }


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
    @Path("/frageText/{frageText}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuestionsByFrageText(@PathParam("frageText") String frageText) {
        List<FragaSvar> fragorOchSvar = fragasvarRepository.findByFrageTextLike(frageText);
        if (fragorOchSvar != null) {
            for (FragaSvar fragaSvar : fragorOchSvar) {
                fragasvarRepository.delete(fragaSvar);
            }
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/svarsText/{svarsText}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuestionsBySvarsText(@PathParam("svarsText") String svarsText) {
        List<FragaSvar> fragorOchSvar = fragasvarRepository.findBySvarsTextLike(svarsText);
        if (fragorOchSvar != null) {
            for (FragaSvar fragaSvar : fragorOchSvar) {
                fragasvarRepository.delete(fragaSvar);
            }
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuestion(@PathParam("id") Long id) {
        fragasvarRepository.delete(id);
        return Response.ok().build();
    }

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllQuestions() {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            public Response doInTransaction(TransactionStatus status) {
                @SuppressWarnings("unchecked")
                List<FragaSvar> fragorOchSvar = entityManager.createQuery("SELECT f FROM FragaSvar f").getResultList();
                for (FragaSvar fragaSvar : fragorOchSvar) {
                    entityManager.remove(fragaSvar);
                }
                return Response.ok().build();
            }
        });
    }
}
