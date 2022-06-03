/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.web.controller.testability;

import io.swagger.annotations.Api;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.authorities.CommonAuthoritiesResolver;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.CreateQuestionParameter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Bean for inserting questions directly into the database.
 *
 * Created by Pehr Assarsson on 9/24/13.
 */
@Transactional
@Api(value = "services fragasvar", description = "REST API för testbarhet - Fråga/Svar")
@Path("/fragasvar")
public class FragaSvarResource {

    private static final Logger LOG = LoggerFactory.getLogger(FragaSvarResource.class);

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTxManager(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Autowired
    private FragaSvarService fragaSvarService;

    @Autowired
    private FragaSvarRepository fragasvarRepository;

    @Autowired
    private CommonAuthoritiesResolver authoritiesResolver;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertFragaSvar(FragaSvar fragaSvar) {
        FragaSvar saved = fragasvarRepository.save(fragaSvar);
        LOG.info("Created FragaSvar with id {} using testability API", saved.getInternReferens());
        return Response.ok(saved).build();
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
        @PathParam("enhet") final String enhetsId, @PathParam("intygId") final String intygId, @PathParam("typ") final String typ,
        CreateQuestionParameter parameter) {
        SecurityContext originalContext = SecurityContextHolder.getContext();
        SecurityContextHolder.setContext(getSecurityContext(vardgivarId, enhetsId));
        try {
            FragaSvar fragaSvarResponse = fragaSvarService.saveNewQuestion(intygId, typ, parameter.getAmne(), parameter.getFrageText());
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
    public Response deleteFragaSvarByExternReferens(@PathParam("externReferens") String externReferens) {
        List<FragaSvar> fragor = fragasvarRepository.findByExternReferensLike(externReferens);
        for (FragaSvar fraga : fragor) {
            fragasvarRepository.delete(fraga);
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/frageText/{frageText}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFragaSvarByFrageText(@PathParam("frageText") String frageText) {
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
    public Response deleteFragaSvarBySvarsText(@PathParam("svarsText") String svarsText) {
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
    public Response deleteFragaSvarByEnhet(@PathParam("enhetsId") String enhetsId) {
        List<String> enhetsIds = new ArrayList<>();
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
    public Response deleteFragaSvarById(@PathParam("id") Long id) {
        fragasvarRepository.deleteById(id);
        return Response.ok().build();
    }

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllFragaSvar() {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            @Override
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

    @GET
    @Path("/fragaSvarCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Long getFragaSvarCountCertificateIds(List<String> certificateIds) {
        final var fragaSvarList = fragasvarRepository.findAll();
        return fragaSvarList.stream().filter(fragaSvar -> certificateIds.contains(fragaSvar.getIntygsReferens().getIntygsId())).count();
    }

    @DELETE
    @Path("/byCertificateIds")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFragaSvarByCertificateIds(List<String> certificateIds) {
        final var fragaSvarList = fragasvarRepository.findAll();
        final var fragaSvarForDeletion = fragaSvarList.stream()
            .filter(fragaSvar -> certificateIds.contains(fragaSvar.getIntygsReferens().getIntygsId()))
            .collect(Collectors.toList());
        fragasvarRepository.deleteAll(fragaSvarForDeletion);

        return Response.ok().build();
    }

    // Create a fake SecurityContext for a user which is authorized for the given care giver and unit
    @SuppressWarnings("serial")
    private SecurityContext getSecurityContext(final String vardgivarId, final String enhetsId) {
        final WebCertUser user = getWebCertUser(vardgivarId, enhetsId);

        return new SecurityContext() {

            @Override
            public void setAuthentication(Authentication authentication) {
                // Do nothing
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
                        // Do nothing
                    }
                };
            }
        };
    }

    // Create a fake WebCertUser which is authorized for the given care giver and unit
    private WebCertUser getWebCertUser(String vardgivarId, String enhetsId) {
        WebCertUser user = new WebCertUser();

        Role role = authoritiesResolver.getRole(AuthoritiesConstants.ROLE_LAKARE);
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));

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

}
