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
package se.inera.intyg.webcert.web.web.controller.facade;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.GetCertificateTypesFacadeService;

@Path("/certificate/type")
public class CertificateTypeController {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateTypeController.class);

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    private final GetCertificateTypesFacadeService getCertificateTypesFacadeService;

    @Autowired
    public CertificateTypeController(GetCertificateTypesFacadeService getCertificateTypesFacadeService) {
        this.getCertificateTypesFacadeService = getCertificateTypesFacadeService;
    }

    @GET
    @Path("/{patientId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getCertificateTypes(@PathParam("patientId") @NotNull String patientId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving certificate types for patient");
        }
        try {
            final var certificateTypes = getCertificateTypesFacadeService.get(createPersonnummer(patientId));
            return Response.ok(certificateTypes).build();
        } catch (InvalidPersonNummerException e) {
            LOG.error(e.getMessage());
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    private Personnummer createPersonnummer(String personId) throws InvalidPersonNummerException {
        return Personnummer.createPersonnummer(personId)
            .orElseThrow(() -> new InvalidPersonNummerException("Could not parse personnummer: " + personId));
    }
}
