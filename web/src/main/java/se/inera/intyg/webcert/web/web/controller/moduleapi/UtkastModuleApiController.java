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

package se.inera.intyg.webcert.web.web.controller.moduleapi;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import io.swagger.annotations.Api;
import se.inera.intyg.common.security.common.model.AuthoritiesConstants;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.relation.RelationService;
import se.inera.intyg.webcert.web.service.signatur.SignaturService;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.signatur.grp.GrpSignaturService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidation;
import se.inera.intyg.webcert.web.service.utkast.dto.DraftValidationMessage;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveAndValidateDraftRequest;
import se.inera.intyg.webcert.web.service.utkast.dto.SaveAndValidateDraftResponse;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftHolder;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.DraftValidationStatus;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.RelationItem;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.SaveDraftResponse;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.SignaturTicketResponse;

/**
 * Controller for module interaction with drafts.
 *
 * @author npet
 *
 */
@Path("/utkast")
@Api(value = "utkast", description = "REST API - moduleapi - utkast", produces = MediaType.APPLICATION_JSON)
public class UtkastModuleApiController extends AbstractApiController {

    public static final String LAST_SAVED_DRAFT = "lastSavedDraft";

    private static final Logger LOG = LoggerFactory.getLogger(UtkastModuleApiController.class);

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private SignaturService signaturService;

    @Autowired
    private GrpSignaturService grpSignaturService;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private IntygTextsService intygTextsService;

    @Autowired
    private RelationService relationService;

    /**
     * Returns the draft certificate as JSON identified by the intygId.
     *
     * @param intygsId
     *            The id of the certificate
     * @return a JSON object
     */
    @GET
    @Path("/{intygsTyp}/{intygsId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response getDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @DefaultValue("false") @QueryParam("sjf") boolean coherentJournaling, @Context HttpServletRequest request) {

        LOG.debug("Retrieving Intyg with id {} and type {}, coherent journaling: {}", intygsId, intygsTyp, coherentJournaling);

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(WebcertFeature.HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        Utkast utkast = utkastService.getDraft(intygsId, coherentJournaling);

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        DraftHolder draftHolder = new DraftHolder();
        draftHolder.setVersion(utkast.getVersion());
        draftHolder.setVidarebefordrad(utkast.getVidarebefordrad());
        draftHolder.setStatus(utkast.getStatus());
        draftHolder.setEnhetsNamn(utkast.getEnhetsNamn());
        draftHolder.setVardgivareNamn(utkast.getVardgivarNamn());
        draftHolder.setContent(utkast.getModel());
        draftHolder.setLatestTextVersion(intygTextsService.getLatestVersion(utkast.getIntygsTyp()));
        draftHolder.getRelations().addAll(relationService.getRelations(utkast.getIntygsId())
                .orElse(RelationItem.createBaseCase(utkast.getIntygsId(), utkast.getSenastSparadDatum(), RelationItem.UTKAST)));

        return Response.ok(draftHolder).build();
    }

    /**
     * Persists the supplied draft certificate using the intygId as key.
     *
     * @param intygsId
     *            The id of the certificate.
     * @param payload
     *            Object holding the certificate and its current status.
     */
    @PUT
    @Path("/{intygsTyp}/{intygsId}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response saveDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId, @PathParam("version") long version,
            @DefaultValue("false") @QueryParam("autoSave") boolean autoSave, byte[] payload, @Context HttpServletRequest request) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(WebcertFeature.HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        LOG.debug("Saving utkast with id '{}', autosave is {}", intygsId, autoSave);

        String draftAsJson = fromBytesToString(payload);

        LOG.debug("---- intyg : " + draftAsJson);

        SaveAndValidateDraftRequest serviceRequest = createSaveAndValidateDraftRequest(intygsId, version, draftAsJson, autoSave);

        boolean firstSave = false;
        HttpSession session = request.getSession(true);
        String lastSavedDraft = (String) session.getAttribute(LAST_SAVED_DRAFT);
        if (!intygsId.equals(lastSavedDraft)) {
            firstSave = true;
        }
        session.setAttribute(LAST_SAVED_DRAFT, intygsId);

        try {
            SaveAndValidateDraftResponse validateResponse = utkastService.saveAndValidateDraft(serviceRequest, firstSave);

            SaveDraftResponse responseEntity = buildSaveDraftResponse(validateResponse.getVersion(), validateResponse.getDraftValidation());

            return Response.ok().entity(responseEntity).build();
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }
    }

    private SaveAndValidateDraftRequest createSaveAndValidateDraftRequest(String intygId, long version, String draftAsJson, Boolean autoSave) {
        SaveAndValidateDraftRequest request = new SaveAndValidateDraftRequest();

        request.setIntygId(intygId);
        request.setVersion(version);
        request.setDraftAsJson(draftAsJson);
        request.setAutoSave(autoSave);

        return request;
    }

    private SaveDraftResponse buildSaveDraftResponse(long version, DraftValidation draftValidation) {

        if (draftValidation.isDraftValid()) {
            return new SaveDraftResponse(version, DraftValidationStatus.COMPLETE);
        }

        SaveDraftResponse responseEntity = new SaveDraftResponse(version, DraftValidationStatus.INCOMPLETE);

        List<DraftValidationMessage> validationMessages = draftValidation.getMessages();

        for (DraftValidationMessage validationMessage : validationMessages) {
            responseEntity.addMessage(validationMessage.getField(), validationMessage.getType(), validationMessage.getMessage());
        }

        return responseEntity;
    }

    private String fromBytesToString(byte[] bytes) {
        try {
            return new String(bytes, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not convert the payload from draftCertificate to String!", e);
        }
    }

    /**
     * Deletes a draft certificate identified by the certificateId.
     *
     * @param intygsId
     *            The id of the certificate
     */
    @DELETE
    @Path("/{intygsTyp}/{intygsId}/{version}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response discardDraft(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @PathParam("version") long version,
            @Context HttpServletRequest request) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(WebcertFeature.HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        LOG.debug("Deleting draft with id {}", intygsId);

        try {
            utkastService.deleteUnsignedDraft(intygsId, version);
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        return Response.ok().build();
    }

    /**
     * Creates a PDL log event that a persons draft has been printed.
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/loggautskrift")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public Response logPrintOfDraftToPDL(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId) {

        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp).features(WebcertFeature.HANTERA_INTYGSUTKAST).orThrow();

        LOG.debug("Logging printout of draft intyg '{}'", intygsId);

        utkastService.logPrintOfDraftToPDL(intygsId);

        return Response.ok().build();
    }

    /**
     * Signera utkast.
     *
     * @param intygsId
     *            intyg id
     * @return SignaturTicketResponse
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/{version}/signeraserver")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturTicketResponse serverSigneraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @PathParam("version") long version, @Context HttpServletRequest request) {
        verifyIsAuthorizedToSignIntyg(intygsTyp);
        SignaturTicket ticket;
        try {
            ticket = signaturService.serverSignature(intygsId, version);
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        return new SignaturTicketResponse(ticket);
    }

    private void verifyIsAuthorizedToSignIntyg(String intygsTyp) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp)
                .features(WebcertFeature.HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG)
                .orThrow();
    }

    /**
     * Signera utkast mha Bank ID GRP API.
     *
     * @param intygsId
     *            intyg id
     * @return SignaturTicketResponse
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/{version}/grp/signeraserver")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturTicketResponse serverSigneraUtkastMedGrp(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @PathParam("version") long version, @Context HttpServletRequest request) {

        verifyIsAuthorizedToSignIntyg(intygsTyp);

        SignaturTicket ticket;
        try {
            ticket = grpSignaturService.startGrpAuthentication(intygsId, version);
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        return new SignaturTicketResponse(ticket);
    }

    /**
     * Signera utkast.
     *
     * @param biljettId
     *            biljett id
     * @return BiljettResponse
     */
    @POST
    @Path("/{intygsTyp}/{biljettId}/signeraklient")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM })
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturTicketResponse klientSigneraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("biljettId") String biljettId,
            @Context HttpServletRequest request, byte[] rawSignatur) {

        verifyIsAuthorizedToSignIntyg(intygsTyp);

        LOG.debug("Signerar intyg med biljettId {}", biljettId);

        if (rawSignatur.length == 0) {
            LOG.error("Inkommande signatur parameter saknas");
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "Signatur saknas");
        }

        String rawSignaturString = fromBytesToString(rawSignatur);
        SignaturTicket ticket;
        try {
            ticket = signaturService.clientSignature(biljettId, rawSignaturString);
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            ticket = signaturService.ticketStatus(biljettId);
            monitoringLogService.logUtkastConcurrentlyEdited(ticket.getIntygsId(), intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }

        request.getSession(true).removeAttribute(LAST_SAVED_DRAFT);

        return new SignaturTicketResponse(ticket);
    }

    /**
     * Skapa signeringshash.
     *
     * @param intygsId
     *            intyg id
     * @return SignaturTicketResponse
     */
    @POST
    @Path("/{intygsTyp}/{intygsId}/{version}/signeringshash")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturTicketResponse signeraUtkast(@PathParam("intygsTyp") String intygsTyp, @PathParam("intygsId") String intygsId,
            @PathParam("version") long version) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp).features(WebcertFeature.HANTERA_INTYGSUTKAST).orThrow();

        SignaturTicket ticket;
        try {
            ticket = signaturService.createDraftHash(intygsId, version);
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            monitoringLogService.logUtkastConcurrentlyEdited(intygsId, intygsTyp);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION, e.getMessage());
        }
        return new SignaturTicketResponse(ticket);
    }

    /**
     * Hamta signeringsstatus.
     *
     * @param biljettId
     *            biljett id
     * @return SignaturTicketResponse
     */
    @GET
    @Path("/{intygsTyp}/{biljettId}/signeringsstatus")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public SignaturTicketResponse biljettStatus(@PathParam("intygsTyp") String intygsTyp, @PathParam("biljettId") String biljettId) {
        authoritiesValidator.given(getWebCertUserService().getUser(), intygsTyp).features(WebcertFeature.HANTERA_INTYGSUTKAST).orThrow();

        SignaturTicket ticket = signaturService.ticketStatus(biljettId);
        return new SignaturTicketResponse(ticket);
    }
}
