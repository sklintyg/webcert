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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.webcert.web.service.facade.patient.GetPatientFacadeService;
import se.inera.intyg.webcert.web.service.facade.patient.InvalidPatientIdException;
import se.inera.intyg.webcert.web.service.facade.patient.PatientNoNameException;
import se.inera.intyg.webcert.web.service.facade.patient.PatientSearchErrorException;
import se.inera.intyg.webcert.web.web.controller.facade.dto.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientControllerTest {

    @Mock
    private GetPatientFacadeService getPatientFacadeService;
    @InjectMocks
    private PatientController patientController;

    final String PATIENT_ID = "patientId";

    private Patient createPatient() {
        return Patient.builder()
            .personId(
                PersonId.builder()
                    .id(PATIENT_ID)
                    .build()
            )
            .build();
    }

    @Nested
    class GetPatient {

        void setup() {
            try {
                doReturn(null)
                    .when(getPatientFacadeService)
                    .getPatient(anyString());
            } catch (InvalidPatientIdException | PatientSearchErrorException | PatientNoNameException e) {
                e.printStackTrace();
            }
        }

        void setup(Patient patient) {
            try {
                doReturn(patient)
                    .when(getPatientFacadeService)
                    .getPatient(anyString());
            } catch (InvalidPatientIdException | PatientSearchErrorException | PatientNoNameException e) {
                e.printStackTrace();
            }
        }

        void setup(Exception e) throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
            doThrow(e)
                .when(getPatientFacadeService)
                .getPatient(anyString());
        }

        @Test
        void shallIncludePatientInResponse() {
            final var patient = createPatient();
            setup(patient);
            final var response = (PatientResponseDTO) patientController.getPatient(PATIENT_ID).getEntity();
            assertEquals(response.getPatient(), patient);
        }

        @Test
        void shallSetStatusToFoundIfPatientExists() {
            final var patient = createPatient();
            setup(patient);
            final var response = (PatientResponseDTO) patientController.getPatient(PATIENT_ID).getEntity();
            assertEquals(response.getStatus(), PatientResponseStatusDTO.FOUND);
        }

        @Test
        void shallSetStatusToNotFoundIfPatientIsNull() {
            setup();
            final var response = (PatientResponseDTO) patientController.getPatient(PATIENT_ID).getEntity();
            assertEquals(response.getStatus(), PatientResponseStatusDTO.NOT_FOUND);
        }

        @Test
        void shallSetStatusToErrorIfExceptionIsThrown()
            throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
            setup(new PatientSearchErrorException());
            final var response = (PatientResponseDTO) patientController.getPatient(PATIENT_ID).getEntity();
            assertEquals(response.getStatus(), PatientResponseStatusDTO.ERROR);
        }

        @Test
        void shallSetStatusToInvalidPatientIdIfExceptionIsThrown()
            throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
            setup(new InvalidPatientIdException());
            final var response = (PatientResponseDTO) patientController.getPatient(PATIENT_ID).getEntity();
            assertEquals(response.getStatus(), PatientResponseStatusDTO.INVALID_PATIENT_ID);
        }

        @Test
        void shallSetStatusNoNameIfExceptionIsThrown()
            throws InvalidPatientIdException, PatientSearchErrorException, PatientNoNameException {
            setup(new PatientNoNameException());
            final var response = (PatientResponseDTO) patientController.getPatient(PATIENT_ID).getEntity();
            assertEquals(response.getStatus(), PatientResponseStatusDTO.NO_NAME);
        }
    }
}
