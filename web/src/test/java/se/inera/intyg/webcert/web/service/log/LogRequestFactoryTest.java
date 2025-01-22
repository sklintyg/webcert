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
package se.inera.intyg.webcert.web.service.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.log.factory.LogRequestFactoryImpl;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
public class LogRequestFactoryTest {

    private static final String CERTIFICATE_ID = "intygsId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String PATIENT_ID = "20121212-1212";
    private static final String PATIENT_FIRST_NAME = "fornamn";
    private static final String PATIENT_MIDDLE_NAME = "mellannamn";
    private static final String PATIENT_LAST_NAME = "efternamn";
    private static final String UNIT_ID = "enhetsid";
    private static final String UNIT_NAME = "enhetsnamn";
    private static final String CARE_PROVIDER_ID = "vardgivarid";
    private static final String CARE_PROVIDER_NAME = "vardgivarnamn";
    private static final String SJF_LOG_POST = "Läsning i enlighet med sammanhållen journalföring";
    private static final Personnummer PATIENT_PERSONNUMMER = Personnummer.createPersonnummer(PATIENT_ID).orElseThrow();
    private static final String ADDITIONAL_INFO = "additionalInfo";

    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @InjectMocks
    private LogRequestFactoryImpl logRequestFactory;

    @Nested
    class CreateLogRequestFromUtkast {

        private Utkast utkast;

        @BeforeEach
        void setUp() {
            utkast = createUtkast();
        }

        @Test
        void shallReturnIntygsId() {
            assertEquals(CERTIFICATE_ID, logRequestFactory.createLogRequestFromUtkast(utkast).getIntygId());
        }

        @Test
        void shallReturnPatientId() {
            assertEquals(PATIENT_ID, logRequestFactory.createLogRequestFromUtkast(utkast).getPatientId().getPersonnummerWithDash());
        }

        @Test
        void shallReturnCareUnitId() {
            assertEquals(UNIT_ID, logRequestFactory.createLogRequestFromUtkast(utkast).getIntygCareUnitId());
        }

        @Test
        void shallReturnCareUnitName() {
            assertEquals(UNIT_NAME, logRequestFactory.createLogRequestFromUtkast(utkast).getIntygCareUnitName());
        }

        @Test
        void shallReturnCareGiverId() {
            assertEquals(CARE_PROVIDER_ID, logRequestFactory.createLogRequestFromUtkast(utkast).getIntygCareGiverId());
        }

        @Test
        void shallReturnCareGiverName() {
            assertEquals(CARE_PROVIDER_NAME, logRequestFactory.createLogRequestFromUtkast(utkast).getIntygCareGiverName());
        }

        @Test
        void shallNotIncludeAdditionalInfoIfNotSjf() {
            assertNull(logRequestFactory.createLogRequestFromUtkast(utkast).getAdditionalInfo());
        }

        @Test
        void shallReturnAdditionalInfoForSjf() {
            assertEquals(SJF_LOG_POST, logRequestFactory.createLogRequestFromUtkast(utkast, true).getAdditionalInfo());
        }

        @Test
        void shallNotIncludePatientNameBecauseRequirementToHandleProtectedPersons() {
            assertNull(logRequestFactory.createLogRequestFromUtkast(utkast).getPatientName());
        }
    }

    @Nested
    class CreateLogRequestFromUtlatande {

        private Utlatande utlatande;

        @BeforeEach
        void setUp() {
            utlatande = createUtlatande();
        }

        @Test
        void shallReturnIntygsId() {
            assertEquals(CERTIFICATE_ID, logRequestFactory.createLogRequestFromUtlatande(utlatande).getIntygId());
        }

        @Test
        void shallReturnPatientId() {
            assertEquals(PATIENT_ID, logRequestFactory.createLogRequestFromUtlatande(utlatande).getPatientId().getPersonnummerWithDash());
        }

        @Test
        void shallReturnCareUnitId() {
            assertEquals(UNIT_ID, logRequestFactory.createLogRequestFromUtlatande(utlatande).getIntygCareUnitId());
        }

        @Test
        void shallReturnCareUnitName() {
            assertEquals(UNIT_NAME, logRequestFactory.createLogRequestFromUtlatande(utlatande).getIntygCareUnitName());
        }

        @Test
        void shallReturnCareGiverId() {
            assertEquals(CARE_PROVIDER_ID, logRequestFactory.createLogRequestFromUtlatande(utlatande).getIntygCareGiverId());
        }

        @Test
        void shallReturnCareGiverName() {
            assertEquals(CARE_PROVIDER_NAME, logRequestFactory.createLogRequestFromUtlatande(utlatande).getIntygCareGiverName());
        }

        @Test
        void shallNotIncludeAdditionalInfoIfNotSjf() {
            assertNull(logRequestFactory.createLogRequestFromUtlatande(utlatande).getAdditionalInfo());
        }

        @Test
        void shallReturnAdditionalInfoForSjf() {
            assertEquals(SJF_LOG_POST, logRequestFactory.createLogRequestFromUtlatande(utlatande, true).getAdditionalInfo());
        }

        @Test
        void shallNotIncludePatientNameBecauseRequirementToHandleProtectedPersons() {
            assertNull(logRequestFactory.createLogRequestFromUtlatande(utlatande).getPatientName());
        }

        @Test
        void shallReturnAdditionalInfoIfProvided() {
            assertEquals(ADDITIONAL_INFO, logRequestFactory.createLogRequestFromUtlatande(utlatande, ADDITIONAL_INFO).getAdditionalInfo());
        }

        @Test
        void shallNotIncludeAdditionalInfoIfNotProvided() {
            assertNull(logRequestFactory.createLogRequestFromUtlatande(utlatande, null).getAdditionalInfo());
        }
    }

    @Nested
    class CreateLogRequestFromUser {

        @Test
        void shallReturnIntygsId() {
            assertEquals(CERTIFICATE_ID,
                logRequestFactory.createLogRequestFromUser(createUser(false), PATIENT_ID, CERTIFICATE_ID).getIntygId());
        }

        @Test
        void shallReturnPatientId() {
            assertEquals(PATIENT_ID,
                logRequestFactory.createLogRequestFromUser(createUser(false), PATIENT_ID).getPatientId().getPersonnummerWithDash());
        }

        @Test
        void shallReturnCareUnitId() {
            assertEquals(UNIT_ID, logRequestFactory.createLogRequestFromUser(createUser(false), PATIENT_ID).getIntygCareUnitId());
        }

        @Test
        void shallReturnCareUnitName() {
            assertEquals(UNIT_NAME, logRequestFactory.createLogRequestFromUser(createUser(false), PATIENT_ID).getIntygCareUnitName());
        }

        @Test
        void shallReturnCareGiverId() {
            assertEquals(CARE_PROVIDER_ID, logRequestFactory.createLogRequestFromUser(createUser(false), PATIENT_ID).getIntygCareGiverId());
        }

        @Test
        void shallReturnCareGiverName() {
            assertEquals(CARE_PROVIDER_NAME,
                logRequestFactory.createLogRequestFromUser(createUser(false), PATIENT_ID).getIntygCareGiverName());
        }

        @Test
        void shallReturnTestIntygFalseWhenNotTestIndicated() {
            assertFalse(logRequestFactory.createLogRequestFromUser(createUser(false), PATIENT_ID).isTestIntyg());
        }

        @Test
        void shallReturnTestIntygTrueWhenTestIndicated() {
            doReturn(true).when(patientDetailsResolver).isTestIndicator(any(Personnummer.class));
            assertTrue(logRequestFactory.createLogRequestFromUser(createUser(false), PATIENT_ID).isTestIntyg());
        }

        @Test
        void shallNotIncludeAdditionalInfoIfNotSjf() {
            assertNull(logRequestFactory.createLogRequestFromUser(createUser(false), PATIENT_ID).getAdditionalInfo());
        }

        @Test
        void shallReturnAdditionalInfoForSjf() {
            assertEquals(SJF_LOG_POST, logRequestFactory.createLogRequestFromUser(createUser(true), PATIENT_ID).getAdditionalInfo());
        }

        @Test
        void shallNotIncludePatientNameBecauseRequirementToHandleProtectedPersons() {
            assertNull(logRequestFactory.createLogRequestFromUser(createUser(false), PATIENT_ID).getPatientName());
        }

        @Test
        void shallThrowExceptionIfPatientIdIsOfWrongFormat() {
            final var user = new WebCertUser();
            assertThrows(IllegalArgumentException.class,
                () -> logRequestFactory.createLogRequestFromUser(user, "wrong-format")
            );
        }
    }

    @Nested
    class CreateLogRequestFromCertificate {

        private Certificate certificate;

        @BeforeEach
        void setUp() {
            certificate = createCertificate();
        }

        @Test
        void shallReturnIntygsId() {
            assertEquals(CERTIFICATE_ID, logRequestFactory.createLogRequestFromCertificate(certificate, null).getIntygId());
        }

        @Test
        void shallReturnPatientId() {
            assertEquals(PATIENT_ID,
                logRequestFactory.createLogRequestFromCertificate(certificate, null).getPatientId().getPersonnummerWithDash()
            );
        }

        @Test
        void shallReturnCareUnitId() {
            assertEquals(UNIT_ID, logRequestFactory.createLogRequestFromCertificate(certificate, null).getIntygCareUnitId());
        }

        @Test
        void shallReturnCareUnitName() {
            assertEquals(UNIT_NAME, logRequestFactory.createLogRequestFromCertificate(certificate, null).getIntygCareUnitName());
        }

        @Test
        void shallReturnCareGiverId() {
            assertEquals(CARE_PROVIDER_ID, logRequestFactory.createLogRequestFromCertificate(certificate, null).getIntygCareGiverId());
        }

        @Test
        void shallReturnCareGiverName() {
            assertEquals(CARE_PROVIDER_NAME, logRequestFactory.createLogRequestFromCertificate(certificate, null).getIntygCareGiverName());
        }

        @Test
        void shallNotIncludeAdditionalInfoIfNotSjf() {
            assertNull(logRequestFactory.createLogRequestFromCertificate(certificate, null).getAdditionalInfo());
        }

        @Test
        void shallReturnAdditional() {
            assertEquals(SJF_LOG_POST, logRequestFactory.createLogRequestFromCertificate(certificate, SJF_LOG_POST).getAdditionalInfo());
        }

        @Test
        void shallNotIncludePatientNameBecauseRequirementToHandleProtectedPersons() {
            assertNull(logRequestFactory.createLogRequestFromCertificate(certificate, null).getPatientName());
        }
    }

    private WebCertUser createUser(boolean sjf) {
        final var user = mock(WebCertUser.class);

        final var expectedCareUnit = new se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet();
        expectedCareUnit.setId(UNIT_ID);
        expectedCareUnit.setNamn(UNIT_NAME);
        doReturn(expectedCareUnit).when(user).getValdVardenhet();

        final var expectedCareProvider = new se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare();
        expectedCareProvider.setId(CARE_PROVIDER_ID);
        expectedCareProvider.setNamn(CARE_PROVIDER_NAME);
        doReturn(expectedCareProvider).when(user).getValdVardgivare();

        final var parameters = IntegrationParameters
            .of(null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                sjf,
                false,
                false,
                false
            );

        doReturn(parameters).when(user).getParameters();
        return user;
    }


    private Utkast createUtkast() {
        final var utkast = new Utkast();
        utkast.setIntygsId(CERTIFICATE_ID);
        utkast.setIntygsTyp(CERTIFICATE_TYPE);
        utkast.setPatientPersonnummer(PATIENT_PERSONNUMMER);
        utkast.setPatientFornamn(PATIENT_FIRST_NAME);
        utkast.setPatientMellannamn(PATIENT_MIDDLE_NAME);
        utkast.setPatientEfternamn(PATIENT_LAST_NAME);
        utkast.setEnhetsId(UNIT_ID);
        utkast.setEnhetsNamn(UNIT_NAME);
        utkast.setVardgivarId(CARE_PROVIDER_ID);
        utkast.setVardgivarNamn(CARE_PROVIDER_NAME);
        return utkast;
    }

    private static Utlatande createUtlatande() {
        final var utlatande = mock(Utlatande.class);
        final String patientNamn = String.join(" ", PATIENT_FIRST_NAME, PATIENT_MIDDLE_NAME, PATIENT_LAST_NAME);
        GrundData grundData = new GrundData();
        grundData.setPatient(new Patient());
        grundData.getPatient().setPersonId(PATIENT_PERSONNUMMER);
        grundData.getPatient().setFullstandigtNamn(patientNamn);
        grundData.setSkapadAv(new HoSPersonal());
        grundData.getSkapadAv().setVardenhet(new Vardenhet());
        grundData.getSkapadAv().getVardenhet().setEnhetsid(UNIT_ID);
        grundData.getSkapadAv().getVardenhet().setEnhetsnamn(UNIT_NAME);
        grundData.getSkapadAv().getVardenhet().setVardgivare(new Vardgivare());
        grundData.getSkapadAv().getVardenhet().getVardgivare().setVardgivarid(CARE_PROVIDER_ID);
        grundData.getSkapadAv().getVardenhet().getVardgivare().setVardgivarnamn(CARE_PROVIDER_NAME);

        when(utlatande.getId()).thenReturn(CERTIFICATE_ID);
        when(utlatande.getGrundData()).thenReturn(grundData);

        return utlatande;
    }

    private Certificate createCertificate() {
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .id(CERTIFICATE_ID)
                .type(CERTIFICATE_TYPE)
                .unit(
                    Unit.builder()
                        .unitId(UNIT_ID)
                        .unitName(UNIT_NAME)
                        .build()
                )
                .careProvider(
                    Unit.builder()
                        .unitId(CARE_PROVIDER_ID)
                        .unitName(CARE_PROVIDER_NAME)
                        .build()
                )
                .patient(
                    se.inera.intyg.common.support.facade.model.Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(PATIENT_ID)
                                .build()
                        )
                        .firstName(PATIENT_FIRST_NAME)
                        .middleName(PATIENT_MIDDLE_NAME)
                        .lastName(PATIENT_LAST_NAME)
                        .fullName(
                            String.join(" ", PATIENT_FIRST_NAME, PATIENT_MIDDLE_NAME, PATIENT_LAST_NAME)
                        )
                        .build()
                )
                .build()
        );
        return certificate;
    }
}
