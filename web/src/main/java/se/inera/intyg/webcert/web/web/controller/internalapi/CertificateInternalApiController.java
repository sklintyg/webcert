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

package se.inera.intyg.webcert.web.web.controller.internalapi;

import io.swagger.annotations.Api;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.CertificatePdfResponseDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateIntegrationRequestDTO;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.GetCertificateResponse;

@Path("/certificate")
@Api(value = "/internalapi/certificate", produces = MediaType.APPLICATION_JSON)
public class CertificateInternalApiController {

    private final GetCertificateInteralApi getCertificateInternalAggregator;
    private final GetCertificatePdfService getCertificateInternalPdfAggregator;
    private static final String UTF_8_CHARSET = ";charset=utf-8";

    public CertificateInternalApiController(
        @Qualifier("getCertificateInternalAggregator") GetCertificateInteralApi getCertificateInternalAggregator,
        @Qualifier("getCertificateInternalPdfAggregator") GetCertificatePdfService getCertificateInternalPdfAggregator) {
        this.getCertificateInternalAggregator = getCertificateInternalAggregator;
        this.getCertificateInternalPdfAggregator = getCertificateInternalPdfAggregator;
    }

    @POST
    @Path("/{certificateId}")
    @PrometheusTimeMethod
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "certificate-internal-get-certificate", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public GetCertificateResponse getCertificate(@RequestBody GetCertificateIntegrationRequestDTO request,
        @PathParam("certificateId") String certificateId) {
        return getCertificateInternalAggregator.get(certificateId, request.getPersonId());
    }

    @POST
    @Path("/{certificateId}/pdf")
    @PrometheusTimeMethod
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PerformanceLogging(eventAction = "certificate-internal-get-pdf-data", eventType = MdcLogConstants.EVENT_TYPE_ACCESS)
    public CertificatePdfResponseDTO getPdfData(@RequestBody CertificatePdfRequestDTO request,
        @PathParam("certificateId") String certificateId) {
        return getCertificateInternalPdfAggregator.get(
            request.getCustomizationId(),
            certificateId,
            request.getPersonId()
        );
    }
}
