/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.internalapi;

import io.swagger.annotations.Api;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.facade.internalapi.service.GetAvailableFunctionsForCertificateService;
import se.inera.intyg.webcert.web.service.facade.internalapi.service.GetCertificatePdfService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateResponse;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfRequestDTO;

@Path("/certificate")
@Api(value = "/internalapi/certificate", produces = MediaType.APPLICATION_JSON)
public class CertificateInternalApiController {

    private final GetCertificateFacadeService getCertificateFacadeService;
    private final GetAvailableFunctionsForCertificateService getAvailableFunctionsForCertificateService;
    private final GetCertificatePdfService getCertificatePdfService;

    private static final String UTF_8_CHARSET = ";charset=utf-8";
    private static final boolean SHOULD_NOT_PDL_LOG = false;
    private static final boolean SHOULD_NOT_VALIDATE_ACCESS = false;

    public CertificateInternalApiController(GetCertificateFacadeService getCertificateFacadeService,
        GetAvailableFunctionsForCertificateService getAvailableFunctionsForCertificateService,
        GetCertificatePdfService getCertificatePdfService) {
        this.getCertificateFacadeService = getCertificateFacadeService;
        this.getAvailableFunctionsForCertificateService = getAvailableFunctionsForCertificateService;
        this.getCertificatePdfService = getCertificatePdfService;
    }

    @GET
    @Path("/{certificateId}")
    @PrometheusTimeMethod
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public GetCertificateResponse getCertificate(@PathParam("certificateId") String certificateId) {
        final var certificate = getCertificateFacadeService.getCertificate(certificateId, SHOULD_NOT_PDL_LOG, SHOULD_NOT_VALIDATE_ACCESS);
        final var availableFunction = getAvailableFunctionsForCertificateService.get(certificate);
        return GetCertificateResponse.create(
            certificate,
            availableFunction
        );
    }

    @POST
    @Path("/{certificateId}/pdf")
    @PrometheusTimeMethod
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    public CertificatePdfResponseDTO getPdfData(@RequestBody CertificatePdfRequestDTO request,
        @PathParam("certificateId") String certificateId) {
        return getCertificatePdfService.get(
            request.getCustomizationId(),
            certificateId
        );
    }
}
