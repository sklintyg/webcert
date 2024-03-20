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
package se.inera.intyg.webcert.web.csintegration.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificatesQueryCriteriaDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdDTO;
import se.inera.intyg.webcert.web.csintegration.patient.PersonIdType;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitDTO;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;

@ExtendWith(MockitoExtension.class)
class CSIntegrationRequestFactoryTest {

    @Mock
    CertificateServiceUnitHelper certificateServiceUnitHelper;
    @Mock
    CertificateServiceUserHelper certificateServiceUserHelper;
    @Mock
    CertificateServicePatientHelper certificateServicePatientHelper;
    @Mock
    CertificatesQueryCriteriaFactory certificatesQueryCriteriaFactory;
    @InjectMocks
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    private static final String TYPE = "TYPE";
    private static final String VERSION = "VERSION";
    private static final CertificateModelIdDTO CERTIFICATE_MODEL_ID = CertificateModelIdDTO.builder()
        .type(TYPE)
        .version(VERSION)
        .build();
    private static final Certificate CERTIFICATE = new Certificate();
    private static final String PATIENT_ID = "191212121212";
    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer(PATIENT_ID).orElseThrow();
    private static final CertificateServiceUserDTO USER = CertificateServiceUserDTO.builder().build();
    private static final CertificateServiceUnitDTO UNIT = CertificateServiceUnitDTO.builder().build();
    private static final CertificateServiceUnitDTO CARE_UNIT = CertificateServiceUnitDTO.builder().build();
    private static final CertificateServiceUnitDTO CARE_PROVIDER = CertificateServiceUnitDTO.builder().build();
    private static final CertificateServicePatientDTO PATIENT = CertificateServicePatientDTO.builder().build();
    private static final ListFilter LIST_FILTER = new ListFilter();
    private static final QueryIntygParameter QUERY_INTYG_PARAMETER = new QueryIntygParameter();
    private static final CertificatesQueryCriteriaDTO CERTIFICATES_QUERY_CRITERIA_DTO = CertificatesQueryCriteriaDTO.builder().build();
    private static final String ADDITIONAL_INFO_TEXT = "ADDITIONAL_INFO_TEXT";

    static {
        CERTIFICATE.setMetadata(
            CertificateMetadata.builder()
                .patient(
                    Patient.builder()
                        .personId(
                            PersonId.builder()
                                .id(PATIENT_ID)
                                .build()
                        )
                        .build()
                )
                .build()
        );
    }

    @Test
    void shouldThrowExceptionIfPatientIdIsIncorrectFormat() {
        assertThrows(IllegalArgumentException.class,
            () -> csIntegrationRequestFactory.createCertificateRequest(CERTIFICATE_MODEL_ID, "wrong-patient-id")
        );
    }

    @Nested
    class CertificateTypeRequest {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServicePatientHelper.get(PERSONNUMMER))
                .thenReturn(PATIENT);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateTypesRequest(PERSONNUMMER);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateTypesRequest(PERSONNUMMER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateTypesRequest(PERSONNUMMER);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateTypesRequest(PERSONNUMMER);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateTypesRequest(PERSONNUMMER);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class CreateCertificateRequest {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServicePatientHelper.get(PERSONNUMMER))
                .thenReturn(PATIENT);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetCertificateModel() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(CERTIFICATE_MODEL_ID, actualRequest.getCertificateModelId());
        }
    }

    @Nested
    class GetCertificateRequest {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class SaveCertificateRequest {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetCertificate() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE);
            assertEquals(CERTIFICATE, actualRequest.getCertificate());
        }
    }

    @Nested
    class DeleteCertificateRequest {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.deleteCertificateRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.deleteCertificateRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.deleteCertificateRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.deleteCertificateRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class GetPatientCertificatesRequest {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
            when(certificateServicePatientHelper.get(PERSONNUMMER))
                .thenReturn(PATIENT);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.getPatientCertificatesRequest(PATIENT_ID);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getPatientCertificatesRequest(PATIENT_ID);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getPatientCertificatesRequest(PATIENT_ID);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getPatientCertificatesRequest(PATIENT_ID);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.getPatientCertificatesRequest(PATIENT_ID);
            assertEquals(PATIENT, actualRequest.getPatient());
        }
    }

    @Nested
    class GetUnitCertificatesRequestFromListFilter {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            when(certificatesQueryCriteriaFactory.create(LIST_FILTER)).thenReturn(CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(LIST_FILTER);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            when(certificatesQueryCriteriaFactory.create(LIST_FILTER)).thenReturn(CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(LIST_FILTER);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            when(certificatesQueryCriteriaFactory.create(LIST_FILTER)).thenReturn(CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(LIST_FILTER);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            when(certificatesQueryCriteriaFactory.create(LIST_FILTER)).thenReturn(CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(LIST_FILTER);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatientIfIdIsDefined() {
            final var queryCriteriaDTO = CertificatesQueryCriteriaDTO.builder()
                .personId(
                    PersonIdDTO.builder()
                        .id(PATIENT_ID)
                        .type(PersonIdType.PERSONAL_IDENTITY_NUMBER)
                        .build()
                )
                .build();
            when(certificatesQueryCriteriaFactory.create(LIST_FILTER)).thenReturn(queryCriteriaDTO);
            when(certificateServicePatientHelper.get(PERSONNUMMER)).thenReturn(PATIENT);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(LIST_FILTER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }
    }

    @Nested
    class GetUnitCertificatesRequestFromCriteriaIntygFilter {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            when(certificatesQueryCriteriaFactory.create(QUERY_INTYG_PARAMETER)).thenReturn(CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(QUERY_INTYG_PARAMETER);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            when(certificatesQueryCriteriaFactory.create(QUERY_INTYG_PARAMETER)).thenReturn(CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(QUERY_INTYG_PARAMETER);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            when(certificatesQueryCriteriaFactory.create(QUERY_INTYG_PARAMETER)).thenReturn(CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(QUERY_INTYG_PARAMETER);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            when(certificatesQueryCriteriaFactory.create(QUERY_INTYG_PARAMETER)).thenReturn(CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(QUERY_INTYG_PARAMETER);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatientIfIdIsDefined() {
            final var queryCriteriaDTO = CertificatesQueryCriteriaDTO.builder()
                .personId(
                    PersonIdDTO.builder()
                        .id(PATIENT_ID)
                        .type(PersonIdType.PERSONAL_IDENTITY_NUMBER)
                        .build()
                )
                .build();
            when(certificatesQueryCriteriaFactory.create(QUERY_INTYG_PARAMETER)).thenReturn(queryCriteriaDTO);
            when(certificateServicePatientHelper.get(PERSONNUMMER)).thenReturn(PATIENT);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(QUERY_INTYG_PARAMETER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }
    }

    @Nested
    class GetUnitCertificatesInfoRequest {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesInfoRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesInfoRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesInfoRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesInfoRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class ValidateCertificateRequest {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
            when(certificateServicePatientHelper.get(PERSONNUMMER))
                .thenReturn(PATIENT);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(CERTIFICATE);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(CERTIFICATE);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(CERTIFICATE);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(CERTIFICATE);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(CERTIFICATE);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetCertificate() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(CERTIFICATE);
            assertEquals(CERTIFICATE, actualRequest.getCertificate());
        }
    }

    @Nested
    class PrintCertificateRequest {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
            when(certificateServicePatientHelper.get(PERSONNUMMER))
                .thenReturn(PATIENT);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetAdditionalInfoText() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(ADDITIONAL_INFO_TEXT, actualRequest.getAdditionalInfoText());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(PATIENT, actualRequest.getPatient());
        }
    }

    @Nested
    class GetCertificateXmlRequest {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateXmlRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateXmlRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateXmlRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateXmlRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class SignCertificateRequest {

        private static final String SIGNATURE_XML = "signatureXml";

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.signCertificateRequest(SIGNATURE_XML);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.signCertificateRequest(SIGNATURE_XML);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.signCertificateRequest(SIGNATURE_XML);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.signCertificateRequest(SIGNATURE_XML);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetBase64EncodedSignatureXml() {
            final var actualRequest = csIntegrationRequestFactory.signCertificateRequest(SIGNATURE_XML);
            assertEquals(Base64.getEncoder().encodeToString(SIGNATURE_XML.getBytes(StandardCharsets.UTF_8)),
                actualRequest.getSignatureXml());
        }
    }

    @Nested
    class SignCertificateWithoutSignatureRequest {

        private static final String SIGNATURE_XML = "signatureXml";

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.signCertificateWithoutSignatureRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.signCertificateWithoutSignatureRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.signCertificateWithoutSignatureRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.signCertificateWithoutSignatureRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }
}
