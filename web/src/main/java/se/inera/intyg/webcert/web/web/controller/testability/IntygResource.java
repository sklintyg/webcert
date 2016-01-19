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

import io.swagger.annotations.Api;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.dto.Patient;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;
import se.inera.intyg.webcert.web.service.dto.Vardgivare;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygServiceConverter;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Api(value = "services intyg", description = "REST API för testbarhet - Utkast")
@Path("/intyg")
public class IntygResource {

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygServiceConverter intygServiceConverter;

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
        utkastRepository.delete(utkast);
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
    public Response signDraft(@PathParam("id") String id) {
        updateUtkastForSign(id);
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

    private void updateUtkastForSign(@PathParam("id") String id) {
        Utkast utkast = utkastRepository.findOne(id);
        if (utkast != null) {
            utkast.setStatus(UtkastStatus.SIGNED);
            Signatur sig = new Signatur(LocalDateTime.now(), "", id, "", "", "");
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
                // This should not be empty
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
}
