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
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.CopyCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.DeleteCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ForwardCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateEventsFacadeService;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ReplaceCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.RevokeCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SaveCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.SignCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.ValidateCertificateFacadeService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateEventResponseDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.CertificateResponseDTO;
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
    private ForwardCertificateFacadeService forwardCertificateFacadeService;
    @Autowired
    private GetCertificateEventsFacadeService getCertificateEventsFacadeService;

    @GET
    @Path("/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getCertificate(@PathParam("certificateId") @NotNull String certificateId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting certificate with id: '{}'", certificateId);
        }
        final Certificate certificate = getCertificateFacadeService.getCertificate(certificateId);
        return Response.ok(CertificateResponseDTO.create(certificate)).build();
    }

    @PUT
    @Path("/{certificateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response saveCertificate(@RequestBody @NotNull Certificate certificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving certificate with id: '{}'", certificate.getMetadata().getId());
        }
        final long version = saveCertificateFacadeService.saveCertificate(certificate);
        return Response.ok(SaveCertificateResponseDTO.create(version)).build();
    }

    @POST
    @Path("/{certificateId}/validate")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response validateCertificate(@RequestBody @NotNull Certificate certificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Validating certificate with id: '{}'", certificate.getMetadata().getId());
        }
        final var validationErrors = validationCertificateFacadeService.validate(certificate);
        return Response.ok(ValidateCertificateResponseDTO.create(validationErrors)).build();
    }

    @POST
    @Path("/{certificateId}/sign")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response signCertificate(@RequestBody @NotNull Certificate certificate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Signing certificate with id: '{}'", certificate.getMetadata().getId());
        }
        final var signedCertificate = signCertificateFacadeService.signCertificate(certificate);
        return Response.ok(CertificateResponseDTO.create(signedCertificate)).build();
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
        return Response.ok(CertificateResponseDTO.create(certificate)).build();
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
        final var newCertificateId = replaceCertificateFacadeService.replaceCertificate(
            certificateId,
            replaceCertificate.getCertificateType(),
            replaceCertificate.getPatientId().getId()
        );
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
        return Response.ok(CertificateResponseDTO.create(certificate)).build();
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
