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
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.CertificateService;
import se.inera.intyg.webcert.web.web.controller.facade.dto.RevokeCertificateDTO;

@Path("/certificate")
public class CertificateController {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private CertificateService certificateService;

    @Autowired
    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GET
    @Path("/{certificateId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getCertificate(@PathParam("certificateId") @NotNull String certificateId) {
        LOG.debug("Getting certificate with id: {}", certificateId);
        final CertificateDTO certificateDTO = certificateService.getCertificate(certificateId);
        return Response.ok(certificateDTO).build();
    }

    @PUT
    @Path("/{certificateId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response saveCertificate(@RequestBody @NotNull CertificateDTO certificate) {
        LOG.debug("Saving certificate with id: {}", certificate.getMetadata().getCertificateId());
        final long version = certificateService.saveCertificate(certificate);
        return Response.ok(version).build();
    }

    @POST
    @Path("/{certificateId}/validate")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response validateCertificate(@RequestBody @NotNull CertificateDTO certificate) {
        LOG.debug("Validating certificate with id: {}", certificate.getMetadata().getCertificateId());
        final var validationErrors = certificateService.validate(certificate);
        return Response.ok(validationErrors).build();
    }

    @POST
    @Path("/{certificateId}/sign")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response signCertificate(@RequestBody @NotNull CertificateDTO certificate) {
        LOG.debug("Signing certificate with id: {}", certificate.getMetadata().getCertificateId());
        final CertificateDTO certificateDTO = certificateService.signCertificate(certificate);
        return Response.ok(certificateDTO).build();
    }

    @DELETE
    @Path("/{certificateId}/{version}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response deleteCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @PathParam("version") @NotNull long version) {
        LOG.debug("Deleting certificate with id: {} and version: {}", certificateId, version);
        certificateService.deleteCertificate(certificateId, version);
        return Response.ok().build();
    }

    @POST
    @Path("/{certificateId}/revoke")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response revokeCertificate(@PathParam("certificateId") @NotNull String certificateId,
        @RequestBody @NotNull RevokeCertificateDTO revokeCertificate) {
        LOG.debug("Revoking certificate with id: {} and reason: {} and message: {}", certificateId, revokeCertificate.getReason(),
            revokeCertificate.getMessage());
        certificateService.revokeCertificate(certificateId, revokeCertificate.getReason(), revokeCertificate.getMessage());
        return Response.ok().build();
    }
}
