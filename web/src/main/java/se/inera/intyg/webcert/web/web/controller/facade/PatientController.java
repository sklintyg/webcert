/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.facade.patient.GetPatientFacadeService;
import se.inera.intyg.webcert.web.service.facade.patient.InvalidPatientIdException;
import se.inera.intyg.webcert.web.service.facade.patient.PatientNoNameException;
import se.inera.intyg.webcert.web.service.facade.patient.PatientSearchErrorException;
import se.inera.intyg.webcert.web.web.controller.facade.dto.PatientResponseDTO;

@Path("/patient")
public class PatientController {

    private static final String UTF_8_CHARSET = ";charset=utf-8";

    @Autowired
    private GetPatientFacadeService getPatientFacadeService;

    @GET
    @Path("/{patientId}")
    @Produces(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @PrometheusTimeMethod
    public Response getPatient(@PathParam("patientId") @NotNull String patientId) {
        try {
            final var patient = getPatientFacadeService.getPatient(patientId);
            return Response.ok(PatientResponseDTO.create(patient)).build();
        } catch (InvalidPatientIdException e) {
            return Response.ok(PatientResponseDTO.createInvalidPatientIdResponse()).build();
        } catch (PatientSearchErrorException e) {
            return Response.ok(PatientResponseDTO.createErrorResponse()).build();
        } catch (PatientNoNameException e) {
            return Response.ok(PatientResponseDTO.createNoNameResponse()).build();
        }
    }
}
