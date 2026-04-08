/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.inera.intyg.webcert.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.web.service.facade.patient.GetPatientFacadeService;
import se.inera.intyg.webcert.web.service.facade.patient.InvalidPatientIdException;
import se.inera.intyg.webcert.web.service.facade.patient.PatientNoNameException;
import se.inera.intyg.webcert.web.service.facade.patient.PatientSearchErrorException;
import se.inera.intyg.webcert.web.web.controller.facade.dto.PatientResponseDTO;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

  private static final String UTF_8_CHARSET = ";charset=utf-8";

  @Autowired private GetPatientFacadeService getPatientFacadeService;

  @GetMapping("/{patientId}")
  @PrometheusTimeMethod
  @PerformanceLogging(
      eventAction = "patient-get-patient",
      eventType = MdcLogConstants.EVENT_TYPE_ERROR)
  public ResponseEntity<PatientResponseDTO> getPatient(
      @PathVariable("patientId") @NotNull String patientId) {
    try {
      final var patient = getPatientFacadeService.getPatient(patientId);
      return ResponseEntity.ok(PatientResponseDTO.create(patient));
    } catch (InvalidPatientIdException e) {
      return ResponseEntity.ok(PatientResponseDTO.createInvalidPatientIdResponse());
    } catch (PatientSearchErrorException e) {
      return ResponseEntity.ok(PatientResponseDTO.createErrorResponse());
    } catch (PatientNoNameException e) {
      return ResponseEntity.ok(PatientResponseDTO.createNoNameResponse());
    }
  }
}
