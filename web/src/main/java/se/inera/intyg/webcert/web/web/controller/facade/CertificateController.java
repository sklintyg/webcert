package se.inera.intyg.webcert.web.web.controller.facade;

import javax.validation.constraints.NotNull;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateDTO;
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateEventResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CopyCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CopyCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ForwardCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ReplaceCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ReplaceCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateRequestDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.SaveCertificateResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ValidateCertificateResponseDTO;

@Path("/certificate")
public class CertificateController {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private CertificateFacadeService certificateFacadeService;

    @Autowired
    public CertificateController(CertificateFacadeService certificateFacadeService) {
        this.certificateFacadeService = certificateFacadeService;
    }

    @GET
    @Path("/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getCertificate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting certificate with id: '{}'", certificateId);
        }
        final CertificateDTO certificateDTO = certificateFacadeService.getCertificate(certificateId);
        return Response.ok(certificateDTO).build();
    }

    @PUT
    @Path("/{certificateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response saveCertificate(@RequestBody @NotNull CertificateDTO certificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving certificate with id: '{}'", certificate.getMetadata().getCertificateId());
        }
        final long version = certificateFacadeService.saveCertificate(certificate);
        return Response.ok(SaveCertificateResponseDTO.create(version)).build();
    }

    @POST
    @Path("/{certificateId}/validate")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response validateCertificate(@RequestBody @NotNull CertificateDTO certificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Validating certificate with id: '{}'", certificate.getMetadata().getCertificateId());
        }
        final var validationErrors = certificateFacadeService.validate(certificate);
        return Response.ok(ValidateCertificateResponseDTO.create(validationErrors)).build();
    }

    @POST
    @Path("/{certificateId}/sign")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response signCertificate(@RequestBody @NotNull CertificateDTO certificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signing certificate with id: '{}'", certificate.getMetadata().getCertificateId());
        }
        final CertificateDTO certificateDTO = certificateFacadeService.signCertificate(certificate);
        return Response.ok(certificateDTO).build();
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
        certificateFacadeService.deleteCertificate(certificateId, version);
        return Response.ok().build();
    }

    @POST
    @Path("/{certificateId}/revoke")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response revokeCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull RevokeCertificateRequestDTO revokeCertificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Revoking certificate with id: '{}' and reason: '{}' and message: '{}'", certificateId, revokeCertificate.getReason(),
                revokeCertificate.getMessage());
        }
        final CertificateDTO certificateDTO = certificateFacadeService
            .revokeCertificate(certificateId, revokeCertificate.getReason(), revokeCertificate.getMessage());
        return Response.ok(certificateDTO).build();
    }

    @POST
    @Path("/{certificateId}/replace")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response replaceCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull ReplaceCertificateRequestDTO replaceCertificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Replacing certificate with id: '{}'", certificateId);
        }
        final var newCertificateId = certificateFacadeService
            .replaceCertificate(certificateId, replaceCertificate.getCertificateType(), replaceCertificate.getPatientId());
        return Response.ok(ReplaceCertificateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/copy")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response copyCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull CopyCertificateRequestDTO copyCertificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Copy certificate with id: '{}'", certificateId);
        }
        final var newCertificateId = certificateFacadeService
            .copyCertificate(certificateId, copyCertificate.getCertificateType(), copyCertificate.getPatientId());
        return Response.ok(CopyCertificateResponseDTO.create(newCertificateId)).build();
    }

    @POST
    @Path("/{certificateId}/{version}/forward")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response forwardCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @PathParam("version") @NotNull long version, @RequestBody @NotNull ForwardCertificateRequestDTO forwardCertificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Forward certificate with id: '{}' and version: '{}' and forwarded: '{}'", certificateId, version,
                forwardCertificate.isForwarded());
        }
        final CertificateDTO certificateDTO = certificateFacadeService
            .forwardCertificate(certificateId, version, forwardCertificate.isForwarded());
        return Response.ok(certificateDTO).build();
    }

    @GET
    @Path("/{certificateId}/events")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getCertificateEvents(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving events for certificate with id: '{}'", certificateId);
        }
        final CertificateEventDTO[] certificateEvents = certificateFacadeService.getCertificateEvents(certificateId);
        return Response.ok(CertificateEventResponseDTO.create(certificateEvents)).build();
    }
}
