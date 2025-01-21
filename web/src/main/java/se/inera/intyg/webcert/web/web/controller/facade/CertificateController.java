/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.facade;

import static se.inera.intyg.webcert.web.web.controller.moduleapi.UtkastModuleApiController.LAST_SAVED_DRAFT;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.facade.ComplementCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CopyCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFromCandidateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFromTemplateFacadeService;
import se.inera.intyg.webcert.web.service.facade.DeleteCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ForwardCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCandidateMesssageForCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateEventsFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateResourceLinks;
import se.inera.intyg.webcert.web.service.facade.GetRelatedCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReadyForSignFacadeService;
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReplaceCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.RevokeCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SaveCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SendCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SignCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ValidateCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.impl.CreateCertificateException;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateEventResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ComplementCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CopyCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateFromCandidateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateFromTemplateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CreateCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ForwardCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.GetRelatedCertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.NewCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RenewCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ReplaceCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SendCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;

@Path("/certificate")
public class CertificateController {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    @Autowired
    @Qualifier("getCertificateAggregator")
    private GetCertificateFacadeService getCertificateFacadeService;
    @Autowired
    @Qualifier("saveCertificateAggregator")
    private SaveCertificateFacadeService saveCertificateFacadeService;
    @Autowired
    @Qualifier("validateCertificateAggregator")
    private ValidateCertificateFacadeService validationCertificateFacadeService;
    @Autowired
    private SignCertificateFacadeService signCertificateFacadeService;
    @Autowired
    @Qualifier("deleteCertificateAggregator")
    private DeleteCertificateFacadeService deleteCertificateFacadeService;
    @Autowired
    @Qualifier("revokeCertificateAggregator")
    private RevokeCertificateFacadeService revokeCertificateFacadeService;
    @Autowired
    @Qualifier("replaceCertificateAggregator")
    private ReplaceCertificateFacadeService replaceCertificateFacadeService;
    @Autowired
    private CopyCertificateFacadeService copyCertificateFacadeService;
    @Autowired
    @Qualifier("renewCertificateAggregator")
    private RenewCertificateFacadeService renewCertificateFacadeService;
    @Autowired
    @Qualifier("forwardCertificateAggregator")
    private ForwardCertificateFacadeService forwardCertificateFacadeService;
    @Autowired
    private ReadyForSignFacadeService readyForSignAggregator;
    @Autowired
    @Qualifier("getCertificateEventsAggregator")
    private GetCertificateEventsFacadeService getCertificateEventsFacadeService;
    @Autowired
    private GetCertificateResourceLinks getCertificateResourceLinks;
    @Autowired
    @Qualifier("sendCertificateAggregator")
    private SendCertificateFacadeService sendCertificateFacadeService;
    @Autowired
    @Qualifier("complementCertificateAggregator")
    private ComplementCertificateFacadeService complementCertificateFacadeService;
    @Autowired
    private CreateCertificateFromTemplateFacadeService createCertificateFromTemplateFacadeService;
    @Autowired
    private CreateCertificateFromCandidateFacadeService createCertificateFromCandidateFacadeService;
    @Autowired
    @Qualifier("createCertificateAggregator")
    private CreateCertificateFacadeService createCertificateFacadeService;

    @Autowired
    private GetRelatedCertificateFacadeService getRelatedCertificateFacadeService;

    @Autowired
    private GetCandidateMesssageForCertificateFacadeService getCandidateMesssageForCertificateFacadeService;

    @GET
    @Path("/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-get-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getCertificate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting certificate with id: '{}'", certificateId);
        }
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, true, true);
        final var resourceLinks = getCertificateResourceLinks.get(certificate);
        final var certificateDTO = CertificateDTO.create(certificate, resourceLinks);
        return Response.ok(CertificateResponseDTO.create(certificateDTO)).build();
    }

    @PUT
    @Path("/{certificateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-save-certificate", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response saveCertificate(@PathParam("certificateId") String certificateId, @RequestBody @NotNull Certificate certificate,
        @Context HttpServletRequest request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving certificate with id: '{}'", certificate.getMetadata().getId());
        }
        final var pdlLog = isFirstTimeSavedDuringSession(certificateId, request);
        final var version = saveCertificateFacadeService.saveCertificate(certificate, pdlLog);
        return Response.ok(SaveCertificateResponseDTO.create(version)).build();
    }

    private boolean isFirstTimeSavedDuringSession(String certificateId, HttpServletRequest request) {
        final var session = request.getSession(true);
        final var lastSavedDraft = (String) session.getAttribute(LAST_SAVED_DRAFT);
        session.setAttribute(LAST_SAVED_DRAFT, certificateId);
        return !certificateId.equals(lastSavedDraft);
    }

    @POST
    @Path("/{certificateId}/validate")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-validate-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response validateCertificate(@PathParam("certificateId") String certificateId, @RequestBody @NotNull Certificate certificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Validating certificate with id: '{}'", certificateId);
        }

        final var validationErrors = validationCertificateFacadeService.validate(certificate);
        return Response.ok(ValidateCertificateResponseDTO.create(validationErrors)).build();
    }

    @POST
    @Path("/{certificateId}/sign")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-sign-certificate", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response signCertificate(@PathParam("certificateId") String certificateId, @RequestBody @NotNull Certificate certificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signing certificate with id: '{}'", certificateId);
        }
        final var signedCertificate = signCertificateFacadeService.signCertificate(certificate);
        final var resourceLinks = getCertificateResourceLinks.get(signedCertificate);
        final var certificateDTO = CertificateDTO.create(signedCertificate, resourceLinks);
        return Response.ok(CertificateResponseDTO.create(certificateDTO)).build();
    }

    @DELETE
    @Path("/{certificateId}/{version}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-delete-certificate", eventType = MdcLogConstants.EVENT_TYPE_DELETION)
    public Response deleteCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @PathParam("version") @NotNull long version) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting certificate with id: '{}' and version: '{}'", certificateId, version);
        }
        deleteCertificateFacadeService.deleteCertificate(certificateId, version);
        return Response.ok().build();
    }

    @POST
    @Path("/{certificateId}/revoke")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-revoke-certificate", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response revokeCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull RevokeCertificateRequestDTO revokeCertificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Revoking certificate with id: '{}' and reason: '{}' and message: '{}'",
                certificateId,
                revokeCertificate.getReason(),
                revokeCertificate.getMessage()
            );
        }
        final var certificate = revokeCertificateFacadeService.revokeCertificate(
            certificateId,
            revokeCertificate.getReason(),
            revokeCertificate.getMessage()
        );

        final var resourceLinks = getCertificateResourceLinks.get(certificate);
        final var certificateDTO = CertificateDTO.create(certificate, resourceLinks);
        return Response.ok(CertificateResponseDTO.create(certificateDTO)).build();
    }

    @POST
    @Path("/{certificateId}/replace")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-replace-certificate", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response replaceCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull NewCertificateRequestDTO newCertificateRequestDTO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Replacing certificate with id: '{}'", certificateId);
        }
        final var newCertificateId = replaceCertificateFacadeService.replaceCertificate(certificateId);
        return Response.ok(ReplaceCertificateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/renew")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-renew-certificate", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response renewCertificate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Renewing certificate with id: '{}'", certificateId);
        }
        final var newCertificateId = renewCertificateFacadeService.renewCertificate(certificateId);
        return Response.ok(RenewCertificateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/template")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-create-certificate-from-template", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response createCertificateFromTemplate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating draft from template with id: '{}'", certificateId);
        }
        final var newCertificateId = createCertificateFromTemplateFacadeService.createCertificateFromTemplate(certificateId);
        return Response.ok(CreateCertificateFromTemplateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/candidate")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-create-certificate-from-candidate", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response createCertificateFromCandidate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Filling draft of id: '{}' with candidate", certificateId);
        }
        final var newCertificateId = createCertificateFromCandidateFacadeService.createCertificateFromCandidate(certificateId);
        return Response.ok(CreateCertificateFromCandidateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/complement")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-complement-certificate", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response complementCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull ComplementCertificateRequestDTO complementCertificateRequest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Complementing certificate with id: '{}'", certificateId);
        }
        final var complementCertificate = complementCertificateFacadeService.complement(
            certificateId,
            complementCertificateRequest.getMessage()
        );

        final var resourceLinks = getCertificateResourceLinks.get(complementCertificate);
        final var certificateDTO = CertificateDTO.create(complementCertificate, resourceLinks);
        return Response.ok(CertificateResponseDTO.create(certificateDTO)).build();
    }

    @POST
    @Path("/{certificateId}/answercomplement")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-answer-complement-certificate", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response answerComplementCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull ComplementCertificateRequestDTO complementCertificateRequest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Answer complement certificate with id: '{}'", certificateId);
        }
        final var answeredComplementCertificate = complementCertificateFacadeService.answerComplement(
            certificateId,
            complementCertificateRequest.getMessage()
        );

        final var resourceLinks = getCertificateResourceLinks.get(answeredComplementCertificate);
        final var certificateDTO = CertificateDTO.create(answeredComplementCertificate, resourceLinks);
        return Response.ok(CertificateResponseDTO.create(certificateDTO)).build();
    }

    @POST
    @Path("/{certificateId}/copy")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-copy-certificate", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response copyCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull NewCertificateRequestDTO copyCertificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Copy certificate with id: '{}'", certificateId);
        }
        final var newCertificateId = copyCertificateFacadeService.copyCertificate(
            certificateId
        );
        return Response.ok(CopyCertificateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/forward")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-forward-certificate", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response forwardCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull ForwardCertificateRequestDTO forwardCertificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Forward certificate with id: '{}' and forwarded: '{}'",
                certificateId,
                forwardCertificate.isForwarded()
            );
        }
        final var certificate = forwardCertificateFacadeService.forwardCertificate(
            certificateId,
            forwardCertificate.isForwarded()
        );

        final var resourceLinks = getCertificateResourceLinks.get(certificate);
        final var certificateDTO = CertificateDTO.create(certificate, resourceLinks);
        return Response.ok(CertificateResponseDTO.create(certificateDTO)).build();
    }

    @POST
    @Path("/{certificateId}/readyforsign")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-ready-for-sign", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response readyForSign(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Ready for sign certificate with id: '{}' ", certificateId);
        }
        final var certificate = readyForSignAggregator.readyForSign(certificateId);

        final var resourceLinks = getCertificateResourceLinks.get(certificate);
        final var certificateDTO = CertificateDTO.create(certificate, resourceLinks);
        return Response.ok(CertificateResponseDTO.create(certificateDTO)).build();
    }

    @POST
    @Path("/{certificateId}/send")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-send-certificate", eventType = MdcLogConstants.EVENT_TYPE_CHANGE)
    public Response sendCertificate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending certificate with id: '{}'", certificateId);
        }
        final var result = sendCertificateFacadeService.sendCertificate(certificateId);
        return Response.ok(SendCertificateResponseDTO.create(certificateId, result)).build();
    }

    @GET
    @Path("/{certificateId}/events")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-get-certificate-events", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getCertificateEvents(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving events for certificate with id: '{}'", certificateId);
        }
        try {
            final var certificateEvents = getCertificateEventsFacadeService.getCertificateEvents(certificateId);
            return Response.ok(CertificateEventResponseDTO.create(certificateEvents)).build();
        } catch (Exception e) {
            return Response.ok(CertificateEventResponseDTO.create(new CertificateEventDTO[0])).build();
        }
    }

    @POST
    @Path("/{certificateType}/{patientId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-create-certificate", eventType = MdcLogConstants.EVENT_TYPE_CREATION)
    public Response createCertificate(@PathParam("certificateType") String certificateType, @PathParam("patientId") String patientId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating certificate with type: '{}'", certificateType);
        }
        try {
            final var certificateId = createCertificateFacadeService.create(certificateType, patientId);
            return Response.ok().entity(new CreateCertificateResponseDTO(certificateId)).build();
        } catch (CreateCertificateException e) {
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("/{certificateId}/related")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-get-related-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getRelatedCertificate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get related certificate for certificateId: '{}'", certificateId);
        }
        final var relatedCertificateId = getRelatedCertificateFacadeService.get(certificateId);
        return Response.ok(GetRelatedCertificateDTO.create(relatedCertificateId)).build();
    }

    @GET
    @Path("/{certificateId}/candidatemessage")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    @PerformanceLogging(eventAction = "certificate-get-candidate-message-for-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public Response getCandidateMessageForCertificate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Get candidate message for certificateId: '{}'", certificateId);
        }
        return Response.ok(getCandidateMesssageForCertificateFacadeService.get(certificateId)).build();
    }
}
