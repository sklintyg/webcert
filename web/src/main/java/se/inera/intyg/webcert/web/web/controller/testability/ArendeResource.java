/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeDraft;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeDraftRepository;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.model.Status;
import se.inera.intyg.webcert.web.web.controller.testability.dto.ArendeAffectedResponse;
import se.inera.intyg.webcert.web.web.controller.testability.dto.SimpleArende;

@Transactional
@Api(value = "services arende", description = "REST API för testbarhet - Ärenden")
@Path("/arendetest")
public class ArendeResource {

    @PersistenceContext
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    public void setTxManager(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Autowired
    private ArendeRepository arendeRepository;

    @Autowired
    private ArendeDraftRepository arendeDraftRepository;

    @GET
    @Path("/intyg/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStalldaFragor(@PathParam("intygsId") String intygsId) {
        List<Arende> byIntygsId = arendeRepository.findByIntygsId(intygsId);
        return Response.ok(byIntygsId.stream()
                .filter(a -> a.getStatus() == Status.PENDING_EXTERNAL_ACTION)
                .map(a -> a.getMeddelandeId())
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * Returnerar ärenden på givet intygsId i status PENDING_INTERNAL_ACTION.
     *
     * Används av ärendeverktyget för att ge förslag på möjliga ärenden att skicka in en påminnelse för.
     */
    @GET
    @Path("/intyg/{intygsId}/internal")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVantarPaSvarFranOss(@PathParam("intygsId") String intygsId) {
        List<Arende> byIntygsId = arendeRepository.findByIntygsId(intygsId);
        return Response.ok(byIntygsId.stream()
                .filter(a -> a.getStatus() == Status.PENDING_INTERNAL_ACTION)
                .map(a -> new SimpleArende(a.getMeddelandeId(), a.getRubrik()))
                .collect(Collectors.toList()))
            .build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Arende getArende(@PathParam("id") Long id) {
        return arendeRepository.findById(id).orElse(null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertQuestion(Arende arende) {
        arende.setTimestamp(LocalDateTime.now());
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
            @Override
            public Response doInTransaction(TransactionStatus status) {
                @SuppressWarnings("unchecked")
                List<Arende> arenden = entityManager.createQuery("SELECT f FROM Arende f").getResultList();
                for (Arende arende : arenden) {
                    entityManager.remove(arende);
                }

                ArendeAffectedResponse affected = new ArendeAffectedResponse(arenden.size());

                return Response.ok(affected).build();
            }
        });
    }

    @DELETE
    @Path("/enhet/{enhetsId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllQuestionsOnUnit(@PathParam("enhetsId") String enhetsId) {
        return transactionTemplate.execute(new TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(TransactionStatus status) {
                @SuppressWarnings("unchecked")
                List<Arende> arenden = entityManager.createQuery("SELECT f FROM Arende f WHERE f.enhetId = :enhetId")
                    .setParameter("enhetId", enhetsId)
                    .getResultList();
                for (Arende arende : arenden) {
                    entityManager.remove(arende);
                }

                ArendeAffectedResponse affected = new ArendeAffectedResponse(arenden.size());
                return Response.ok(affected).build();
            }
        });
    }

    @GET
    @Path("/arendeCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Long getArendeCountForCertificateIds(List<String> certificateIds) {
        final var arenden = (List<Arende>) arendeRepository.findAll();
        return arenden.stream().filter(arende -> certificateIds.contains(arende.getIntygsId())).count();
    }

    @DELETE
    @Path("/arende")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteArendenForCertificateIds(List<String> certificateIds) {
        final var arenden = (List<Arende>) arendeRepository.findAll();
        final var arendenForDeletion = arenden.stream()
            .filter(arende -> certificateIds.contains(arende.getIntygsId()))
            .collect(Collectors.toList());
        arendeRepository.deleteAll(arendenForDeletion);

        return Response.ok().build();
    }

    @GET
    @Path("/arendeDraftCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Long getArendeDraftCountForCertificateIds(List<String> certificateIds) {
        final var arendeDrafts = (List<ArendeDraft>) arendeDraftRepository.findAll();
        return arendeDrafts.stream().filter(arendeDraft -> certificateIds.contains(arendeDraft.getIntygId())).count();
    }

    @DELETE
    @Path("/arendeDraft")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteArendeDraftsForCertificateIds(List<String> certificateIds) {
        final var arendeDrafts = (List<ArendeDraft>) arendeDraftRepository.findAll();
        final var arendeList = arendeDrafts.stream()
            .filter(arendeDraft -> certificateIds.contains(arendeDraft.getIntygId()))
            .collect(Collectors.toList());
        arendeDraftRepository.deleteAll(arendeList);

        return Response.ok().build();
    }

}
