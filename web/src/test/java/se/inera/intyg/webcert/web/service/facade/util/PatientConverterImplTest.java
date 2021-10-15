/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class PatientConverterImplTest {

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    PatientDetailsResolver patientDetailsResolver;

    @InjectMocks
    PatientConverterImpl patientConverter;

    final String PATIENT_ID = "191212121212";
    final String ALTERNATE_PATIENT_ID = "191212121213";
    final String FIRSTNAME = "firstname";
    final String MIDDLENAME = "middlename";
    final String LASTNAME = "lastname";
    final String ALTERNATE_FIRSTNAME = "firstnam";
    final String ALTERNATE_LASTNAME = "astname";
    final String RESERVE_NUMBER = "191212123030";
    final Utkast utkast = createDraft();
    final WebCertUser user = new WebCertUser();


    @BeforeEach
    public void setup() {
        doReturn(true).when(webCertUserService).hasAuthenticationContext();
        doReturn(user).when(webCertUserService).getUser();
    }

    @Nested
    class PatientValues {

        @Test
        public void shallConvertPatientFirstName() {
            final var patient = patientConverter.convert(utkast);
            assertEquals(utkast.getPatientFornamn(), patient.getFirstName());
        }

        @Test
        public void shallConvertPatientLastName() {
            final var patient = patientConverter.convert(utkast);
            assertEquals(utkast.getPatientEfternamn(), patient.getLastName());
        }

        @Test
        public void shallConvertPatientMiddle() {
            final var patient = patientConverter.convert(utkast);
            assertEquals(utkast.getPatientMellannamn(), patient.getMiddleName());
        }

        @Test
        public void shallConvertPatientFullName() {
            final var patient = patientConverter.convert(utkast);
            assertEquals(utkast.getPatientFornamn() + " " + utkast.getPatientMellannamn() + " " + utkast.getPatientEfternamn(),
                patient.getFullName());
        }

        @Test
        public void shallHandleNullMiddlename() {
            utkast.setPatientMellannamn(null);
            final var patient = patientConverter.convert(utkast);
            assertNull(patient.getMiddleName());
            assertEquals(utkast.getPatientFornamn() + " " + utkast.getPatientEfternamn(),
                patient.getFullName());
        }

        @Test
        public void shallConvertPatientId() {
            final var patient = patientConverter.convert(utkast);
            assertEquals(utkast.getPatientPersonnummer().getPersonnummer(), patient.getPersonId().getId());
        }

        @Test
        public void shallConvertPatientIdType() {
            final var patient = patientConverter.convert(utkast);
            assertEquals("PERSON_NUMMER", patient.getPersonId().getType());
        }
    }

    @Nested
    class PatientStatuses {

        @Test
        public void shallSetIsPatientIdDifferentFromIntegrationParameters() {
            final var parameters = getIntegrationParameters(ALTERNATE_PATIENT_ID, FIRSTNAME, LASTNAME);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(utkast);
            assertTrue(patient.isDifferentPatientIdFromJS());
        }

        @Test
        public void shallNotSetIsPatientIdDifferentFromIntegrationParameters() {
            final var parameters = getIntegrationParameters(PATIENT_ID, FIRSTNAME, LASTNAME);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(utkast);
            assertFalse(patient.isDifferentPatientIdFromJS());
        }

        @Test
        public void shallSetReserveIdIfExistsInIntegrationParameters() {
            final var parameters = getIntegrationParameters(PATIENT_ID, FIRSTNAME, LASTNAME);
            parameters.setBeforeAlternateSsn(RESERVE_NUMBER);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(utkast);
            assertEquals(RESERVE_NUMBER, patient.getReserveId());
        }

        @Test
        public void shallNotSetReserveIdIfNotInIntegrationParameters() {
            final var parameters = getIntegrationParameters(PATIENT_ID, FIRSTNAME, LASTNAME);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(utkast);
            assertEquals("", patient.getReserveId());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void shallSetIsPatientDeceasedFromPU(boolean isDeceased) {
            doReturn(isDeceased).when(patientDetailsResolver).isAvliden(any());
            final var patient = patientConverter.convert(utkast);
            assertEquals(isDeceased, patient.isDeceased());
        }

        @Test
        public void shallSetThatPatientHasProtectedIdentityIfTrue() {
            doReturn(SekretessStatus.TRUE).when(patientDetailsResolver).getSekretessStatus(any());
            final var patient = patientConverter.convert(utkast);
            assertTrue(patient.isProtectedPerson());
        }

        @Test
        public void shallSetThatPatientHasProtectedIdentityIfTrueIfUndefined() {
            doReturn(SekretessStatus.UNDEFINED).when(patientDetailsResolver).getSekretessStatus(any());
            final var patient = patientConverter.convert(utkast);
            assertTrue(patient.isProtectedPerson());
        }


        @Test
        public void shallNotSetThatPatientHasProtectedIdentityIfFalse() {
            doReturn(SekretessStatus.FALSE).when(patientDetailsResolver).getSekretessStatus(any());
            final var patient = patientConverter.convert(utkast);
            assertFalse(patient.isProtectedPerson());
        }

        @Test
        public void shallSetIsPatientNameDifferentIfFirstnameChanges() {
            final var parameters = getIntegrationParameters(PATIENT_ID, ALTERNATE_FIRSTNAME, LASTNAME);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(utkast);
            assertTrue(patient.isDifferentNameFromJS());
        }

        @Test
        public void shallSetIsPatientNameDifferentIfLastnameChanges() {
            final var parameters = getIntegrationParameters(PATIENT_ID, FIRSTNAME, ALTERNATE_LASTNAME);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(utkast);
            assertTrue(patient.isDifferentNameFromJS());
        }

        @Test
        public void shallSetIsPatientNameDifferentIfBothChange() {
            final var parameters = getIntegrationParameters(PATIENT_ID, ALTERNATE_FIRSTNAME, ALTERNATE_LASTNAME);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(utkast);
            assertTrue(patient.isDifferentNameFromJS());
        }

        @Test
        public void shallNotSetIsPatientNameDifferentIfNameIsSame() {
            final var parameters = getIntegrationParameters(PATIENT_ID, FIRSTNAME, LASTNAME);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(utkast);
            assertFalse(patient.isDifferentNameFromJS());
        }

        @Test
        public void shallNotStausesIfNoIntegrationParameters() {
            final var patient = patientConverter.convert(utkast);
            assertFalse(patient.isDifferentNameFromJS());
            assertFalse(patient.isDifferentPatientIdFromJS());
        }
    }

    private IntegrationParameters getIntegrationParameters(String alternateSsn, String firstname, String lastname) {
        return new IntegrationParameters("reference", "responsible", alternateSsn, firstname, "mellannamn", lastname,
            "address", "zipcode", "city", true, false, false, true);
    }

    private Utkast createDraft() {
        final var draft = new Utkast();
        draft.setIntygsId("certificateId");
        draft.setIntygsTyp("certificateType");
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setModel("draftJson");
        draft.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        draft.setSkapad(LocalDateTime.now());
        draft.setPatientPersonnummer(Personnummer.createPersonnummer(PATIENT_ID).orElseThrow());
        draft.setPatientEfternamn(LASTNAME);
        draft.setPatientFornamn(FIRSTNAME);
        draft.setPatientMellannamn(MIDDLENAME);
        draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
        return draft;
    }
}