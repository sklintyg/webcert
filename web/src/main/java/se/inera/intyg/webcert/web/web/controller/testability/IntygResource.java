package se.inera.intyg.webcert.web.web.controller.testability;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.webcert.web.service.dto.Patient;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.dto.Vardenhet;
import se.inera.intyg.webcert.web.service.dto.Vardgivare;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;

@Transactional
@Api(value = "services intyg", description = "REST API för testbarhet - Utkast")
@Path("/intyg")
public class IntygResource {

    @Autowired
    private UtkastRepository utkastRepository;

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
        Utkast utkast = utkastRepository.findOne(id);
        if (utkast != null) {
            utkast.setStatus(UtkastStatus.DRAFT_COMPLETE);
            utkastRepository.save(utkast);
        }
        return Response.ok().build();
    }
}
