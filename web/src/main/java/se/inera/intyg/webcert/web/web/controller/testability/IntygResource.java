/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import com.fasterxml.jackson.databind.JsonNode;
import com.secmaker.netid.nias.v1.ResultCollect;
import com.secmaker.netid.nias.v1.SignResponse;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModule;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.repository.ArendeRepository;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.repository.FragaSvarRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.signatur.SignaturTicketTracker;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.signatur.nias.NiasSignaturService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.testability.dto.SigningUnit;

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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private FragaSvarRepository fragaSvarRepository;

    @Autowired
    private ArendeRepository arendeRepository;

    @Autowired
    private IntygModuleFacade moduleFacade;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private SignaturTicketTracker signaturTicketTracker;

    @Autowired
    private NiasSignaturService niasSignaturService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    /**
     * This method is not very safe nor accurate - it parses the [intygsTyp].sch file using XPath and tries
     * to assemble a list of "frageId"'s. It used for the Arendeverktyg testing tool and is _not_ meant to be
     * used in production code.
     *
     * @param intygsTyp
     *            SIT-intyg: luae_fs, luae_na, luse, lisjp
     * @return
     */
    @GET
    @Path("/questions/{intygsTyp}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllFragorFromConstants(@PathParam("intygsTyp") String intygsTyp) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // Load schematron file from classpath.
            Resource resource = resourceLoader.getResource("classpath:" + intygsTyp + ".sch");
            Document document = builder.parse(resource.getInputStream());

            XPath xpath = XPathFactory.newInstance().newXPath();
            String expression = "/schema/pattern";
            NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

            List<String> qList = new ArrayList<>();
            for (int a = 0; a < nodes.getLength(); a++) {
                Node item = nodes.item(a);
                String idAttr = item.getAttributes().getNamedItem("id").getNodeValue();
                if (idAttr.startsWith("q") && !idAttr.contains("-") && !idAttr.contains(".")) {
                    String frageId = idAttr.substring(1);
                    if ("9000".equals(frageId)) {
                        qList.add("9001");
                        qList.add("9002");
                    } else {
                        qList.add(frageId);
                    }
                }
            }
            return Response.ok(qList).build();
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    /**
     * Returns a List of {@link SigningUnit} of vardenheter having at least one signed and sent intyg.
     *
     * @return
     */
    @GET
    @Path("/signingunits")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSigningUnits() {
        return Response.ok(utkastRepository.findAllUnitsWithSentCertificate()
                .stream()
                .map(arr -> new SigningUnit((String) arr[0], (String) arr[1]))
                .collect(Collectors.toList())).build();
    }

    /**
     * Returns all signed and sent Intyg (based on the Utkast table) for the specified enhetsId.
     *
     * @param enhetsId
     * @return
     */
    @GET
    @Path("/{enhetsId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSignedAndSentIntygOnUnit(@PathParam("enhetsId") String enhetsId) {
        List<Utkast> all = utkastRepository.findByEnhetsIdsAndStatuses(Arrays.asList(enhetsId), Arrays.asList(UtkastStatus.SIGNED));
        return Response.ok(all.stream()
                .filter(utkast -> utkast.getSkickadTillMottagareDatum() != null && utkast.getSignatur() != null)
                .sorted((u1, u2) -> u2.getSignatur().getSigneringsDatum().compareTo(u1.getSignatur().getSigneringsDatum()))
                .collect(Collectors.toList())).build();
    }

    /**
     * Returns all complete drafts for the specified enhetsId.
     *
     * @param enhetsId
     * @return
     */
    @GET
    @Path("/{enhetsId}/drafts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCompleteDradtsOnUnit(@PathParam("enhetsId") String enhetsId) {
        List<Utkast> all = utkastRepository.findByEnhetsIdsAndStatuses(Arrays.asList(enhetsId), Arrays.asList(UtkastStatus.DRAFT_COMPLETE));
        return Response.ok(all.stream()
                .collect(Collectors.toList())).build();
    }

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllDrafts() {
        // Need deleteAll here, deleteAllInBatch doesn't apply cascade delete
        utkastRepository.deleteAll();
        fragaSvarRepository.deleteAll();
        arendeRepository.deleteAll();
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDraft(@PathParam("id") String id) {
        Utkast utkast = utkastRepository.findOne(id);
        deleteDraftAndRelatedQAs(utkast);
        return Response.ok().build();
    }

    @DELETE
    @Path("/enhet/{enhetsId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDraftsByEnhet(@PathParam("enhetsId") String enhetsId) {
        List<String> enhetsIds = new ArrayList<>();
        enhetsIds.add(enhetsId);
        List<UtkastStatus> statuses = new ArrayList<>();
        statuses.add(UtkastStatus.DRAFT_INCOMPLETE);
        statuses.add(UtkastStatus.DRAFT_COMPLETE);
        List<Utkast> utkast = utkastRepository.findByEnhetsIdsAndStatuses(enhetsIds, statuses);
        if (utkast != null) {
            for (Utkast u : utkast) {
                deleteDraftAndRelatedQAs(u);
            }
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/patient/{patientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDraftsByPatient(@PathParam("patientId") String patientId) {
        Set<String> intygTyper = moduleRegistry.listAllModules().stream()
                .map(IntygModule::getId).collect(Collectors.toSet());
        List<Utkast> utkast = utkastRepository.findAllByPatientPersonnummerAndIntygsTypIn(patientId, intygTyper);
        if (utkast != null) {
            for (Utkast u : utkast) {
                deleteDraftAndRelatedQAs(u);
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
        Utlatande utlatande = moduleFacade.getUtlatandeFromInternalModel(intygsTyp, model);
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
            Signatur signatur = new Signatur(LocalDateTime.now(), utlatande.getGrundData().getSkapadAv().getPersonId(), utlatande.getId(),
                    model,
                    "ruffel", "fusk");
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

        utkast.setPatientPersonnummer(patient.getPersonId());
        utkast.setPatientFornamn(patient.getFornamn());
        utkast.setPatientMellannamn(patient.getMellannamn());
        utkast.setPatientEfternamn(patient.getEfternamn());

        utkast.setIntygsId(request.getIntygId());
        utkast.setIntygsTyp(request.getIntygType());

        utkast.setStatus(UtkastStatus.DRAFT_INCOMPLETE);

        Vardenhet vardenhet = request.getHosPerson().getVardenhet();

        utkast.setEnhetsId(vardenhet.getEnhetsid());
        utkast.setEnhetsNamn(vardenhet.getEnhetsnamn());

        Vardgivare vardgivare = vardenhet.getVardgivare();

        utkast.setVardgivarId(vardgivare.getVardgivarid());
        utkast.setVardgivarNamn(vardgivare.getVardgivarnamn());

        VardpersonReferens vardPerson = new VardpersonReferens();
        vardPerson.setNamn(request.getHosPerson().getFullstandigtNamn());
        vardPerson.setHsaId(request.getHosPerson().getPersonId());

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
    @Path("/{id}/kompletterarintyg")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setKompletteringRelation(@PathParam("id") String utkastId, String relatedToIntygId) {
        setRelationToKompletterandeIntyg(utkastId, relatedToIntygId);
        return Response.ok().build();
    }

    @PUT
    @Path("/{id}/skickat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendDraft(@PathParam("id") String id) {
        updateUtkastForSend(id);
        return Response.ok().build();
    }

    @GET
    @Path("/ticket/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSigningTicket(@PathParam("id") String id) {
        SignaturTicket ticket = signaturTicketTracker.getTicket(id);
        return Response.ok(ticket).build();
    }

    @GET
    @Path("/nias/sign/{personId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response netiDSign(@PathParam("personId") String personId) {
        SignResponse signResponse = niasSignaturService.sign(personId, "", "", "");
        return Response.ok(signResponse.getSignResult()).build();
    }

    @GET
    @Path("/nias/collect/{orderRef}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response netiDCollect(@PathParam("orderRef") String orderRef) {
        ResultCollect resultCollect = niasSignaturService.collect(orderRef);
        return Response.ok(resultCollect).build();
    }

    private void setRelationToKompletterandeIntyg(String id, String oldIntygId) {
        Utkast utkast = utkastRepository.findOne(id);
        Utkast relatedUtkast = utkastRepository.findOne(id);
        if (utkast != null
                && relatedUtkast != null
                && relatedUtkast.getSignatur() != null
                && relatedUtkast.getSkickadTillMottagareDatum() != null) {

            utkast.setRelationIntygsId(oldIntygId);
            utkast.setRelationKod(RelationKod.KOMPLT);
            utkastRepository.saveAndFlush(utkast);
        }
    }

    private void deleteDraftAndRelatedQAs(Utkast utkast) {
        if (utkast != null) {
            List<Arende> arenden = arendeRepository.findByIntygsId(utkast.getIntygsId());
            if (arenden != null) {
                arenden.stream().forEach(a -> arendeRepository.delete(a));
            }
            List<FragaSvar> fragaSvarList = fragaSvarRepository.findByIntygsReferensIntygsId(utkast.getIntygsId());
            if (fragaSvarList != null) {
                fragaSvarList.stream().forEach(fs -> fragaSvarRepository.delete(fs));
            }
            utkastRepository.delete(utkast);
        }
    }

    private void updateStatus(String id, UtkastStatus status) {
        Utkast utkast = utkastRepository.findOne(id);
        if (utkast != null) {
            utkast.setStatus(status);
            utkastRepository.save(utkast);
        }
    }

    private void updateUtkastForSign(String id, String signeratAv) {
        Utkast utkast = utkastRepository.findOne(id);
        if (utkast != null) {
            utkast.setStatus(UtkastStatus.SIGNED);
            Signatur sig = new Signatur(LocalDateTime.now(), signeratAv != null ? signeratAv : "", id, "", "", "");
            utkast.setSignatur(sig);
            utkastRepository.save(utkast);
        }
    }

    private void updateUtkastForSend(String id) {
        Utkast utkast = utkastRepository.findOne(id);
        if (utkast != null) {
            utkast.setStatus(UtkastStatus.SIGNED);
            Utlatande utlatande = moduleFacade.getUtlatandeFromInternalModel(utkast.getIntygsTyp(), utkast.getModel());
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

            utkast.setSkickadTillMottagare("FKASSA");
            utkast.setSkickadTillMottagareDatum(LocalDateTime.now());

            utkastRepository.save(utkast);
        }
    }

    static class IntygContentWrapper {
        private JsonNode contents;
        private boolean revoked;
        private UtkastStatus utkastStatus;
        private Relations relations;

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

        public Relations getRelations() {
            return relations;
        }

        public void setRelations(Relations relations) {
            this.relations = relations;
        }
    }
}
