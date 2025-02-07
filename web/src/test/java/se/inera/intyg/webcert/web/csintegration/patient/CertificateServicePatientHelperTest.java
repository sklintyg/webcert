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

package se.inera.intyg.webcert.web.csintegration.patient;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.pu.integration.api.model.Person;
import se.inera.intyg.infra.pu.integration.api.model.PersonSvar;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;

@ExtendWith(MockitoExtension.class)
class CertificateServicePatientHelperTest {

    public static final String MIDDLE_NAME = "middleName";
    public static final String STREET = "postalAdress";
    public static final String ZIP_CODE = "postalNumber";
    public static final String CITY = "city";
    private static final String ORIGINAL_PATIENT_ID = "191212121212";
    private static final String COORDINATION_NUMBER_PATIENT_ID = "191212721212";
    private static final Personnummer PATIENT_ID = Personnummer.createPersonnummer(ORIGINAL_PATIENT_ID).orElseThrow();
    private static final Personnummer COORDINATION_NUMBER = Personnummer.createPersonnummer(COORDINATION_NUMBER_PATIENT_ID).orElseThrow();
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    @Mock
    PatientDetailsResolver patientDetailsResolver;

    @InjectMocks
    CertificateServicePatientHelper certificateServicePatientHelper;

    private Person createPerson(Personnummer id) {
        return new Person(
            id,
            false,
            false,
            FIRST_NAME,
            MIDDLE_NAME,
            LAST_NAME,
            STREET,
            ZIP_CODE,
            CITY,
            false

        );
    }

    private Person createPerson() {
        return createPerson(PATIENT_ID);
    }

    @Nested
    class CoordinationNumber {

        @BeforeEach
        void setup() {
            when(patientDetailsResolver.getPersonFromPUService(COORDINATION_NUMBER))
                .thenReturn(PersonSvar.found(createPerson(COORDINATION_NUMBER)));
        }

        @Test
        void shouldSetPatientIdTypeAsCoordinationNumberId() {
            final var response = certificateServicePatientHelper.get(COORDINATION_NUMBER);

            assertEquals(PersonIdType.COORDINATION_NUMBER, response.getId().getType());
        }
    }

    @Nested
    class TextValues {

        @BeforeEach
        void setUp() {
            when(patientDetailsResolver.getPersonFromPUService(PATIENT_ID))
                .thenReturn(PersonSvar.found(createPerson()));
        }

        @Test
        void shouldSetPatientId() {
            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertEquals(ORIGINAL_PATIENT_ID, response.getId().getId());
        }

        @Test
        void shouldSetPatientIdTypeAsPersonId() {
            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertEquals(PersonIdType.PERSONAL_IDENTITY_NUMBER, response.getId().getType());
        }

        @Test
        void shouldSetPatientProtectedPerson() {
            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertEquals(ORIGINAL_PATIENT_ID, response.getId().getId());
        }


        @Test
        void shouldSetPatientFirstName() {
            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertEquals(FIRST_NAME, response.getFirstName());
        }

        @Test
        void shouldSetPatientMiddleName() {
            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertEquals(MIDDLE_NAME, response.getMiddleName());
        }

        @Test
        void shouldSetPatientLastName() {
            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertEquals(LAST_NAME, response.getLastName());
        }

        @Test
        void shouldSetPatientStreet() {
            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertEquals(STREET, response.getStreet());
        }

        @Test
        void shouldSetPatientZipCode() {
            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertEquals(ZIP_CODE, response.getZipCode());
        }

        @Test
        void shouldSetPatientCity() {
            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertEquals(CITY, response.getCity());
        }

    }

    @Nested
    class BooleanValues {

        @Test
        void shouldSetPatientProtectedPersonTrue() {
            final var patient = mock(Person.class);

            when(patient.sekretessmarkering())
                .thenReturn(true);

            when(patientDetailsResolver.getPersonFromPUService(PATIENT_ID))
                .thenReturn(PersonSvar.found(patient));

            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertTrue(response.getProtectedPerson());
        }

        @Test
        void shouldSetPatientProtectedPersonFalse() {
            final var patient = mock(Person.class);

            when(patient.sekretessmarkering())
                .thenReturn(false);

            when(patientDetailsResolver.getPersonFromPUService(PATIENT_ID))
                .thenReturn(PersonSvar.found(patient));

            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertFalse(response.getProtectedPerson());
        }


        @Test
        void shouldSetPatientIsDeceasedTrue() {
            final var patient = mock(Person.class);

            when(patient.avliden())
                .thenReturn(true);

            when(patientDetailsResolver.getPersonFromPUService(PATIENT_ID))
                .thenReturn(PersonSvar.found(patient));

            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertTrue(response.getDeceased());
        }

        @Test
        void shouldSetPatientIsDeceasedFalse() {
            final var patient = mock(Person.class);

            when(patient.avliden())
                .thenReturn(false);

            when(patientDetailsResolver.getPersonFromPUService(PATIENT_ID))
                .thenReturn(PersonSvar.found(patient));

            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertFalse(response.getDeceased());
        }

        @Test
        void shouldSetPatientTestIndicatorTrue() {
            final var patient = mock(Person.class);

            when(patient.testIndicator())
                .thenReturn(true);

            when(patientDetailsResolver.getPersonFromPUService(PATIENT_ID))
                .thenReturn(PersonSvar.found(patient));

            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertTrue(response.getTestIndicated());
        }

        @Test
        void shouldSetPatientTestIndicatorFalse() {
            final var patient = mock(Person.class);

            when(patient.testIndicator())
                .thenReturn(false);

            when(patientDetailsResolver.getPersonFromPUService(PATIENT_ID))
                .thenReturn(PersonSvar.found(patient));

            final var response = certificateServicePatientHelper.get(PATIENT_ID);

            assertFalse(response.getTestIndicated());
        }
    }
}
