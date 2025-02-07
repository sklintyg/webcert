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
package se.inera.intyg.webcert.web.service.facade.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import se.inera.intyg.common.support.model.common.internal.Patient;
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

    final String PATIENT_ID = "19121212-1212";
    final String ALTERNATE_PATIENT_ID = "19121212-1213";
    final String PATIENT_RESERVE_ID = "19121212-12AB";
    final String FIRSTNAME = "firstname";
    final String MIDDLENAME = "middlename";
    final String LASTNAME = "lastname";
    final String ALTERNATE_FIRSTNAME = "firstnam";
    final String ALTERNATE_LASTNAME = "astname";
    final Patient puPatient = createPUPatient(true);
    final se.inera.intyg.common.support.facade.model.Patient originalPatient = se.inera.intyg.common.support.facade.model.Patient.builder()
        .build();
    final Utkast draftWithUpdatedPersonId = createDraftWithUpdatedPersonId();
    final WebCertUser user = new WebCertUser();
    final Personnummer PERSON_NUMMER = Personnummer.createPersonnummer(PATIENT_ID).orElseThrow();
    final Personnummer ALTERNATE_PERSON_NUMMER = Personnummer.createPersonnummer(ALTERNATE_PATIENT_ID).orElseThrow();
    final String CERTIFICATE_TYPE = "certificateType";
    final String CERTIFICATE_TYPE_VERSION = "certificateTypeVersion";


    @Nested
    class PatientWithName {

        @BeforeEach
        public void setup() {
            doReturn(puPatient)
                .when(patientDetailsResolver)
                .resolvePatient(any(Personnummer.class), eq(CERTIFICATE_TYPE), eq(CERTIFICATE_TYPE_VERSION));
            doReturn(true).when(webCertUserService).hasAuthenticationContext();
            doReturn(user).when(webCertUserService).getUser();
        }

        @Nested
        class PatientValues {

            @Test
            public void shallConvertPatientFirstName() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(puPatient.getFornamn(), patient.getFirstName());
            }

            @Test
            public void shallConvertPatientLastName() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(puPatient.getEfternamn(), patient.getLastName());
            }

            @Test
            public void shallConvertPatientMiddle() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(puPatient.getMellannamn(), patient.getMiddleName());
            }

            @Test
            public void shallConvertPatientFullName() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(puPatient.getFullstandigtNamn(), patient.getFullName());
            }

            @Test
            public void shallConvertPatientIdAndIncludeDash() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(puPatient.getPersonId().getPersonnummerWithDash(), patient.getPersonId().getId());
            }

            @Test
            public void shallConvertPatientIdType() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals("PERSON_NUMMER", patient.getPersonId().getType());
            }
        }

        @Nested
        class PatientStatuses {

            @Nested
            class PatientId {

                @Test
                public void shallHaveAlternateSSNAsPatientIdIfPatientIdHasBeenReplaced() {
                    final var parameters = getIntegrationParameters(ALTERNATE_PATIENT_ID, FIRSTNAME, LASTNAME);
                    parameters.setBeforeAlternateSsn(PATIENT_ID);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, ALTERNATE_PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertEquals(ALTERNATE_PATIENT_ID, patient.getPersonId().getId());
                    assertTrue(patient.isPersonIdChanged());
                }

                @Test
                public void shallHaveOriginalIdIfAlternateSSNIsEmpty() {
                    final var parameters = getIntegrationParameters("", FIRSTNAME, LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertEquals(PATIENT_ID, patient.getPersonId().getId());
                    assertFalse(patient.isPersonIdChanged());
                }

                @Test
                public void shallHaveOriginalPatientIdIfAlternateSSNIsNull() {
                    final var parameters = getIntegrationParameters(null, FIRSTNAME, LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertEquals(PATIENT_ID, patient.getPersonId().getId());
                    assertFalse(patient.isPersonIdChanged());
                }

                @Test
                public void shallHaveAlternateSSNAsPatientIdIfSet() {
                    final var parameters = getIntegrationParameters(ALTERNATE_PATIENT_ID, FIRSTNAME, LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertEquals(ALTERNATE_PATIENT_ID, patient.getPersonId().getId());
                    assertTrue(patient.isPersonIdChanged());
                }
            }

            @Nested
            class PreviousId {

                @Test
                public void shallNotSetPreviousIdIfAlternateSSNIsEmpty() {
                    final var parameters = getIntegrationParameters("", FIRSTNAME, LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertNull(patient.getPreviousPersonId());
                    assertFalse(patient.isPersonIdChanged());
                }

                @Test
                public void shallNotSetPreviousIdIfAlternateSSNIsNull() {
                    final var parameters = getIntegrationParameters(null, FIRSTNAME, LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertNull(patient.getPreviousPersonId());
                    assertFalse(patient.isPersonIdChanged());
                }

                @Test
                public void shallSetPreviousPersonIdToOriginalIdIfPersonIdIsReplaced() {
                    final var parameters = getIntegrationParameters(ALTERNATE_PATIENT_ID, FIRSTNAME, LASTNAME);
                    parameters.setBeforeAlternateSsn(PATIENT_ID);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, ALTERNATE_PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertEquals(PATIENT_ID, patient.getPreviousPersonId().getId());
                    assertTrue(patient.isPersonIdChanged());
                }

                @Test
                public void shallSetPreviousPersonIdToOriginalIdIfPersonIdIsNotReplaced() {
                    final var parameters = getIntegrationParameters(ALTERNATE_PATIENT_ID, FIRSTNAME, LASTNAME);
                    parameters.setBeforeAlternateSsn(PATIENT_ID);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertEquals(PATIENT_ID, patient.getPreviousPersonId().getId());
                    assertTrue(patient.isPersonIdChanged());
                }

                @Test
                public void shallSetPreviousPersonIdToOriginalIdIfAlternatePatientSSnIsReserveId() {
                    final var parameters = getIntegrationParameters(PATIENT_RESERVE_ID, FIRSTNAME, LASTNAME);
                    parameters.setBeforeAlternateSsn(PATIENT_ID);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, ALTERNATE_PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertEquals(PATIENT_ID, patient.getPreviousPersonId().getId());
                    assertFalse(patient.isPersonIdChanged());
                }

                @Test
                public void shallSetPreviousIdWithCorrectFormatIfBeforeAlternateSsnIsNotAReserveId() {
                    final var parameters = getIntegrationParameters(PATIENT_RESERVE_ID, FIRSTNAME, LASTNAME);
                    parameters.setBeforeAlternateSsn(PATIENT_ID.replace("-", ""));
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, ALTERNATE_PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertEquals(PATIENT_ID, patient.getPreviousPersonId().getId());
                    assertFalse(patient.isPersonIdChanged());
                }

                @Test
                public void shallSetPreviousIdWithCorrectFormatIfBeforeAlternateSsnIsAReserveId() {
                    final var parameters = getIntegrationParameters(PATIENT_ID, FIRSTNAME, LASTNAME);
                    parameters.setBeforeAlternateSsn(PATIENT_RESERVE_ID);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, ALTERNATE_PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertEquals(PATIENT_RESERVE_ID, patient.getPreviousPersonId().getId());
                    assertFalse(patient.isPersonIdChanged());
                }
            }

            @Nested
            class PersonIdChanged {

                @Test
                public void shallSetFlagIfAlternateAndBeforeSsnIsSetAndDiffers() {
                    final var parameters = getIntegrationParameters(ALTERNATE_PATIENT_ID, FIRSTNAME, LASTNAME);
                    parameters.setBeforeAlternateSsn(PATIENT_ID);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, ALTERNATE_PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertTrue(patient.isPersonIdChanged());
                }

                @Test
                public void shallSetFlagIfBeforeSsnIsNotSetButPersonIdAndAlternateSsnDiffers() {
                    final var parameters = getIntegrationParameters(ALTERNATE_PATIENT_ID, FIRSTNAME, LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertTrue(patient.isPersonIdChanged());
                }

                @Test
                public void shallNotSetFlagIfPersonIdIsNotValid() {
                    final var parameters = getIntegrationParameters(PATIENT_RESERVE_ID, FIRSTNAME, LASTNAME);
                    parameters.setBeforeAlternateSsn(PATIENT_ID);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertFalse(patient.isPersonIdChanged());
                }

                @Test
                public void shallNotSetFlagForPersonIdIfNoParametersAreSent() {
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertFalse(patient.isPersonIdChanged());
                }
            }

            @Nested
            class ReserveId {

                @Test
                public void shallSetReserveIdFlagIfPersonIdIsReserveId() {
                    final var parameters = getIntegrationParameters(PATIENT_RESERVE_ID, FIRSTNAME, LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertTrue(patient.isReserveId());
                }

                @Test
                public void shallNotSetReserveIdFlagIfPersonIdIsValid() {
                    final var parameters = getIntegrationParameters(PATIENT_ID, FIRSTNAME, LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertFalse(patient.isReserveId());
                }
            }

            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            public void shallSetIsPatientDeceasedFromPU(boolean isDeceased) {
                puPatient.setAvliden(isDeceased);
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(isDeceased, patient.isDeceased());
            }

            @Nested
            class ProtectedIdentity {

                @Test
                public void shallSetThatPatientHasProtectedIdentityIfTrue() {
                    doReturn(SekretessStatus.TRUE).when(patientDetailsResolver).getSekretessStatus(any());
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertTrue(patient.isProtectedPerson());
                }

                @Test
                public void shallSetThatPatientHasProtectedIdentityIfTrueIfUndefined() {
                    doReturn(SekretessStatus.UNDEFINED).when(patientDetailsResolver).getSekretessStatus(any());
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertTrue(patient.isProtectedPerson());
                }


                @Test
                public void shallNotSetThatPatientHasProtectedIdentityIfFalse() {
                    doReturn(SekretessStatus.FALSE).when(patientDetailsResolver).getSekretessStatus(any());
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertFalse(patient.isProtectedPerson());
                }
            }

            @Nested
            class PatientNameDifferent {

                @Test
                public void shallSetIsPatientNameDifferentIfFirstnameChanges() {
                    final var parameters = getIntegrationParameters(PATIENT_ID, ALTERNATE_FIRSTNAME, LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertTrue(patient.isDifferentNameFromEHR());
                }

                @Test
                public void shallSetIsPatientNameDifferentIfLastnameChanges() {
                    final var parameters = getIntegrationParameters(PATIENT_ID, FIRSTNAME, ALTERNATE_LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertTrue(patient.isDifferentNameFromEHR());
                }

                @Test
                public void shallSetIsPatientNameDifferentIfBothChange() {
                    final var parameters = getIntegrationParameters(PATIENT_ID, ALTERNATE_FIRSTNAME, ALTERNATE_LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertTrue(patient.isDifferentNameFromEHR());
                }

                @Test
                public void shallNotSetIsPatientNameDifferentIfNameIsSame() {
                    final var parameters = getIntegrationParameters(PATIENT_ID, FIRSTNAME, LASTNAME);
                    user.setParameters(parameters);
                    final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE,
                        CERTIFICATE_TYPE_VERSION);
                    assertFalse(patient.isDifferentNameFromEHR());
                }
            }

            @Test
            public void shallNotStausesIfNoIntegrationParameters() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertFalse(patient.isDifferentNameFromEHR());
                assertFalse(patient.isPersonIdChanged());
            }
        }
    }


    @Nested
    class PatientWithNoName {

        @BeforeEach
        public void setup() {
            doReturn(createPUPatientWithNoName())
                .when(patientDetailsResolver)
                .resolvePatient(any(Personnummer.class), eq(CERTIFICATE_TYPE), eq(CERTIFICATE_TYPE_VERSION));
            doReturn(true).when(webCertUserService).hasAuthenticationContext();
            doReturn(user).when(webCertUserService).getUser();
        }

        @Test
        public void shallSetFirstNameToEmptyStringForPatientWithNameAsNull() {
            final var parameters = getIntegrationParameters(ALTERNATE_PATIENT_ID, FIRSTNAME, LASTNAME);
            parameters.setBeforeAlternateSsn(PATIENT_ID);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(originalPatient, ALTERNATE_PERSON_NUMMER, CERTIFICATE_TYPE,
                CERTIFICATE_TYPE_VERSION);
            assertEquals("", patient.getFirstName());
        }

        @Test
        public void shallSetLastNameToEmptyStringForPatientWithNameAsNull() {
            final var parameters = getIntegrationParameters(ALTERNATE_PATIENT_ID, FIRSTNAME, LASTNAME);
            parameters.setBeforeAlternateSsn(PATIENT_ID);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(originalPatient, ALTERNATE_PERSON_NUMMER, CERTIFICATE_TYPE,
                CERTIFICATE_TYPE_VERSION);
            assertEquals("", patient.getLastName());
        }

        @Test
        public void shallSetMiddleNameToEmptyStringForPatientWithNameAsNull() {
            final var parameters = getIntegrationParameters(ALTERNATE_PATIENT_ID, FIRSTNAME, LASTNAME);
            parameters.setBeforeAlternateSsn(PATIENT_ID);
            user.setParameters(parameters);
            final var patient = patientConverter.convert(originalPatient, ALTERNATE_PERSON_NUMMER, CERTIFICATE_TYPE,
                CERTIFICATE_TYPE_VERSION);
            assertEquals("", patient.getMiddleName());
        }
    }

    @Nested
    class PatientAddress {

        @Nested
        class PatientAddressInPU {

            @BeforeEach
            public void setup() {
                doReturn(puPatient)
                    .when(patientDetailsResolver)
                    .resolvePatient(any(Personnummer.class), eq(CERTIFICATE_TYPE), eq(CERTIFICATE_TYPE_VERSION));
                doReturn(true).when(webCertUserService).hasAuthenticationContext();
                doReturn(user).when(webCertUserService).getUser();
            }

            @Test
            public void shallConvertPatientCity() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(puPatient.getPostort(), patient.getCity());
            }

            @Test
            public void shallConvertPatientZipCode() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(puPatient.getPostnummer(), patient.getZipCode());
            }

            @Test
            public void shallConvertPatientStreet() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(puPatient.getPostadress(), patient.getStreet());
            }

            @Test
            public void shallConvertPatientAddressFromPU() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(puPatient.isAddressDetailsSourcePU(), patient.isAddressFromPU());
            }
        }

        @Nested
        class PatientAddressNotInPU {

            private se.inera.intyg.common.support.facade.model.Patient originalPatient;

            @BeforeEach
            public void setup() {
                originalPatient = se.inera.intyg.common.support.facade.model.Patient.builder()
                    .addressFromPU(false)
                    .street("Manually entered street")
                    .city("Manually entered city")
                    .zipCode("Manually entered zipcode")
                    .build();

                doReturn(createPUPatient(false))
                    .when(patientDetailsResolver)
                    .resolvePatient(any(Personnummer.class), eq(CERTIFICATE_TYPE), eq(CERTIFICATE_TYPE_VERSION));
                doReturn(true).when(webCertUserService).hasAuthenticationContext();
                doReturn(user).when(webCertUserService).getUser();
            }

            @Test
            public void shallConvertPatientCity() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(originalPatient.getCity(), patient.getCity());
            }

            @Test
            public void shallConvertPatientZipCode() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(originalPatient.getZipCode(), patient.getZipCode());
            }

            @Test
            public void shallConvertPatientStreet() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(originalPatient.getStreet(), patient.getStreet());
            }

            @Test
            public void shallConvertPatientAddressFromPU() {
                final var patient = patientConverter.convert(originalPatient, PERSON_NUMMER, CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);
                assertEquals(originalPatient.isAddressFromPU(), patient.isAddressFromPU());
            }
        }
    }

    private IntegrationParameters getIntegrationParameters(String alternateSsn, String firstname, String lastname) {
        return new IntegrationParameters("reference", "responsible", alternateSsn, firstname, "mellannamn", lastname,
            "address", "zipcode", "city", true, false, false, true, null);
    }

    private Utkast createDraftWithUpdatedPersonId() {
        final var draft = new Utkast();
        draft.setIntygsId("certificateId");
        draft.setIntygsTyp("certificateType");
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setModel("draftJson");
        draft.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        draft.setSkapad(LocalDateTime.now());
        draft.setPatientPersonnummer(Personnummer.createPersonnummer(ALTERNATE_PATIENT_ID).orElseThrow());
        draft.setPatientEfternamn(LASTNAME);
        draft.setPatientFornamn(FIRSTNAME);
        draft.setPatientMellannamn(MIDDLENAME);
        draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
        return draft;
    }

    private Patient createPUPatient(boolean isAddressDetailsSourcePU) {
        final var patient = new Patient();
        patient.setPersonId(Personnummer.createPersonnummer(PATIENT_ID).orElseThrow());
        patient.setEfternamn(LASTNAME);
        patient.setFornamn(FIRSTNAME);
        patient.setMellannamn(MIDDLENAME);
        if (isAddressDetailsSourcePU) {
            patient.setPostadress("Storgatan 1");
            patient.setPostnummer("831 33");
            patient.setPostort("Östersund");
        }
        patient.setAddressDetailsSourcePU(isAddressDetailsSourcePU);
        return patient;
    }

    private Patient createPUPatientWithNoName() {
        final var patient = new Patient();
        patient.setPersonId(Personnummer.createPersonnummer(PATIENT_ID).orElseThrow());
        patient.setEfternamn(null);
        patient.setFornamn(null);
        patient.setMellannamn(null);
        return patient;
    }
}
