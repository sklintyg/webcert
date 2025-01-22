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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.doi.support.DoiModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRecipient;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.facade.TypeAheadProvider;
import se.inera.intyg.infra.integration.hsatk.model.HealthCareUnit;
import se.inera.intyg.infra.integration.hsatk.services.HsatkOrganizationService;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class UtkastToCertificateConverterTest {

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private IntygTextsService intygTextsService;

    @Mock
    private PatientConverter patientConverter;

    @Mock
    private CertificateRelationsConverter certificateRelationsConverter;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private HsatkOrganizationService hsatkOrganizationService;

    @Mock
    private TypeAheadProvider typeAheadProvider;

    @Mock
    private CertificateRecipientConverter certificateRecipientConverter;

    @Mock
    DraftAccessServiceHelper draftAccessServiceHelper;

    @Mock
    FeaturesHelper featuresHelper;

    @InjectMocks
    private UtkastToCertificateConverterImpl utkastToCertificateConverter;

    private final Utkast draft = createDraft();
    private final CertificateRelations certificateRelations = CertificateRelations.builder().build();
    private final Patient patient = getPatient();

    private static final String CARE_UNIT_ID = "careUnitId";
    private static final String CARE_UNIT_NAME = "careUnitName";
    private static final String PERSON_ID_FROM_JSON = "PersonId - json";
    private static final String PERSON_NAME_FROM_JSON = "Doctor Alpha - json";

    @Nested
    class TestConvert {

        @BeforeEach
        void setupMocks() throws Exception {
            final var moduleApi = mock(ModuleApi.class);

            doReturn(moduleApi)
                .when(moduleRegistry).getModuleApi(anyString(), anyString());

            doReturn(createCertificate())
                .when(moduleApi).getCertificateFromJson(draft.getModel(), typeAheadProvider);

            doReturn(certificateRelations)
                .when(certificateRelationsConverter).convert(draft.getIntygsId());

            doReturn(patient)
                .when(patientConverter).convert(
                    any(), any(), any(), any()
                );
        }

        @Test
        void shouldSetUnitInfoFromDraftIfHsaExceptionForGetUnit() {
            when(hsatkOrganizationService.getUnit(anyString(), anyString()))
                .thenThrow(new IllegalStateException());
            when(hsatkOrganizationService.getHealthCareUnit(anyString()))
                .thenReturn(getHealthCareUnit());

            final var response = utkastToCertificateConverter.convert(draft);

            assertEquals(response.getMetadata().getUnit().getUnitId(), draft.getEnhetsId());
            assertEquals(response.getMetadata().getUnit().getUnitName(), draft.getEnhetsNamn());
        }

        @Test
        void shouldSetUnitInfoFromDraftIfHsaExceptionForGetHealthCareUnit() {
            when(hsatkOrganizationService.getHealthCareUnit(anyString()))
                .thenThrow(new IllegalStateException());

            final var response = utkastToCertificateConverter.convert(draft);

            assertEquals(response.getMetadata().getUnit().getUnitId(), draft.getEnhetsId());
            assertEquals(response.getMetadata().getUnit().getUnitName(), draft.getEnhetsNamn());
        }

        @Nested
        class ValidateCommonMetadata {

            @BeforeEach
            void setup() {
                doReturn(getHealthCareUnit())
                    .when(hsatkOrganizationService).getHealthCareUnit(any(String.class));

                doReturn(getUnit())
                    .when(hsatkOrganizationService).getUnit(any(String.class), nullable(String.class));

                doReturn(true).when(webCertUserService).hasAuthenticationContext();

                final var user = mock(WebCertUser.class);
                when(webCertUserService.getUser())
                    .thenReturn(user);
                when(user.getOrigin())
                    .thenReturn("DJUPINTEGRATION");
            }

            @Test
            void shallIncludeCreatedDateTimeAsTheLatestsSavedDateTime() {
                final var expectedCreated = draft.getSenastSparadDatum();

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedCreated, actualCertificate.getMetadata().getCreated());
            }

            @Test
            void shallIncludeConfirmationModalIfProviderIsAvailable() {
                doReturn(true).when(draftAccessServiceHelper)
                    .isAllowToEditUtkast(any(Utkast.class));
                draft.setIntygsTyp("db");
                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertNotNull(actualCertificate.getMetadata().getConfirmationModal());
            }

            @Test
            void shallIncludeSignModalIfProviderIsAvailable() {
                draft.setIntygsTyp("db");
                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertNotNull(actualCertificate.getMetadata().getSignConfirmationModal());
            }

            @Test
            void shallNotIncludeConfirmationModalIfProviderIsNotAvailable() {
                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertNull(actualCertificate.getMetadata().getConfirmationModal());
            }

            @ParameterizedTest
            @ValueSource(ints = {0, 1, 100})
            void shallIncludeVersion(int expectedVersion) {
                draft.setVersion(expectedVersion);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedVersion, actualCertificate.getMetadata().getVersion());
            }

            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            void shallIncludeForwarded(boolean expectedForwarded) {
                draft.setVidarebefordrad(expectedForwarded);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedForwarded, actualCertificate.getMetadata().isForwarded());
            }

            @Test
            void shallIncludeReadyForSign() {
                final var expectedReadyForSign = LocalDateTime.now();
                draft.setKlartForSigneringDatum(expectedReadyForSign);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedReadyForSign, actualCertificate.getMetadata().getReadyForSign());
            }

            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            void shallIncludeTestCertificate(boolean expectedTestCertificate) {
                draft.setTestIntyg(expectedTestCertificate);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedTestCertificate, actualCertificate.getMetadata().isTestCertificate());
            }

            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            void shallIncludeLatestMajorVersion(boolean expectedLatestMajorVersion) {
                doReturn(expectedLatestMajorVersion).when(intygTextsService).isLatestMajorVersion(any(), any());
                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedLatestMajorVersion, actualCertificate.getMetadata().isLatestMajorVersion());
            }

            @ParameterizedTest
            @ValueSource(booleans = {true, false})
            void shallIncludeInactiveCertificateType(boolean expectedInactiveCertificateType) {
                doReturn(expectedInactiveCertificateType)
                    .when(featuresHelper)
                    .isFeatureActive(AuthoritiesConstants.FEATURE_INACTIVE_CERTIFICATE_TYPE, draft.getIntygsTyp());
                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedInactiveCertificateType, actualCertificate.getMetadata().isInactiveCertificateType());
            }
        }

        @Nested
        class ValidateUnit {

            @BeforeEach
            void setup() {
                doReturn(getHealthCareUnit())
                    .when(hsatkOrganizationService).getHealthCareUnit(any(String.class));

                doReturn(getUnit())
                    .when(hsatkOrganizationService).getUnit(any(String.class), nullable(String.class));
            }

            @Test
            void shallContainCompleteUnitData() {
                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertAll(
                    () -> assertNotNull(actualCertificate.getMetadata().getUnit().getUnitId(), "UnitId should not be null"),
                    () -> assertNotNull(actualCertificate.getMetadata().getUnit().getUnitName(), "UnitName should not be null"),
                    () -> assertNotNull(actualCertificate.getMetadata().getUnit().getAddress(), "Address should not be null"),
                    () -> assertNotNull(actualCertificate.getMetadata().getUnit().getZipCode(), "ZipCode should not be null"),
                    () -> assertNotNull(actualCertificate.getMetadata().getUnit().getCity(), "City should not be null"),
                    () -> assertNotNull(actualCertificate.getMetadata().getUnit().getEmail(), "Email should not be null"),
                    () -> assertNotNull(actualCertificate.getMetadata().getUnit().getPhoneNumber(), "Phonenumber should not be null")
                );
            }
        }

        @Nested
        class ValidateCareProvider {

            @BeforeEach
            void setup() {
                doReturn(getHealthCareUnit())
                    .when(hsatkOrganizationService).getHealthCareUnit(any(String.class));

                doReturn(getUnit())
                    .when(hsatkOrganizationService).getUnit(any(String.class), nullable(String.class));
            }

            @Test
            void shallIncludeCareProviderId() {
                final var expectedCareProviderId = "CareProviderId";
                draft.setVardgivarId(expectedCareProviderId);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedCareProviderId, actualCertificate.getMetadata().getCareProvider().getUnitId());
            }

            @Test
            void shallIncludeCareProviderName() {
                final var expectedCareProviderName = "CareProviderName";
                draft.setVardgivarNamn(expectedCareProviderName);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedCareProviderName, actualCertificate.getMetadata().getCareProvider().getUnitName());
            }
        }

        @Nested
        class ValidateCareUnit {

            @BeforeEach
            void setup() {
                doReturn(getHealthCareUnit())
                    .when(hsatkOrganizationService).getHealthCareUnit(any(String.class));

                doReturn(getUnit())
                    .when(hsatkOrganizationService).getUnit(any(String.class), nullable(String.class));
            }

            @Test
            void shallIncludeCareUnitId() {
                final var actualCertificate = utkastToCertificateConverter.convert(draft);
                assertEquals(CARE_UNIT_ID, actualCertificate.getMetadata().getCareUnit().getUnitId());
            }

            @Test
            void shallIncludeCareUnitName() {
                final var actualCertificate = utkastToCertificateConverter.convert(draft);
                assertEquals(CARE_UNIT_NAME, actualCertificate.getMetadata().getCareUnit().getUnitName());
            }
        }

        @Nested
        class ValidateIssuedBy {

            @BeforeEach
            void setup() {
                doReturn(getHealthCareUnit())
                    .when(hsatkOrganizationService).getHealthCareUnit(any(String.class));

                doReturn(getUnit())
                    .when(hsatkOrganizationService).getUnit(any(String.class), nullable(String.class));
            }

            @Test
            void shallIncludePersonId() {
                final var expectedPersonId = PERSON_ID_FROM_JSON;
                draft.getSkapadAv().setHsaId("PersonId from utkast");

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedPersonId, actualCertificate.getMetadata().getIssuedBy().getPersonId());
            }

            @Test
            void shallIncludeName() {
                final var expectedFullName = PERSON_NAME_FROM_JSON;
                draft.getSkapadAv().setNamn("Person name from utkast");

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedFullName, actualCertificate.getMetadata().getIssuedBy().getFullName());
            }
        }

        @Test
        void shallIncludePatient() {
            final var expectedPatient = getPatient();
            final var actualCertificate = utkastToCertificateConverter.convert(draft);
            assertEquals(expectedPatient, actualCertificate.getMetadata().getPatient());
        }

        @Test
        void shallIncludeCertificateRelations() {
            final var actualCertificate = utkastToCertificateConverter.convert(draft);
            assertEquals(certificateRelations, actualCertificate.getMetadata().getRelations());
        }

        @Nested
        class ValidateStatus {

            @BeforeEach
            void setup() {
                doReturn(getHealthCareUnit())
                    .when(hsatkOrganizationService).getHealthCareUnit(any(String.class));

                doReturn(getUnit())
                    .when(hsatkOrganizationService).getUnit(any(String.class), nullable(String.class));
            }

            @Test
            void shallIncludeStatusUnsigned() {
                final var expectedStatus = CertificateStatus.UNSIGNED;

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
            }

            @Test
            void shallIncludeStatusSigned() {
                final var expectedStatus = CertificateStatus.SIGNED;
                draft.setStatus(UtkastStatus.SIGNED);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
            }

            @Test
            void shallIncludeStatusRevoked() {
                final var expectedStatus = CertificateStatus.REVOKED;
                draft.setStatus(UtkastStatus.SIGNED);
                draft.setAterkalladDatum(LocalDateTime.now());

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
            }

            @Test
            void shallIncludeStatusLocked() {
                final var expectedStatus = CertificateStatus.LOCKED;
                draft.setStatus(UtkastStatus.DRAFT_LOCKED);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
            }

            @Test
            void shallIncludeStatusLockedRevoked() {
                final var expectedStatus = CertificateStatus.LOCKED_REVOKED;
                draft.setStatus(UtkastStatus.DRAFT_LOCKED);
                draft.setAterkalladDatum(LocalDateTime.now());

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
            }
        }

        @Nested
        class ValidateSent {

            @BeforeEach
            void setup() {
                doReturn(getHealthCareUnit())
                    .when(hsatkOrganizationService).getHealthCareUnit(any(String.class));

                doReturn(getUnit())
                    .when(hsatkOrganizationService).getUnit(any(String.class), nullable(String.class));
            }

            @BeforeEach
            void setUp() {
                draft.setStatus(UtkastStatus.SIGNED);
            }

            @Test
            void shallIncludeIsSentTrueIfCertificateIsSent() {
                final var expectedIsSent = true;
                draft.setSkickadTillMottagareDatum(LocalDateTime.now());
                draft.setSkickadTillMottagare("FKASSA");

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedIsSent, actualCertificate.getMetadata().isSent());
            }

            @Test
            void shallIncludeIsSentFalseIfCertificateIsSent() {
                final var expectedIsSent = false;

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedIsSent, actualCertificate.getMetadata().isSent());
            }

            @Test
            void shallIncludeReceiverIfCertificateIsSent() {
                final var recipient = CertificateRecipient
                    .builder()
                    .name("Name")
                    .id("Id")
                    .build();
                when(certificateRecipientConverter.get(anyString(), anyString(), any()))
                    .thenReturn(recipient);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals("Name", actualCertificate.getMetadata().getSentTo());
            }
        }

        @Nested
        class ValidateResponsibleHospName {

            @BeforeEach
            void setup() {
                doReturn(getHealthCareUnit())
                    .when(hsatkOrganizationService).getHealthCareUnit(any(String.class));

                doReturn(getUnit())
                    .when(hsatkOrganizationService).getUnit(any(String.class), nullable(String.class));
            }

            @Test
            void shallNotSetResponsibleHospNameWhenNoAuthenticationContext() {
                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertNull(actualCertificate.getMetadata().getResponsibleHospName());
            }

            @Test
            void shallNotSetResponsibleHospNameWhenIntegrationParametersIsNull() {
                doReturn(true).when(webCertUserService).hasAuthenticationContext();
                final var user = mock(WebCertUser.class);
                when(webCertUserService.getUser()).thenReturn(user);
                when(user.getOrigin()).thenReturn("DJUPINTEGRATION");
                when(user.getParameters()).thenReturn(null);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertNull(actualCertificate.getMetadata().getResponsibleHospName());
            }

            @Test
            void shallSetResponsibleHospNameWhenIntegrationParametersArePresent() {
                final var expectedResponsibleHospName = "responsibleHospName";
                doReturn(true).when(webCertUserService).hasAuthenticationContext();
                final var user = mock(WebCertUser.class);
                final var integrationParameters = mock(IntegrationParameters.class);
                when(webCertUserService.getUser()).thenReturn(user);
                when(user.getOrigin()).thenReturn("DJUPINTEGRATION");
                when(user.getParameters()).thenReturn(integrationParameters);
                when(integrationParameters.getResponsibleHospName()).thenReturn(expectedResponsibleHospName);

                final var actualCertificate = utkastToCertificateConverter.convert(draft);

                assertEquals(expectedResponsibleHospName, actualCertificate.getMetadata().getResponsibleHospName());
            }
        }

        @Test
        void shouldSetRecipient() {
            final var recipient = CertificateRecipient.builder().build();
            when(certificateRecipientConverter.get(anyString(), anyString(), any()))
                .thenReturn(recipient);
            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(recipient, actualCertificate.getMetadata().getRecipient());
        }
    }


    @Nested
    class ValidateAvailableToCitizen {

        @BeforeEach
        void setup() throws ModuleNotFoundException, IOException, ModuleException {
            final var moduleApi = mock(ModuleApi.class);
            doReturn(moduleApi).when(moduleRegistry).getModuleApi(anyString(), eq(draft.getIntygTypeVersion()));
            doReturn(createCertificate()).when(moduleApi).getCertificateFromJson(draft.getModel(), typeAheadProvider);
        }

        @Test
        void shouldSetAvailableForCitizenToFalseForDb() {
            draft.setIntygsTyp(DbModuleEntryPoint.MODULE_ID);
            final var actualCertificate = utkastToCertificateConverter.convert(draft);
            assertFalse(actualCertificate.getMetadata().isAvailableForCitizen());
        }

        @Test
        void shouldSetAvailableForCitizenToFalseForDoi() {
            draft.setIntygsTyp(DoiModuleEntryPoint.MODULE_ID);
            final var actualCertificate = utkastToCertificateConverter.convert(draft);
            assertFalse(actualCertificate.getMetadata().isAvailableForCitizen());
        }

        @Test
        void shouldSetAvailableForCitizenToTrueForCertificatesOtherThanDbAndDoi() {
            draft.setIntygsTyp(LisjpEntryPoint.MODULE_ID);
            final var actualCertificate = utkastToCertificateConverter.convert(draft);
            assertTrue(actualCertificate.getMetadata().isAvailableForCitizen());
        }
    }

    private Utkast createDraft() {
        final var draft = new Utkast();
        draft.setIntygsId("certificateId");
        draft.setIntygsTyp("certificateType");
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setEnhetsId("unitId");
        draft.setEnhetsNamn("unitName");
        draft.setModel("draftJson");
        draft.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        draft.setSkapad(LocalDateTime.now().minusDays(1));
        draft.setSenastSparadDatum(LocalDateTime.now());
        draft.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").orElseThrow());
        draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
        return draft;
    }

    private Certificate createCertificate() {
        return CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id("certificateId")
                    .type("certificateType")
                    .typeVersion("certificateTypeVersion")
                    .unit(
                        Unit.builder()
                            .unitId("unitId")
                            .unitName("unitName")
                            .address("address")
                            .zipCode("zipCode")
                            .city("city")
                            .email("email")
                            .phoneNumber("phoneNumber")
                            .build()
                    )
                    .issuedBy(
                        Staff.builder()
                            .personId(PERSON_ID_FROM_JSON)
                            .fullName(PERSON_NAME_FROM_JSON)
                            .build()
                    )
                    .patient(
                        patient
                    )
                    .build()
            )
            .build();
    }

    private Patient getPatient() {
        return Patient.builder()
            .personId(getPersonId())
            .firstName("Fornamnet")
            .lastName("Efternamnet")
            .middleName("Mellannamnet")
            .build();
    }

    private PersonId getPersonId() {
        return PersonId.builder()
            .id(draft.getPatientPersonnummer().getPersonnummer())
            .type("PERSON_NUMMER")
            .build();
    }

    private HealthCareUnit getHealthCareUnit() {
        final var healthCareUnit = new HealthCareUnit();
        healthCareUnit.setHealthCareUnitHsaId(CARE_UNIT_ID);
        return healthCareUnit;
    }

    private se.inera.intyg.infra.integration.hsatk.model.Unit getUnit() {
        final var unit = new se.inera.intyg.infra.integration.hsatk.model.Unit();
        unit.setUnitHsaId(CARE_UNIT_ID);
        unit.setUnitName(CARE_UNIT_NAME);
        return unit;
    }
}
