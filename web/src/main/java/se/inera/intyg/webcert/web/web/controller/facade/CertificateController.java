/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.ComplementCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.CopyCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.DeleteCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ForwardCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateEventsFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateResourceLinks;
import se.inera.intyg.webcert.web.service.facade.RenewCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReplaceCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.RevokeCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SaveCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SendCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SignCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ValidateCertificateFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateEventResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ComplementCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CopyCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ForwardCertificateRequestDTO;
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
    private GetCertificateFacadeService getCertificateFacadeService;
    @Autowired
    private SaveCertificateFacadeService saveCertificateFacadeService;
    @Autowired
    private ValidateCertificateFacadeService validationCertificateFacadeService;
    @Autowired
    private SignCertificateFacadeService signCertificateFacadeService;
    @Autowired
    private DeleteCertificateFacadeService deleteCertificateFacadeService;
    @Autowired
    private RevokeCertificateFacadeService revokeCertificateFacadeService;
    @Autowired
    private ReplaceCertificateFacadeService replaceCertificateFacadeService;
    @Autowired
    private CopyCertificateFacadeService copyCertificateFacadeService;
    @Autowired
    private RenewCertificateFacadeService renewCertificateFacadeService;
    @Autowired
    private ForwardCertificateFacadeService forwardCertificateFacadeService;
    @Autowired
    private GetCertificateEventsFacadeService getCertificateEventsFacadeService;
    @Autowired
    private GetCertificateResourceLinks getCertificateResourceLinks;
    @Autowired
    private SendCertificateFacadeService sendCertificateFacadeService;
    @Autowired
    private ComplementCertificateFacadeService complementCertificateFacadeService;

    @GET
    @Path("/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getCertificate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting certificate with id: '{}'", certificateId);
        }
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, true);
        final var resourceLinks = getCertificateResourceLinks.get(certificate);
        final var certificateDTO = CertificateDTO.create(certificate, resourceLinks);
        return Response.ok(CertificateResponseDTO.create(certificateDTO)).build();
    }

    @PUT
    @Path("/{certificateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
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
    public Response replaceCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull NewCertificateRequestDTO newCertificateRequestDTO) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Replacing certificate with id: '{}'", certificateId);
        }
        final var newCertificateId = replaceCertificateFacadeService.replaceCertificate(
            certificateId,
            newCertificateRequestDTO.getCertificateType(),
            newCertificateRequestDTO.getPatientId().getId()
        );
        return Response.ok(ReplaceCertificateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/renew")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response renewCertificate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Renewing certificate with id: '{}'", certificateId);
        }
        final var newCertificateId = renewCertificateFacadeService.renewCertificate(certificateId);
        return Response.ok(RenewCertificateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/renew/template")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response renewCertificateFromTemplate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Renewing certificate with id: '{}'", certificateId);
        }
        final var newCertificateId = renewCertificateFacadeService.renewCertificateFromTemplate(certificateId);
        return Response.ok(RenewCertificateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/complement")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
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
    public Response copyCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull NewCertificateRequestDTO copyCertificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Copy certificate with id: '{}'", certificateId);
        }
        final var newCertificateId = copyCertificateFacadeService.copyCertificate(
            certificateId,
            copyCertificate.getCertificateType(),
            copyCertificate.getPatientId().getId()
        );
        return Response.ok(CopyCertificateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/{version}/forward")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response forwardCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @PathParam("version") @NotNull long version, @RequestBody @NotNull ForwardCertificateRequestDTO forwardCertificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Forward certificate with id: '{}' and version: '{}' and forwarded: '{}'",
                certificateId,
                version,
                forwardCertificate.isForwarded()
            );
        }
        final var certificate = forwardCertificateFacadeService.forwardCertificate(
            certificateId,
            version,
            forwardCertificate.isForwarded()
        );

        final var resourceLinks = getCertificateResourceLinks.get(certificate);
        final var certificateDTO = CertificateDTO.create(certificate, resourceLinks);
        return Response.ok(CertificateResponseDTO.create(certificateDTO)).build();
    }

    @POST
    @Path("/{certificateId}/send")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
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
    public Response getCertificateEvents(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving events for certificate with id: '{}'", certificateId);
        }
        final var certificateEvents = getCertificateEventsFacadeService.getCertificateEvents(certificateId);
        return Response.ok(CertificateEventResponseDTO.create(certificateEvents)).build();
    }
}
