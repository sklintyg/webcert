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
package se.inera.intyg.webcert.web.csintegration.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.dto.IncomingMessageRequestDTO;
import se.inera.intyg.webcert.common.dto.PersonIdDTO;
import se.inera.intyg.webcert.common.dto.PersonIdType;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificateModelIdDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.CertificatesQueryCriteriaDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificatePdfRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCitizenCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.MessageQueryCriteriaDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.PrefillXmlDTO;
import se.inera.intyg.webcert.web.csintegration.message.MessageRequestConverter;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientDTO;
import se.inera.intyg.webcert.web.csintegration.patient.CertificateServicePatientHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceIntegrationUnitHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitDTO;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceUnitHelper;
import se.inera.intyg.webcert.web.csintegration.unit.CertificateServiceVardenhetConverter;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceIntegrationUserHelper;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserDTO;
import se.inera.intyg.webcert.web.csintegration.user.CertificateServiceUserHelper;
import se.inera.intyg.webcert.web.service.facade.list.dto.ListFilter;
import se.inera.intyg.webcert.web.web.controller.api.dto.QueryIntygParameter;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.sendMessageToCare.v2.SendMessageToCareType;
import se.riv.clinicalprocess.healthcond.certificate.v3.Svar;
import se.riv.clinicalprocess.healthcond.certificate.v33.Forifyllnad;

@ExtendWith(MockitoExtension.class)
class CSIntegrationRequestFactoryTest {

    private static final String MESSAGE = "message";
    private static final Question QUESTION = Question.builder().build();
    private static final List<String> UNIT_IDS = List.of("unitId");
    @Mock
    CertificateServiceVardenhetConverter certificateServiceVardenhetConverter;
    @Mock
    MessageRequestConverter messageRequestConverter;
    @Mock
    CertificateServiceUnitHelper certificateServiceUnitHelper;
    @Mock
    CertificateServiceIntegrationUnitHelper certificateServiceIntegrationUnitHelper;
    @Mock
    CertificateServiceIntegrationUserHelper certificateServiceIntegrationUserHelper;
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
    private static final String COORDINATION_NUMBER_PATIENT_ID = "191212721212";
    private static final String EXTERNAL_REFERENCE = "REF";
    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer(PATIENT_ID)
        .orElseThrow();
    private static final Personnummer COORDINATION_PERSONNUMMER = Personnummer.createPersonnummer(
            COORDINATION_NUMBER_PATIENT_ID)
        .orElseThrow();
    private static final CertificateServiceUserDTO USER = CertificateServiceUserDTO.builder().build();
    private static final CertificateServiceUnitDTO UNIT = CertificateServiceUnitDTO.builder().build();
    private static final CertificateServiceUnitDTO CARE_UNIT = CertificateServiceUnitDTO.builder()
        .build();
    private static final CertificateServiceUnitDTO CARE_PROVIDER = CertificateServiceUnitDTO.builder()
        .build();
    private static final Patient PATIENT_WITH_ID = Patient.builder()
        .personId(
            PersonId.builder()
                .id(PATIENT_ID)
                .build()
        )
        .build();
    private static final CertificateServicePatientDTO PATIENT = CertificateServicePatientDTO.builder()
        .build();
    private static final ListFilter LIST_FILTER = new ListFilter();
    private static final QueryIntygParameter QUERY_INTYG_PARAMETER = new QueryIntygParameter();
    private static final CertificatesQueryCriteriaDTO CERTIFICATES_QUERY_CRITERIA_DTO = CertificatesQueryCriteriaDTO.builder()
        .build();
    private static final String ADDITIONAL_INFO_TEXT = "ADDITIONAL_INFO_TEXT";
    private static final String REASON_INCORRECT_PATIENT_CONVERTED = "INCORRECT_PATIENT";
    private static final String REASON_INCORRECT_PATIENT_NOT_CONVERTED = "FEL_PATIENT";
    private static final String REASON_OTHER_SERIOUS_ERROR_NOT_CONVERTED = "ANNAT_ALLVARLIGT_FEL";
    private static final String REASON_OTHER_SERIOUS_ERROR_CONVERTED = "OTHER_SERIOUS_ERROR";
    private static final String REVOKED_MESSAGE = "REVOKED_MESSAGE";
    private static final MessageQueryCriteriaDTO MESSAGE_QUERY_CRITERIA_DTO = MessageQueryCriteriaDTO.builder()
        .build();

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
            () -> csIntegrationRequestFactory.createCertificateRequest(CERTIFICATE_MODEL_ID,
                "wrong-patient-id")
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
            final var actualRequest = csIntegrationRequestFactory.getCertificateTypesRequest(
                PERSONNUMMER);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateTypesRequest(
                PERSONNUMMER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateTypesRequest(
                PERSONNUMMER);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateTypesRequest(
                PERSONNUMMER);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateTypesRequest(
                PERSONNUMMER);
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
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(
                CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(
                CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(
                CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(
                CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(
                CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetCertificateModel() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateRequest(
                CERTIFICATE_MODEL_ID, PATIENT_ID);
            assertEquals(CERTIFICATE_MODEL_ID, actualRequest.getCertificateModelId());
        }
    }

    @Nested
    class CreateDraftCertificateRequest {

        private static final String HSA_ID = "hsaId";
        private static final String EXPECTED_REF = "expectedRef";
        private final IntygUser intygUser = new IntygUser(HSA_ID);
        private final Intyg intyg = new Intyg();

        @BeforeEach
        void setup() {
            intyg.setPatient(new se.riv.clinicalprocess.healthcond.certificate.v3.Patient());
            intyg.getPatient()
                .setPersonId(new se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId());
            intyg.getPatient().getPersonId().setExtension(PATIENT_ID);
            intyg.setRef(EXPECTED_REF);
            when(certificateServiceIntegrationUserHelper.get(intygUser))
                .thenReturn(USER);
            when(certificateServicePatientHelper.get(PERSONNUMMER))
                .thenReturn(PATIENT);
            when(certificateServiceIntegrationUnitHelper.getUnit(intygUser))
                .thenReturn(UNIT);
            when(certificateServiceIntegrationUnitHelper.getCareUnit(intygUser))
                .thenReturn(CARE_UNIT);
            when(certificateServiceIntegrationUnitHelper.getCareProvider(intygUser))
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.createDraftCertificateRequest(
                CERTIFICATE_MODEL_ID, intyg, intygUser);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.createDraftCertificateRequest(
                CERTIFICATE_MODEL_ID, intyg, intygUser);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.createDraftCertificateRequest(
                CERTIFICATE_MODEL_ID, intyg, intygUser);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.createDraftCertificateRequest(
                CERTIFICATE_MODEL_ID, intyg, intygUser);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.createDraftCertificateRequest(
                CERTIFICATE_MODEL_ID, intyg, intygUser);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetCertificateModel() {
            final var actualRequest = csIntegrationRequestFactory.createDraftCertificateRequest(
                CERTIFICATE_MODEL_ID, intyg, intygUser);
            assertEquals(CERTIFICATE_MODEL_ID, actualRequest.getCertificateModelId());
        }

        @Test
        void shouldSetExternalReference() {
            final var actualRequest = csIntegrationRequestFactory.createDraftCertificateRequest(
                CERTIFICATE_MODEL_ID, intyg, intygUser);
            assertEquals(EXPECTED_REF, actualRequest.getExternalReference());
        }

        @Test
        void shouldSetPrefillXml() {
            Forifyllnad prefill = new Forifyllnad();
            final var svar = new Svar();
            svar.setId("testSvarId");
            prefill.getSvar().add(svar);
            intyg.setForifyllnad(prefill);
            final var actualRequest = csIntegrationRequestFactory.createDraftCertificateRequest(
                CERTIFICATE_MODEL_ID, intyg, intygUser);
            assertEquals(PrefillXmlDTO.marshall(prefill), actualRequest.getPrefillXml());
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

        private static final String EXTERNAL_REF = "externalRef";

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
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE, PATIENT_ID);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE, PATIENT_ID);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE, PATIENT_ID);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE, PATIENT_ID);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetCertificate() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE, PATIENT_ID);
            assertEquals(CERTIFICATE, actualRequest.getCertificate());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE, PATIENT_ID);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetExternalReference() {
            final var actualRequest = csIntegrationRequestFactory.saveRequest(CERTIFICATE, PATIENT_ID,
                EXTERNAL_REF);
            assertEquals(EXTERNAL_REF, actualRequest.getExternalReference());
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
            final var actualRequest = csIntegrationRequestFactory.getPatientCertificatesRequest(
                PATIENT_ID);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getPatientCertificatesRequest(
                PATIENT_ID);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getPatientCertificatesRequest(
                PATIENT_ID);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getPatientCertificatesRequest(
                PATIENT_ID);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.getPatientCertificatesRequest(
                PATIENT_ID);
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
            when(certificatesQueryCriteriaFactory.create(LIST_FILTER)).thenReturn(
                CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(LIST_FILTER);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            when(certificatesQueryCriteriaFactory.create(LIST_FILTER)).thenReturn(
                CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(LIST_FILTER);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            when(certificatesQueryCriteriaFactory.create(LIST_FILTER)).thenReturn(
                CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(LIST_FILTER);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            when(certificatesQueryCriteriaFactory.create(LIST_FILTER)).thenReturn(
                CERTIFICATES_QUERY_CRITERIA_DTO);
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
            when(certificatesQueryCriteriaFactory.create(QUERY_INTYG_PARAMETER)).thenReturn(
                CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(
                QUERY_INTYG_PARAMETER);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            when(certificatesQueryCriteriaFactory.create(QUERY_INTYG_PARAMETER)).thenReturn(
                CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(
                QUERY_INTYG_PARAMETER);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            when(certificatesQueryCriteriaFactory.create(QUERY_INTYG_PARAMETER)).thenReturn(
                CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(
                QUERY_INTYG_PARAMETER);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            when(certificatesQueryCriteriaFactory.create(QUERY_INTYG_PARAMETER)).thenReturn(
                CERTIFICATES_QUERY_CRITERIA_DTO);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(
                QUERY_INTYG_PARAMETER);
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
            when(certificatesQueryCriteriaFactory.create(QUERY_INTYG_PARAMETER)).thenReturn(
                queryCriteriaDTO);
            when(certificateServicePatientHelper.get(PERSONNUMMER)).thenReturn(PATIENT);
            final var actualRequest = csIntegrationRequestFactory.getUnitCertificatesRequest(
                QUERY_INTYG_PARAMETER);
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
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(
                CERTIFICATE);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(
                CERTIFICATE);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(
                CERTIFICATE);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(
                CERTIFICATE);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(
                CERTIFICATE);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetCertificate() {
            final var actualRequest = csIntegrationRequestFactory.getValidateCertificateRequest(
                CERTIFICATE);
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
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(
                ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(
                ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(
                ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(
                ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetAdditionalInfoText() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(
                ADDITIONAL_INFO_TEXT, PATIENT_ID);
            assertEquals(ADDITIONAL_INFO_TEXT, actualRequest.getAdditionalInfoText());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.getPrintCertificateRequest(
                ADDITIONAL_INFO_TEXT, PATIENT_ID);
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
    class GetCertificateXmlRequestWithIntygUser {

        private final IntygUser intygUser = new IntygUser("employeeHsaId");

        @BeforeEach
        void setup() {
            when(certificateServiceIntegrationUserHelper.get(intygUser))
                .thenReturn(USER);
            when(certificateServiceIntegrationUnitHelper.getUnit(intygUser))
                .thenReturn(UNIT);
            when(certificateServiceIntegrationUnitHelper.getCareUnit(intygUser))
                .thenReturn(CARE_UNIT);
            when(certificateServiceIntegrationUnitHelper.getCareProvider(intygUser))
                .thenReturn(CARE_PROVIDER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateXmlRequest(intygUser);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateXmlRequest(intygUser);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateXmlRequest(intygUser);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateXmlRequest(intygUser);
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
            assertEquals(
                Base64.getEncoder().encodeToString(SIGNATURE_XML.getBytes(StandardCharsets.UTF_8)),
                actualRequest.getSignatureXml());
        }
    }

    @Nested
    class SignCertificateWithoutSignatureRequest {

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

    @Nested
    class SendCertificateRequest {

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
            final var actualRequest = csIntegrationRequestFactory.sendCertificateRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.sendCertificateRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.sendCertificateRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.sendCertificateRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class ValidRevokeCertificateRequest {

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
            final var actualRequest = csIntegrationRequestFactory.revokeCertificateRequest(
                REASON_INCORRECT_PATIENT_NOT_CONVERTED, REVOKED_MESSAGE);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.revokeCertificateRequest(
                REASON_INCORRECT_PATIENT_NOT_CONVERTED, REVOKED_MESSAGE);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.revokeCertificateRequest(
                REASON_INCORRECT_PATIENT_NOT_CONVERTED, REVOKED_MESSAGE);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.revokeCertificateRequest(
                REASON_INCORRECT_PATIENT_NOT_CONVERTED, REVOKED_MESSAGE);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetRevokedReason() {
            final var actualRequest = csIntegrationRequestFactory.revokeCertificateRequest(
                REASON_INCORRECT_PATIENT_NOT_CONVERTED, REVOKED_MESSAGE);
            assertEquals(REASON_INCORRECT_PATIENT_CONVERTED, actualRequest.getRevoked().getReason());
        }

        @Test
        void shouldConvertRevokedReasonIncorrectPatientToEnglish() {
            final var actualRequest = csIntegrationRequestFactory.revokeCertificateRequest(
                REASON_INCORRECT_PATIENT_NOT_CONVERTED, REVOKED_MESSAGE);
            assertEquals(REASON_INCORRECT_PATIENT_CONVERTED, actualRequest.getRevoked().getReason());
        }

        @Test
        void shouldConvertRevokedReasonOtherSeriousErrorToEnglish() {
            final var actualRequest = csIntegrationRequestFactory.revokeCertificateRequest(
                REASON_OTHER_SERIOUS_ERROR_NOT_CONVERTED, REVOKED_MESSAGE);
            assertEquals(REASON_OTHER_SERIOUS_ERROR_CONVERTED, actualRequest.getRevoked().getReason());
        }


        @Test
        void shouldSetRevokedMessage() {
            final var actualRequest = csIntegrationRequestFactory.revokeCertificateRequest(
                REASON_INCORRECT_PATIENT_NOT_CONVERTED, REVOKED_MESSAGE);
            assertEquals(REVOKED_MESSAGE, actualRequest.getRevoked().getMessage());
        }
    }

    @Nested
    class ReplaceCertificateRequest {

        @Mock
        private IntegrationParameters integrationParameters;

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServicePatientHelper.get(any()))
                .thenReturn(PATIENT);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
            when(integrationParameters.getReference())
                .thenReturn(EXTERNAL_REFERENCE);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.replaceCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.replaceCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.replaceCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.replaceCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatientUsingPatientIdIfAlternateSSNIsNotSet() {
            final var actualRequest = csIntegrationRequestFactory.replaceCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);

            verify(certificateServicePatientHelper).get(PERSONNUMMER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetExternalReference() {
            final var actualRequest = csIntegrationRequestFactory.replaceCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);
            assertEquals(EXTERNAL_REFERENCE, actualRequest.getExternalReference());
        }
    }

    @Nested
    class RenewCertificateRequest {

        @Mock
        private IntegrationParameters integrationParameters;

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServicePatientHelper.get(any()))
                .thenReturn(PATIENT);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
            when(integrationParameters.getReference())
                .thenReturn(EXTERNAL_REFERENCE);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.renewCertificateRequest(PATIENT_WITH_ID,
                integrationParameters);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.renewCertificateRequest(PATIENT_WITH_ID,
                integrationParameters);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.renewCertificateRequest(PATIENT_WITH_ID,
                integrationParameters);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.renewCertificateRequest(PATIENT_WITH_ID,
                integrationParameters);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatientUsingPatientIdIfAlternateSSNIsNotSet() {
            final var actualRequest = csIntegrationRequestFactory.renewCertificateRequest(PATIENT_WITH_ID,
                integrationParameters);

            verify(certificateServicePatientHelper).get(PERSONNUMMER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetExternalReference() {
            final var actualRequest = csIntegrationRequestFactory.renewCertificateRequest(PATIENT_WITH_ID,
                integrationParameters);
            assertEquals(EXTERNAL_REFERENCE, actualRequest.getExternalReference());
        }
    }

    @Nested
    class RenewLegacyCertificateRequest {

        private static final CertificateModelIdDTO CERTIFICATE_MODEL_ID = CertificateModelIdDTO.builder()
            .build();
        private static final Unit UNIT = Unit.builder().build();
        @Mock
        private IntegrationParameters integrationParameters;
        private final PrefillXmlDTO prefillXmlDTO = new PrefillXmlDTO("xml");

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServicePatientHelper.get(any()))
                .thenReturn(PATIENT);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(CSIntegrationRequestFactoryTest.UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
            when(integrationParameters.getReference())
                .thenReturn(EXTERNAL_REFERENCE);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.renewLegacyCertificateRequest(
                PATIENT_WITH_ID, integrationParameters, CERTIFICATE_MODEL_ID, CertificateStatus.SIGNED,
                UNIT, prefillXmlDTO);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.renewLegacyCertificateRequest(
                PATIENT_WITH_ID, integrationParameters, CERTIFICATE_MODEL_ID, CertificateStatus.SIGNED,
                UNIT, prefillXmlDTO);
            assertEquals(CSIntegrationRequestFactoryTest.UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.renewLegacyCertificateRequest(
                PATIENT_WITH_ID, integrationParameters, CERTIFICATE_MODEL_ID, CertificateStatus.SIGNED,
                UNIT, prefillXmlDTO);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.renewLegacyCertificateRequest(
                PATIENT_WITH_ID, integrationParameters, CERTIFICATE_MODEL_ID, CertificateStatus.SIGNED,
                UNIT, prefillXmlDTO);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatientUsingPatientIdIfAlternateSSNIsNotSet() {
            final var actualRequest = csIntegrationRequestFactory.renewLegacyCertificateRequest(
                PATIENT_WITH_ID, integrationParameters, CERTIFICATE_MODEL_ID, CertificateStatus.SIGNED,
                UNIT, prefillXmlDTO);

            verify(certificateServicePatientHelper).get(PERSONNUMMER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetExternalReference() {
            final var actualRequest = csIntegrationRequestFactory.renewLegacyCertificateRequest(
                PATIENT_WITH_ID, integrationParameters, CERTIFICATE_MODEL_ID, CertificateStatus.SIGNED,
                UNIT, prefillXmlDTO);
            assertEquals(EXTERNAL_REFERENCE, actualRequest.getExternalReference());
        }

        @Test
        void shouldSetCertificateModelId() {
            final var actualRequest = csIntegrationRequestFactory.renewLegacyCertificateRequest(
                PATIENT_WITH_ID, integrationParameters, CERTIFICATE_MODEL_ID, CertificateStatus.SIGNED,
                UNIT, prefillXmlDTO);
            assertEquals(CERTIFICATE_MODEL_ID, actualRequest.getCertificateModelId());
        }

        @Test
        void shouldSetStatus() {
            final var actualRequest = csIntegrationRequestFactory.renewLegacyCertificateRequest(
                PATIENT_WITH_ID, integrationParameters, CERTIFICATE_MODEL_ID, CertificateStatus.SIGNED,
                UNIT, prefillXmlDTO);
            assertEquals(CertificateStatus.SIGNED, actualRequest.getStatus());
        }

        @Test
        void shouldSetIssuingUnit() {
            final var expectedUnit = CertificateServiceUnitDTO.builder().build();

            when(certificateServiceVardenhetConverter.convert(UNIT)).thenReturn(expectedUnit);

            final var actualRequest = csIntegrationRequestFactory.renewLegacyCertificateRequest(
                PATIENT_WITH_ID, integrationParameters, CERTIFICATE_MODEL_ID, CertificateStatus.SIGNED,
                UNIT, prefillXmlDTO);

            assertEquals(expectedUnit, actualRequest.getIssuingUnit());
        }

        @Test
        void shouldSetPrefillXml() {
            final var actualRequest = csIntegrationRequestFactory.renewLegacyCertificateRequest(
                PATIENT_WITH_ID, integrationParameters, CERTIFICATE_MODEL_ID, CertificateStatus.SIGNED,
                UNIT, prefillXmlDTO);
            assertEquals(prefillXmlDTO, actualRequest.getPrefillXml());
        }
    }

    @Nested
    class ComplementCertificateRequest {

        @Mock
        private IntegrationParameters integrationParameters;

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServicePatientHelper.get(any()))
                .thenReturn(PATIENT);
            when(certificateServiceUnitHelper.getUnit())
                .thenReturn(UNIT);
            when(certificateServiceUnitHelper.getCareUnit())
                .thenReturn(CARE_UNIT);
            when(certificateServiceUnitHelper.getCareProvider())
                .thenReturn(CARE_PROVIDER);
            when(integrationParameters.getReference())
                .thenReturn(EXTERNAL_REFERENCE);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.complementCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.complementCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.complementCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.complementCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatientUsingPatientIdIfAlternateSSNIsNotSet() {
            final var actualRequest = csIntegrationRequestFactory.complementCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);

            verify(certificateServicePatientHelper).get(PERSONNUMMER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetExternalReference() {
            final var actualRequest = csIntegrationRequestFactory.complementCertificateRequest(
                PATIENT_WITH_ID, integrationParameters);
            assertEquals(EXTERNAL_REFERENCE, actualRequest.getExternalReference());
        }
    }

    @Nested
    class InvalidRevokeCertificateRequest {

        @Test
        void shouldThrowIllegalArgumentIfInvalidReason() {
            final var illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> csIntegrationRequestFactory.revokeCertificateRequest("", REVOKED_MESSAGE));

            assertEquals(
                "Invalid revoke reason. Reason must be either 'FEL_PATIENT' or 'ANNAT_ALLVARLIGT_FEL'",
                illegalArgumentException.getMessage());

        }
    }

    @Nested
    class GetCitizenCertificateRequest {

        @Test
        void shouldReturnGetCitizenCertificateRequestWithTypePersonalIdentityNumber() {
            final var expectedRequest = GetCitizenCertificateRequestDTO.builder()
                .personId(
                    PersonIdDTO.builder()
                        .type(PersonIdType.PERSONAL_IDENTITY_NUMBER)
                        .id(PERSONNUMMER.getOriginalPnr())
                        .build()
                )
                .build();

            final var citizenCertificateRequest = csIntegrationRequestFactory.getCitizenCertificateRequest(
                PATIENT_ID);
            assertEquals(expectedRequest, citizenCertificateRequest);
        }

        @Test
        void shouldReturnGetCitizenCertificateRequestWithTypeCoordinationNumber() {
            final var expectedRequest = GetCitizenCertificateRequestDTO.builder()
                .personId(
                    PersonIdDTO.builder()
                        .type(PersonIdType.COORDINATION_NUMBER)
                        .id(COORDINATION_PERSONNUMMER.getOriginalPnr())
                        .build()
                )
                .build();

            final var citizenCertificateRequest = csIntegrationRequestFactory.getCitizenCertificateRequest(
                COORDINATION_NUMBER_PATIENT_ID);
            assertEquals(expectedRequest, citizenCertificateRequest);
        }

        @Test
        void shouldThrowIfInvalidPersonId() {
            assertThrows(IllegalArgumentException.class,
                () -> csIntegrationRequestFactory.getCitizenCertificateRequest("invalidPersonId"));
        }
    }

    @Nested
    class GetCitizenCertificatePdfRequest {

        @Test
        void shouldReturnGetCitizenCertificatePdfRequestWithTypePersonalIdentityNumber() {
            final var expectedRequest = GetCitizenCertificatePdfRequestDTO.builder()
                .personId(
                    PersonIdDTO.builder()
                        .type(PersonIdType.PERSONAL_IDENTITY_NUMBER)
                        .id(PERSONNUMMER.getOriginalPnr())
                        .build()
                )
                .additionalInfo("Utskriven från 1177 intyg")
                .build();

            final var citizenCertificateRequest = csIntegrationRequestFactory.getCitizenCertificatePdfRequest(
                PATIENT_ID);
            assertEquals(expectedRequest, citizenCertificateRequest);
        }

        @Test
        void shouldReturnGetCitizenCertificateRequestWithTypeCoordinationNumber() {
            final var expectedRequest = GetCitizenCertificatePdfRequestDTO.builder()
                .personId(
                    PersonIdDTO.builder()
                        .type(PersonIdType.COORDINATION_NUMBER)
                        .id(COORDINATION_PERSONNUMMER.getOriginalPnr())
                        .build()
                )
                .additionalInfo("Utskriven från 1177 intyg")
                .build();

            final var citizenCertificateRequest = csIntegrationRequestFactory.getCitizenCertificatePdfRequest(
                COORDINATION_NUMBER_PATIENT_ID);
            assertEquals(expectedRequest, citizenCertificateRequest);
        }

        @Test
        void shouldReturnGetCitizenCertificatePdfRequestWithAdditonalInfo() {
            final var citizenCertificateRequest = csIntegrationRequestFactory.getCitizenCertificatePdfRequest(
                PATIENT_ID);
            assertEquals("Utskriven från 1177 intyg", citizenCertificateRequest.getAdditionalInfo());
        }

        @Test
        void shouldThrowIfInvalidPersonId() {
            assertThrows(IllegalArgumentException.class,
                () -> csIntegrationRequestFactory.getCitizenCertificateRequest("invalidPersonId"));
        }
    }

    @Nested
    class AnswerComplementCertificateRequest {

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
            final var actualRequest = csIntegrationRequestFactory.answerComplementOnCertificateRequest(
                MESSAGE);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.answerComplementOnCertificateRequest(
                MESSAGE);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.answerComplementOnCertificateRequest(
                MESSAGE);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.answerComplementOnCertificateRequest(
                MESSAGE);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetMessage() {
            final var actualRequest = csIntegrationRequestFactory.answerComplementOnCertificateRequest(
                MESSAGE);
            assertEquals(MESSAGE, actualRequest.getMessage());
        }
    }

    @Nested
    class GetIncomingMessageRequest {

        @Test
        void shouldReturnIncomingMessageRequest() {
            final var sendMessageToCareType = new SendMessageToCareType();
            final var expectedRequest = IncomingMessageRequestDTO.builder().build();
            doReturn(expectedRequest).when(messageRequestConverter).convert(sendMessageToCareType);

            final var incomingMessageRequest = csIntegrationRequestFactory.getIncomingMessageRequest(
                sendMessageToCareType);
            assertEquals(expectedRequest, incomingMessageRequest);
        }
    }

    @Nested
    class GetCertificateMessageRequestTests {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServicePatientHelper.get(any()))
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
            final var actualRequest = csIntegrationRequestFactory.getCertificateMessageRequest(
                PATIENT_ID);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateMessageRequest(
                PATIENT_ID);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateMessageRequest(
                PATIENT_ID);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateMessageRequest(
                PATIENT_ID);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateMessageRequest(
                PATIENT_ID);

            verify(certificateServicePatientHelper).get(PERSONNUMMER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }
    }

    @Nested
    class HandleMessageRequestTests {

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
            final var actualRequest = csIntegrationRequestFactory.handleMessageRequestDTO(false);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.handleMessageRequestDTO(false);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.handleMessageRequestDTO(false);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.handleMessageRequestDTO(false);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetIsHandled() {
            final var actualRequest = csIntegrationRequestFactory.handleMessageRequestDTO(false);
            assertFalse(actualRequest.getHandled());
        }
    }

    @Nested
    class GetCertificteFromMessageRequestTests {

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
            final var actualRequest = csIntegrationRequestFactory.getCertificateFromMessageRequestDTO();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateFromMessageRequestDTO();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateFromMessageRequestDTO();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateFromMessageRequestDTO();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class DeleteMessageRequestTests {

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
            final var actualRequest = csIntegrationRequestFactory.deleteMessageRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.deleteMessageRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.deleteMessageRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.deleteMessageRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class DeleteAnswerRequestTests {

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
            final var actualRequest = csIntegrationRequestFactory.deleteAnswerRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.deleteAnswerRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.deleteAnswerRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.deleteAnswerRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class CreateMessageRequestTests {

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
            when(certificateServicePatientHelper.get(any()))
                .thenReturn(PATIENT);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.createMessageRequest(
                QuestionType.CONTACT, MESSAGE, PATIENT_ID);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.createMessageRequest(
                QuestionType.CONTACT, MESSAGE, PATIENT_ID);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.createMessageRequest(
                QuestionType.CONTACT, MESSAGE, PATIENT_ID);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.createMessageRequest(
                QuestionType.CONTACT, MESSAGE, PATIENT_ID);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetQuestionType() {
            final var actualRequest = csIntegrationRequestFactory.createMessageRequest(
                QuestionType.CONTACT, MESSAGE, PATIENT_ID);
            assertEquals(QuestionType.CONTACT, actualRequest.getQuestionType());
        }

        @Test
        void shouldSetMessage() {
            final var actualRequest = csIntegrationRequestFactory.createMessageRequest(
                QuestionType.CONTACT, MESSAGE, PATIENT_ID);
            assertEquals(MESSAGE, actualRequest.getMessage());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.createMessageRequest(
                QuestionType.CONTACT, MESSAGE, PATIENT_ID);

            verify(certificateServicePatientHelper).get(PERSONNUMMER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }
    }

    @Nested
    class SaveMessageRequestTests {

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
            final var actualRequest = csIntegrationRequestFactory.saveMessageRequest(QUESTION);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.saveMessageRequest(QUESTION);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.saveMessageRequest(QUESTION);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.saveMessageRequest(QUESTION);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetQuestion() {
            final var actualRequest = csIntegrationRequestFactory.saveMessageRequest(QUESTION);
            assertEquals(QUESTION, actualRequest.getQuestion());
        }
    }

    @Nested
    class SendMessageRequestTests {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServicePatientHelper.get(any()))
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
            final var actualRequest = csIntegrationRequestFactory.sendMessageRequest(PATIENT_ID);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.sendMessageRequest(PATIENT_ID);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.sendMessageRequest(PATIENT_ID);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.sendMessageRequest(PATIENT_ID);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.sendMessageRequest(PATIENT_ID);

            verify(certificateServicePatientHelper).get(PERSONNUMMER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }
    }

    @Nested
    class SendAnswerRequestTests {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
            when(certificateServicePatientHelper.get(any()))
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
            final var actualRequest = csIntegrationRequestFactory.sendAnswerRequest(PATIENT_ID, MESSAGE);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.sendAnswerRequest(PATIENT_ID, MESSAGE);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.sendAnswerRequest(PATIENT_ID, MESSAGE);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.sendAnswerRequest(PATIENT_ID, MESSAGE);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetPatient() {
            final var actualRequest = csIntegrationRequestFactory.sendAnswerRequest(PATIENT_ID, MESSAGE);

            verify(certificateServicePatientHelper).get(PERSONNUMMER);
            assertEquals(PATIENT, actualRequest.getPatient());
        }

        @Test
        void shouldSetContent() {
            final var actualRequest = csIntegrationRequestFactory.sendAnswerRequest(PATIENT_ID, MESSAGE);
            assertEquals(MESSAGE, actualRequest.getContent());
        }
    }

    @Nested
    class GetUnitQuestionsRequestDTOTest {

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
            final var actualRequest = csIntegrationRequestFactory.getUnitQuestionsRequestDTO(
                MESSAGE_QUERY_CRITERIA_DTO);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getUnitQuestionsRequestDTO(
                MESSAGE_QUERY_CRITERIA_DTO);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getUnitQuestionsRequestDTO(
                MESSAGE_QUERY_CRITERIA_DTO);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getUnitQuestionsRequestDTO(
                MESSAGE_QUERY_CRITERIA_DTO);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetMessageQueryCriteria() {
            final var actualRequest = csIntegrationRequestFactory.getUnitQuestionsRequestDTO(
                MESSAGE_QUERY_CRITERIA_DTO);
            assertEquals(MESSAGE_QUERY_CRITERIA_DTO, actualRequest.getMessagesQueryCriteria());
        }
    }

    @Nested
    class SaveAnswerRequestTests {

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
            final var actualRequest = csIntegrationRequestFactory.saveAnswerRequest(MESSAGE);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.saveAnswerRequest(MESSAGE);
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.saveAnswerRequest(MESSAGE);
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.saveAnswerRequest(MESSAGE);
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }

        @Test
        void shouldSetContent() {
            final var actualRequest = csIntegrationRequestFactory.saveAnswerRequest(MESSAGE);
            assertEquals(MESSAGE, actualRequest.getContent());
        }
    }

    @Nested
    class ForwardCertificateRequest {

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
            final var actualRequest = csIntegrationRequestFactory.forwardCertificateRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.forwardCertificateRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.forwardCertificateRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.forwardCertificateRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class GetCertificateEventsRequest {

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
            final var actualRequest = csIntegrationRequestFactory.getCertificateEventsRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateEventsRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateEventsRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.getCertificateEventsRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class CertificatesWithQARequestDTOTest {

        private static final String CERTIFICATE_ID = "certificateId";
        private List<String> certificateIds;

        @BeforeEach
        void setUp() {
            certificateIds = new ArrayList<>();
            certificateIds.addAll(List.of(CERTIFICATE_ID, CERTIFICATE_ID));
        }

        @Test
        void shouldSetCertificateIds() {
            final var actualRequest = csIntegrationRequestFactory.getCertificatesWithQARequestDTO(
                certificateIds);
            assertEquals(List.of(CERTIFICATE_ID, CERTIFICATE_ID), actualRequest.getCertificateIds());
        }
    }

    @Nested
    class LockDraftsRequestTests {

        @Test
        void shouldSetCuttoffDate() {
            final var expectedCuttoffDate = LocalDate.now().minusDays(5).atStartOfDay();
            final var actualRequest = csIntegrationRequestFactory.getLockDraftsRequestDTO(5);
            assertEquals(expectedCuttoffDate, actualRequest.getCutoffDate());
        }
    }

    @Nested
    class StatisticsRequestTests {

        @BeforeEach
        void setup() {
            when(certificateServiceUserHelper.get())
                .thenReturn(USER);
        }

        @Test
        void shouldSetUser() {
            final var actualRequest = csIntegrationRequestFactory.getStatisticsRequest(UNIT_IDS);
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnitIds() {
            final var actualRequest = csIntegrationRequestFactory.getStatisticsRequest(UNIT_IDS);
            assertEquals(UNIT_IDS, actualRequest.getIssuedByUnitIds());
        }
    }

    @Nested
    class ReadyForSignRequestDTOTest {

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
            final var actualRequest = csIntegrationRequestFactory.readyForSignRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.readyForSignRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.readyForSignRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.readyForSignRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }

    @Nested
    class CreateCertificateFromTemplateRequest {

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
            final var actualRequest = csIntegrationRequestFactory.createCertificateFromTemplateRequest();
            assertEquals(USER, actualRequest.getUser());
        }

        @Test
        void shouldSetUnit() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateFromTemplateRequest();
            assertEquals(UNIT, actualRequest.getUnit());
        }

        @Test
        void shouldSetCareUnit() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateFromTemplateRequest();
            assertEquals(CARE_UNIT, actualRequest.getCareUnit());
        }

        @Test
        void shouldSetCareProvider() {
            final var actualRequest = csIntegrationRequestFactory.createCertificateFromTemplateRequest();
            assertEquals(CARE_PROVIDER, actualRequest.getCareProvider());
        }
    }
}

