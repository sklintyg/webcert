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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class DecorateCertificateFromCSWithInformationFromWCTest {

    private static final String ID_TYPE = "PERSON_NUMMER";
    private static final String PATIENT_ID = "19121212-1212";
    private static final String ALTERNATE_PATIENT_ID = "19121212-1213";
    private static final String PATIENT_RESERVE_ID = "19121212-12AB";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String ALTERNATE_FIRST_NAME = "alternateFirstName";
    private static final String ALTERNATE_LAST_NAME = "alternateLastName";
    private static final WebCertUser user = new WebCertUser();

    @Mock
    DecorateCertificateDataService decorateCertificateDataService;
    @Mock
    WebCertUserService webCertUserService;
    @Mock
    WebCertUser webCertUser;
    @InjectMocks
    DecorateCertificateFromCSWithInformationFromWC decorateCertificateFromCSWithInformationFromWC;

    @BeforeEach
    void setUp() {
        when(webCertUserService.getUser())
            .thenReturn(webCertUser);
    }

    @Test
    void shallNotDecorateCertificateIfUserIsNull() {
        when(webCertUserService.getUser())
            .thenReturn(null);

        final var certificate = createCertificate();
        decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

        assertNull(certificate.getMetadata().getPatient().getPreviousPersonId());
        assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        assertFalse(certificate.getMetadata().getPatient().isDifferentNameFromEHR());
        assertFalse(certificate.getMetadata().getPatient().isReserveId());
    }

    @Test
    void shallNotDecorateCertificateIfParametersIsNull() {
        when(webCertUser.getParameters())
            .thenReturn(null);

        final var certificate = createCertificate();
        decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

        assertNull(certificate.getMetadata().getPatient().getPreviousPersonId());
        assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        assertFalse(certificate.getMetadata().getPatient().isDifferentNameFromEHR());
        assertFalse(certificate.getMetadata().getPatient().isReserveId());
    }

    @Test
    void shallNotChangeOtherValuesThanPatient() {
        final var parameters = createIntegrationParameters("", FIRST_NAME, LAST_NAME);
        user.setParameters(parameters);

        when(webCertUser.getParameters())
            .thenReturn(parameters);

        final var description = "description";

        final var unit = Unit.builder()
            .unitId("unitId")
            .unitName("unitName")
            .build();

        final var certificate = createCertificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .status(CertificateStatus.SIGNED)
                .description(description)
                .patient(Patient.builder()
                    .personId(PersonId.builder()
                        .id(PATIENT_ID)
                        .type(ID_TYPE)
                        .build())
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build()
                )
                .unit(unit)
                .build()
        );

        decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

        assertEquals(unit, certificate.getMetadata().getUnit());
        assertEquals(description, certificate.getMetadata().getDescription());
    }

    @Nested
    class PatientId {

        @Test
        void shallHaveAlternateSSNAsPatientIdIfPatientIdHasBeenReplaced() {
            final var parameters = createIntegrationParameters(ALTERNATE_PATIENT_ID, FIRST_NAME, LAST_NAME);
            parameters.setBeforeAlternateSsn(PATIENT_ID);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertEquals(ALTERNATE_PATIENT_ID, certificate.getMetadata().getPatient().getPersonId().getId());
            assertTrue(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallHaveOriginalIdIfAlternateSSNIsEmpty() {
            final var parameters = createIntegrationParameters("", FIRST_NAME, LAST_NAME);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertEquals(PATIENT_ID, certificate.getMetadata().getPatient().getPersonId().getId());
            assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallHaveOriginalPatientIdIfAlternateSSNIsNull() {
            final var parameters = createIntegrationParameters(null, FIRST_NAME, LAST_NAME);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertEquals(PATIENT_ID, certificate.getMetadata().getPatient().getPersonId().getId());
            assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallHaveAlternateSSNAsPatientIdIfSet() {
            final var parameters = createIntegrationParameters(ALTERNATE_PATIENT_ID, FIRST_NAME, LAST_NAME);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertEquals(ALTERNATE_PATIENT_ID, certificate.getMetadata().getPatient().getPersonId().getId());
            assertTrue(certificate.getMetadata().getPatient().isPersonIdChanged());
        }
    }

    @Nested
    class PreviousId {

        @Test
        void shallNotSetPreviousIdIfAlternateSSNIsEmpty() {
            final var parameters = createIntegrationParameters("", FIRST_NAME, LAST_NAME);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertNull(certificate.getMetadata().getPatient().getPreviousPersonId());
            assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallNotSetPreviousIdIfAlternateSSNIsNull() {
            final var parameters = createIntegrationParameters(null, FIRST_NAME, LAST_NAME);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertNull(certificate.getMetadata().getPatient().getPreviousPersonId());
            assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallSetPreviousPersonIdToOriginalIdIfPersonIdIsReplaced() {
            final var parameters = createIntegrationParameters(ALTERNATE_PATIENT_ID, FIRST_NAME, LAST_NAME);
            parameters.setBeforeAlternateSsn(PATIENT_ID);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertEquals(PATIENT_ID, certificate.getMetadata().getPatient().getPreviousPersonId().getId());
            assertTrue(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallSetPreviousPersonIdToOriginalIdIfPersonIdIsNotReplaced() {
            final var parameters = createIntegrationParameters(ALTERNATE_PATIENT_ID, FIRST_NAME, LAST_NAME);
            parameters.setBeforeAlternateSsn(PATIENT_ID);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertEquals(PATIENT_ID, certificate.getMetadata().getPatient().getPreviousPersonId().getId());
            assertTrue(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallSetPreviousPersonIdToOriginalIdIfAlternatePatientSSnIsReserveId() {
            final var parameters = createIntegrationParameters(PATIENT_RESERVE_ID, FIRST_NAME, LAST_NAME);
            parameters.setBeforeAlternateSsn(PATIENT_ID);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertEquals(PATIENT_ID, certificate.getMetadata().getPatient().getPreviousPersonId().getId());
            assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallSetPreviousIdWithCorrectFormatIfBeforeAlternateSsnIsNotAReserveId() {
            final var parameters = createIntegrationParameters(PATIENT_RESERVE_ID, FIRST_NAME, LAST_NAME);
            parameters.setBeforeAlternateSsn(PATIENT_ID.replace("-", ""));
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertEquals(PATIENT_ID, certificate.getMetadata().getPatient().getPreviousPersonId().getId());
            assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallSetPreviousIdWithCorrectFormatIfBeforeAlternateSsnIsAReserveId() {
            final var parameters = createIntegrationParameters(PATIENT_ID, FIRST_NAME, LAST_NAME);
            parameters.setBeforeAlternateSsn(PATIENT_RESERVE_ID);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertEquals(PATIENT_RESERVE_ID, certificate.getMetadata().getPatient().getPreviousPersonId().getId());
            assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        }
    }

    @Nested
    class PersonIdChanged {

        @Test
        void shallSetFlagIfAlternateAndBeforeSsnIsSetAndDiffers() {
            final var parameters = createIntegrationParameters(ALTERNATE_PATIENT_ID, FIRST_NAME, LAST_NAME);
            parameters.setBeforeAlternateSsn(PATIENT_ID);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertTrue(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallSetFlagIfBeforeSsnIsNotSetButPersonIdAndAlternateSsnDiffers() {
            final var parameters = createIntegrationParameters(ALTERNATE_PATIENT_ID, FIRST_NAME, LAST_NAME);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertTrue(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallNotSetFlagIfPersonIdIsNotValid() {
            final var parameters = createIntegrationParameters(PATIENT_RESERVE_ID, FIRST_NAME, LAST_NAME);
            parameters.setBeforeAlternateSsn(PATIENT_ID);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        }

        @Test
        void shallNotSetFlagForPersonIdIfNoParametersAreSent() {
            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertFalse(certificate.getMetadata().getPatient().isPersonIdChanged());
        }
    }

    @Nested
    class PatientNameDifferent {

        @Test
        void shallSetIsPatientNameDifferentIfFirstnameChanges() {
            final var parameters = createIntegrationParameters(PATIENT_ID, ALTERNATE_FIRST_NAME, LAST_NAME);
            user.setParameters(parameters);
            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertTrue(certificate.getMetadata().getPatient().isDifferentNameFromEHR());
        }

        @Test
        void shallSetIsPatientNameDifferentIfLastnameChanges() {
            final var parameters = createIntegrationParameters(PATIENT_ID, FIRST_NAME, ALTERNATE_LAST_NAME);
            user.setParameters(parameters);
            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertTrue(certificate.getMetadata().getPatient().isDifferentNameFromEHR());
        }

        @Test
        void shallSetIsPatientNameDifferentIfBothChange() {
            final var parameters = createIntegrationParameters(PATIENT_ID, ALTERNATE_FIRST_NAME, ALTERNATE_LAST_NAME);
            user.setParameters(parameters);
            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertTrue(certificate.getMetadata().getPatient().isDifferentNameFromEHR());
        }

        @Test
        void shallNotSetIsPatientNameDifferentIfNameIsSame() {
            final var parameters = createIntegrationParameters(PATIENT_ID, FIRST_NAME, LAST_NAME);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertFalse(certificate.getMetadata().getPatient().isDifferentNameFromEHR());
        }
    }

    @Nested
    class ReserveId {

        @Test
        void shallSetReserveIdFlagIfPersonIdIsReserveId() {
            final var parameters = createIntegrationParameters(PATIENT_RESERVE_ID, FIRST_NAME, LAST_NAME);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertTrue(certificate.getMetadata().getPatient().isReserveId());
        }

        @Test
        void shallNotSetReserveIdFlagIfPersonIdIsValid() {
            final var parameters = createIntegrationParameters(PATIENT_ID, FIRST_NAME, LAST_NAME);
            user.setParameters(parameters);

            when(webCertUser.getParameters())
                .thenReturn(parameters);

            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);

            assertFalse(certificate.getMetadata().getPatient().isReserveId());
        }
    }

    @Nested
    class DecorateRenewedCertificateFromParentTests {

        @Test
        void shouldNotDecorateIfCertificateIsNotDraft() {
            final var certificate = createCertificate();
            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);
            verifyNoInteractions(decorateCertificateDataService);
        }

        @Test
        void shouldNotDecorateIfCertificateIsDraftButHasNoParentRelation() {
            final var certificate = createCertificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .status(CertificateStatus.UNSIGNED)
                    .relations(
                        CertificateRelations.builder()
                            .parent(null)
                            .build()
                    )
                    .build()
            );

            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);
            verifyNoInteractions(decorateCertificateDataService);
        }

        @Test
        void shouldNotDecorateIfCertificateIsDraftButHasNoParentRelationOfTypeExtended() {
            final var certificate = createCertificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .status(CertificateStatus.UNSIGNED)
                    .relations(
                        CertificateRelations.builder()
                            .parent(
                                CertificateRelation.builder()
                                    .type(CertificateRelationType.COMPLEMENTED)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );

            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);
            verifyNoInteractions(decorateCertificateDataService);
        }

        @Test
        void shouldDecorateIfCertificateIsDraftAndHasParentRelationOfTypeExtended() {
            final var certificate = createCertificate();
            certificate.setMetadata(
                CertificateMetadata.builder()
                    .status(CertificateStatus.UNSIGNED)
                    .relations(
                        CertificateRelations.builder()
                            .parent(
                                CertificateRelation.builder()
                                    .type(CertificateRelationType.EXTENDED)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );

            decorateCertificateFromCSWithInformationFromWC.decorate(certificate);
            verify(decorateCertificateDataService).decorateFromParent(certificate);
        }
    }

    private IntegrationParameters createIntegrationParameters(String alternateSsn, String firstName, String lastName) {
        return new IntegrationParameters("reference", "responsible", alternateSsn, firstName, "mellannamn", lastName,
            "address", "zipcode", "city", true, false, false, true, null);
    }

    private Certificate createCertificate() {
        final var certificate = new Certificate();
        certificate.setMetadata(
            CertificateMetadata.builder()
                .status(CertificateStatus.SIGNED)
                .patient(Patient.builder()
                    .personId(PersonId.builder()
                        .id(PATIENT_ID)
                        .type(ID_TYPE)
                        .build())
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build())
                .build()
        );

        return certificate;
    }
}