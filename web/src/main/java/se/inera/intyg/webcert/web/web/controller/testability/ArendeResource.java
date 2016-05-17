/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import java.util.List;
import java.util.Map;

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

import io.swagger.annotations.Api;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;

@Transactional
@Api(value = "services arende", description = "REST API för testbarhet - Ärenden")
@Path("/arendetest")
public class ArendeResource {
    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTxManager(PlatformTransactionManager txManager) {
        this.transactionTemplate = new TransactionTemplate(txManager);
    }

    // @Autowired
    // private ArendeService arendeService;

    @Autowired
    private ArendeRepository arendeRepository;

    // @Autowired
    // private AuthoritiesResolver authoritiesResolver;

    @GET
    @Path("/ping")
    @Produces(MediaType.APPLICATION_XML)
    public Response getPing() {
        String xmlResponse = buildXMLResponse(true, 0, null);
        // LOGGER.debug("Pinged Intygstjänsten, got: " + xmlResponse);
        return Response.ok(xmlResponse).build();
    }

    private String buildXMLResponse(boolean ok, long time, Map<String, String> additionalValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("<pingdom_http_custom_check>");
        sb.append("<status>" + (ok ? "OK" : "FAIL") + "</status>");
        sb.append("<response_time>" + time + "</response_time>");
        if (additionalValues != null) {
            sb.append("<additional_data>");
            additionalValues.forEach((k, v) -> sb.append("<" + k + ">" + v + "</" + k + ">"));
            sb.append("</additional_data>");
        }
        sb.append("</pingdom_http_custom_check>");
        return sb.toString();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Arende getArende(@PathParam("id") Long id) {
        return arendeRepository.findOne(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertQuestion(Arende arende) {
        arendeRepository.save(arende);
        return Response.ok(arende).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuestion(@PathParam("id") String meddelandeId) {
        Arende arende = arendeRepository.findOneByMeddelandeId(meddelandeId);
        arendeRepository.delete(arende);
        return Response.ok().build();
    }

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllQuestions() {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            public Response doInTransaction(TransactionStatus status) {
                @SuppressWarnings("unchecked")
                List<Arende> arenden = entityManager.createQuery("SELECT f FROM Arende f").getResultList();
                for (Arende arende : arenden) {
                    entityManager.remove(arende);
                }
                return Response.ok().build();
            }
        });
    }
}
