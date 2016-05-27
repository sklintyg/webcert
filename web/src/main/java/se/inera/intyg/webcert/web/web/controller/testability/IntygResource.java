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

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.annotations.Api;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.utkast.model.*;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.dto.*;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygServiceConverter;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RelationItem;

@Transactional
@Api(value = "services intyg", description = "REST API för testbarhet - Utkast")
@Path("/intyg")
public class IntygResource {

    public static final Logger LOG = LoggerFactory.getLogger(IntygResource.class);

    protected static final String UTF_8_CHARSET = ";charset=utf-8";
    protected static final String UTF_8 = "UTF-8";

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private ArendeRepository arendeRepository;

    @Autowired
    private IntygServiceConverter intygServiceConverter;

    @Autowired
    private IntygModuleRegistryImpl moduleRegistry;

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllDrafts() {
        // Need deleteAll here, deleteAllInBatch doesn't apply cascade delete
        utkastRepository.deleteAll();
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDraft(@PathParam("id") String id) {
        Utkast utkast = utkastRepository.findOne(id);
        if(utkast != null)
        {
            List<Arende> arenden = arendeRepository.findByIntygsId(utkast.getIntygsId());
            if (arenden != null) {
                for (Arende u : arenden) {
                    arendeRepository.delete(u);
                }
            }
            utkastRepository.delete(utkast);
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/enhet/{enhetsId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDraftsByEnhet(@PathParam("enhetsId") String enhetsId) {
        List<String> enhetsIds = new ArrayList<String>();
        enhetsIds.add(enhetsId);
        List<UtkastStatus> statuses = new ArrayList<UtkastStatus>();
        statuses.add(UtkastStatus.DRAFT_INCOMPLETE);
        statuses.add(UtkastStatus.DRAFT_COMPLETE);
        List<Utkast> utkast = utkastRepository.findByEnhetsIdsAndStatuses(enhetsIds, statuses);
        if (utkast != null) {
            for (Utkast u : utkast) {
                utkastRepository.delete(u);
            }
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/utkast")
    public Response insertUtkast(IntygContentWrapper intygContents) throws ModuleNotFoundException, IOException {
        String intygsTyp = intygContents.getContents().get("typ").textValue();

        String model = intygContents.getContents().toString();
        ModuleApi moduleApi = moduleRegistry.getModuleApi(intygsTyp);
        Utlatande utlatande = moduleApi.getUtlatandeFromJson(model);
        Utkast utkast = new Utkast();

        utkast.setModel(model);

        utkast.setEnhetsId(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid());
        utkast.setEnhetsNamn(utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsnamn());
        utkast.setVardgivarId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        utkast.setIntygsTyp(utlatande.getTyp());
        utkast.setIntygsId(utlatande.getId());
        utkast.setPatientEfternamn(utlatande.getGrundData().getPatient().getEfternamn());
        utkast.setPatientFornamn(utlatande.getGrundData().getPatient().getFornamn());
        utkast.setPatientPersonnummer(utlatande.getGrundData().getPatient().getPersonId());

        if (utlatande.getGrundData().getRelation() != null && utlatande.getGrundData().getRelation().getRelationIntygsId() != null) {
            if (utlatande.getId() != null && utlatande.getId().equals(utlatande.getGrundData().getRelation().getRelationIntygsId())) {
                LOG.error("Utkast relation to itself is invalid.");
            } else {
                utkast.setRelationIntygsId(utlatande.getGrundData().getRelation().getRelationIntygsId());
                utkast.setRelationKod(utlatande.getGrundData().getRelation().getRelationKod());
            }
        }

        utkast.setStatus(intygContents.getUtkastStatus());
        utkast.setVidarebefordrad(false);
        if (utkast.getStatus() == UtkastStatus.SIGNED) {
            Signatur signatur = new Signatur(LocalDateTime.now(), utlatande.getGrundData().getSkapadAv().getPersonId(), utlatande.getId(), model, "ruffel",
                    "fusk");
            utkast.setSignatur(signatur);
        }
        VardpersonReferens vardpersonReferens = new VardpersonReferens();
        vardpersonReferens.setHsaId(utlatande.getGrundData().getSkapadAv().getPersonId());
        vardpersonReferens.setNamn(utlatande.getGrundData().getSkapadAv().getFullstandigtNamn());
        utkast.setSkapadAv(vardpersonReferens);
        utkast.setSenastSparadAv(vardpersonReferens);
        utkastRepository.save(utkast);
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDraft(CreateNewDraftRequest request) {
        Utkast utkast = new Utkast();

        Patient patient = request.getPatient();

        utkast.setPatientPersonnummer(patient.getPersonnummer());
        utkast.setPatientFornamn(patient.getFornamn());
        utkast.setPatientMellannamn(patient.getMellannamn());
        utkast.setPatientEfternamn(patient.getEfternamn());

        utkast.setIntygsId(request.getIntygId());
        utkast.setIntygsTyp(request.getIntygType());

        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);

        Vardenhet vardenhet = request.getVardenhet();

        utkast.setEnhetsId(vardenhet.getHsaId());
        utkast.setEnhetsNamn(vardenhet.getNamn());

        Vardgivare vardgivare = vardenhet.getVardgivare();

        utkast.setVardgivarId(vardgivare.getHsaId());
        utkast.setVardgivarNamn(vardgivare.getNamn());

        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(request.getHosPerson().getNamn());
        vardPerson.setHsaId(request.getHosPerson().getHsaId());

        utkast.setSenastSparadAv(vardPerson);
        utkast.setSkapadAv(vardPerson);

        utkastRepository.save(utkast);

        return Response.ok().build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDraft(@PathParam("id") String id, String model) {
        Utkast utkast = utkastRepository.findOne(id);
        if (utkast != null) {
            utkast.setModel(model);
            utkastRepository.save(utkast);
        }
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}/komplett")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDraft(@PathParam("id") String id) {
        updateStatus(id, UtkastStatus.DRAFT_COMPLETE);
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}/signerat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response signDraft(@PathParam("id") String id, String signeratAv) {
        updateUtkastForSign(id, signeratAv);
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}/skickat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendDraft(@PathParam("id") String id) {
        updateUtkastForSend(id);
        return Response.ok().build();
    }

    private void updateStatus(String id, UtkastStatus status) {
        Utkast utkast = utkastRepository.findOne(id);
        if (utkast != null) {
            utkast.setStatus(status);
            utkastRepository.save(utkast);
        }
    }

    private void updateUtkastForSign(@PathParam("id") String id, String signeratAv) {
        Utkast utkast = utkastRepository.findOne(id);
        if (utkast != null) {
            utkast.setStatus(UtkastStatus.SIGNED);
            Signatur sig = new Signatur(LocalDateTime.now(), signeratAv != null ? signeratAv : "", id, "", "", "");
            utkast.setSignatur(sig);
            utkastRepository.save(utkast);
        }
    }

    private void updateUtkastForSend(@PathParam("id") String id) {
        Utkast utkast = utkastRepository.findOne(id);
        if (utkast != null) {
            utkast.setStatus(UtkastStatus.SIGNED);
            Utlatande utlatande = intygServiceConverter.buildUtlatandeFromUtkastModel(utkast);
            utlatande.getGrundData().setSigneringsdatum(LocalDateTime.now());
            try {
                CustomObjectMapper mapper = new CustomObjectMapper();
                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, utlatande);
                utkast.setModel(writer.toString());
            } catch (IOException e) {
                LOG.error("Could not update the model of the utkast. Failed with message ", e.getMessage());
            }

            if (utkast.getSignatur() == null) {
                Signatur sig = new Signatur(LocalDateTime.now(), "", id, "", "", "");
                utkast.setSignatur(sig);
            }

            utkast.setSkickadTillMottagare("FK");
            utkast.setSkickadTillMottagareDatum(LocalDateTime.now());

            utkastRepository.save(utkast);
        }
    }

    static class IntygContentWrapper {
        private JsonNode contents;
        private boolean revoked;
        private UtkastStatus utkastStatus;
        private List<RelationItem> relations;

        IntygContentWrapper(JsonNode contents, boolean revoked, UtkastStatus utkastStatus, Optional<List<RelationItem>> relations) {
            this.contents = contents;
            this.revoked = revoked;
            this.utkastStatus = utkastStatus;
            this.relations = relations.orElse(new ArrayList<>());
        }

        IntygContentWrapper() {
        }

        JsonNode getContents() {
            return contents;
        }

        void setContents(JsonNode contents) {
             this.contents = contents;
        }

        boolean isRevoked() {
            return revoked;
        }

        UtkastStatus getUtkastStatus() {
            return utkastStatus;
        }

        void setUtkastStatus(UtkastStatus utkastStatus) {
            this.utkastStatus = utkastStatus;
        }

        List<RelationItem> getRelations() {
            return relations;
        }

        void setRelations(List<RelationItem> relations) {
            this.relations = relations;
        }
    }
}
