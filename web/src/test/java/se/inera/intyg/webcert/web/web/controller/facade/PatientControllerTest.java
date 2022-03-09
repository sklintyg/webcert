/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.infra.integration.pu.model.PersonSvar;
import se.inera.intyg.webcert.web.service.facade.patient.GetPatientFacadeService;
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

    private PatientResponseDTO createPatientResponse() {
        final var patient = Patient.builder()
                .personId(
                        PersonId.builder()
                                .id(PATIENT_ID)
                                .build()
                )
                .build();
        return PatientResponseDTO.create(patient, PersonSvar.Status.FOUND);
    }

    @Nested
    class GetPatient {
        private PatientResponseDTO patientResponse;

        @BeforeEach
        void setup() {
            patientResponse = createPatientResponse();

            doReturn(patientResponse)
                .when(getPatientFacadeService)
                .getPatient(anyString());
        }

        @Test
        void shallIncludePatientResponse() {
            final var response = (PatientResponseDTO) patientController.getPatient(PATIENT_ID).getEntity();
            assertEquals(response, patientResponse);
        }
    }
}
