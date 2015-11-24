package se.inera.intyg.webcert.web.web.controller.testability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.CreateQuestionParameter;
/**
 * Bean for inserting questions directly into the database.
 * <p/>
 * Created by Pehr Assarsson on 9/24/13.
 */
@Transactional
@Api(value = "services questions", description = "REST API för testbarhet - Fråga/Svar")
@Path("/questions")
public class QuestionResource {

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTxManager(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    @Autowired
    private FragaSvarService fragaSvarService;

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

    @PUT
    @Path("/svara/{vardgivare}/{enhet}/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response answer(@PathParam("vardgivare") final String vardgivarId,
            @PathParam("enhet") final String enhetsId, @PathParam("id") final Long frageSvarId, String svarsText) {
        SecurityContext originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(getSecurityContext(vardgivarId, enhetsId));
        try {
            FragaSvar fragaSvarResponse = fragaSvarService.saveSvar(frageSvarId, svarsText);
            return Response.ok(fragaSvarResponse).build();
        } finally {
            SecurityContextHolder.setContext(originalContext);
        }
    }

    @POST
    @Path("/skickafraga/{vardgivare}/{enhet}/{intygId}/{typ}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response askQuestion(@PathParam("vardgivare") final String vardgivarId,
            @PathParam("enhet") final String enhetsId, @PathParam("intygId") final String intygId, @PathParam("typ") final String typ, CreateQuestionParameter parameter) {
        SecurityContext originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(getSecurityContext(vardgivarId, enhetsId));
        try {
            FragaSvar fragaSvarResponse =  fragaSvarService.saveNewQuestion(intygId, typ, parameter.getAmne(), parameter.getFrageText());
            return Response.ok(fragaSvarResponse).build();
        } finally {
            SecurityContextHolder.setContext(originalContext);
        }
    }

    @GET
    @Path("/extern/{externReferens}/translate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFragaSvarByExternReferens(@PathParam("externReferens") String externReferens) {
        return Response.ok(fragasvarRepository.findByExternReferens(externReferens)).build();
    }

    @DELETE
    @Path("/extern/{externReferens}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuestionByExternReferens(@PathParam("externReferens") String externReferens) {
        List<FragaSvar> fragor = fragasvarRepository.findByExternReferensLike(externReferens);
        for (FragaSvar fraga : fragor) {
            fragasvarRepository.delete(fraga);
        }
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
    @Path("/enhet/{enhetsId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuestionsByEnhet(@PathParam("enhetsId") String enhetsId) {
        List<String> enhetsIds = new ArrayList<String>();
        enhetsIds.add(enhetsId);
        List<FragaSvar> fragorOchSvar = fragasvarRepository.findByEnhetsId(enhetsIds);
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

    // Create a fake SecurityContext for a user which is authorized for the given care giver and unit
    @SuppressWarnings("serial")
    private SecurityContext getSecurityContext(final String vardgivarId, final String enhetsId) {
        final WebCertUser user = getWebCertUser(vardgivarId, enhetsId);
        return new SecurityContext() {
            @Override
            public void setAuthentication(Authentication authentication) {
            }

            @Override
            public Authentication getAuthentication() {
                return new Authentication() {
                    @Override
                    public Object getPrincipal() {
                        return user;
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return true;
                    }

                    @Override
                    public String getName() {
                        return "questionResource";
                    }

                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return null;
                    }

                    @Override
                    public Object getCredentials() {
                        return null;
                    }

                    @Override
                    public Object getDetails() {
                        return null;
                    }

                    @Override
                    public void setAuthenticated(boolean isAuthenticated) {
                    }
                };
            }
        };
    }

    // Create a fake WebCertUser which is authorized for the given care giver and unit
    private static WebCertUser getWebCertUser(String vardgivarId, String enhetsId) {
        WebCertUser user = new WebCertUser();

        user.setRoles(buildUserRoles(UserRole.ROLE_LAKARE));
        user.setAuthorities(new HashMap<String, UserPrivilege>());

        user.setHsaId("questionResource");
        user.setNamn("questionResource");
        user.setForskrivarkod("questionResource");

        List<Vardgivare> vardgivarList = new ArrayList<>();
        Vardgivare vardgivare = new Vardgivare(vardgivarId, "questionResource");
        List<Vardenhet> vardenheter = new ArrayList<>();
        Vardenhet enhet = new Vardenhet(enhetsId, "questionResource");
        vardenheter.add(enhet);
        vardgivare.setVardenheter(vardenheter);
        vardgivarList.add(vardgivare);

        user.setVardgivare(vardgivarList);
        user.setValdVardgivare(vardgivare);
        user.setValdVardenhet(enhet);

        return user;
    }

    private static Map<String, UserRole> buildUserRoles(UserRole... userRoles) {
        Map<String, UserRole> map = new HashMap<>();

        for (UserRole userRole : userRoles) {
            map.put(userRole.name(), userRole);
        }

        return map;
    }

}
