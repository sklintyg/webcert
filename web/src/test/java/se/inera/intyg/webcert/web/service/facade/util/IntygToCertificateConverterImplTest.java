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
package se.inera.intyg.webcert.web.service.facade.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.model.Status;
import se.inera.intyg.common.support.model.common.internal.GrundData;
import se.inera.intyg.common.support.model.common.internal.HoSPersonal;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.facade.TypeAheadProvider;
import se.inera.intyg.infra.integration.hsatk.model.HealthCareUnit;
import se.inera.intyg.infra.integration.hsatk.services.HsatkOrganizationService;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;

@ExtendWith(MockitoExtension.class)
public class IntygToCertificateConverterImplTest {

    public static final String CERTIFICATE_ID = "certificateId";
    public static final String CERTIFICATE_TYPE = "certificateType";
    public static final String CERTIFICATE_TYPE_VERSION = "certificateTypeVersion";
    public static final String CONTENT_JSON = "draftJson";
    public static final Personnummer PATIENT_PERSONNUMMER = Personnummer.createPersonnummer("191212121212").orElseThrow();
    public static final LocalDateTime RECEIVED_DATE_TIME = LocalDateTime.now();
    public static final boolean IS_TEST_INTYG = true;
    public static final String CARE_PROVIDER_ID = "CareProviderId";
    public static final String CARE_PROVIDER_NAME = "CareProviderName";
    public static final String CARE_UNIT_ID = "CareUnitId";
    public static final String CARE_UNIT_NAME = "CareUnitName";
    public static final String UNIT_ID = "unitId";
    public static final String PERSON_ID = "PersonId";
    public static final String PERSON_NAME = "Doctor Alpha";
    public static final String PERSON_ID_FROM_JSON = "PersonId - json";
    public static final String PERSON_NAME_FROM_JSON = "Doctor Alpha - json";

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private IntygTextsService intygTextsService;

    @Mock
    private PatientConverter patientConverter;

    @Mock
    private CertificateRelationsConverter certificateRelationsConverter;

    @Mock
    private HsatkOrganizationService hsatkOrganizationService;

    @Mock
    private TypeAheadProvider typeAheadProvider;

    @InjectMocks
    private IntygToCertificateConverterImpl intygToCertificateConverter;

    private final List<Status> statusList = new ArrayList<>();
    private final IntygContentHolder intygContentHolder = createIntygContentHolder();
    private final CertificateRelations certificateRelations = CertificateRelations.builder().build();
    private final Patient patient = getPatient();

    @BeforeEach
    void setupMocks() throws Exception {
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi)
            .when(moduleRegistry)
            .getModuleApi(CERTIFICATE_TYPE, CERTIFICATE_TYPE_VERSION);

        doReturn(createCertificate())
            .when(moduleApi).getCertificateFromJson(CONTENT_JSON, typeAheadProvider);

        doReturn(certificateRelations)
            .when(certificateRelationsConverter).convert(CERTIFICATE_ID);

        doReturn(patient)
            .when(patientConverter).convert(
                patient,
                PATIENT_PERSONNUMMER,
                CERTIFICATE_TYPE,
                CERTIFICATE_TYPE_VERSION
            );

        doReturn(getHealthCareUnit())
            .when(hsatkOrganizationService).getHealthCareUnit(any(String.class));

        doReturn(getUnit())
            .when(hsatkOrganizationService).getUnit(any(String.class), nullable(String.class));
    }

    @Nested
    class ValidateCommonMetadata {

        @Test
        void shallIncludeCreatedDateTime() {
            final var expectedCreated = RECEIVED_DATE_TIME;

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedCreated, actualCertificate.getMetadata().getCreated());
        }

        @Test
        void shallIncludeVersion99() {
            final var expectedVersion = 99;

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedVersion, actualCertificate.getMetadata().getVersion());
        }

        @Test
        void shallIncludeForwardedFalse() {
            final var expectedForwarded = false;

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedForwarded, actualCertificate.getMetadata().isForwarded());
        }

        @Test
        void shallIncludeIsSentTrue() {
            final var expectedSent = true;
            statusList.add(new Status(CertificateState.SENT, "FK", LocalDateTime.now()));

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedSent, actualCertificate.getMetadata().isSent());
        }

        @Test
        void shallIncludeIsSentFalse() {
            final var expectedSent = false;

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedSent, actualCertificate.getMetadata().isSent());
        }

        @Test
        void shallIncludeTestCertificate() {
            final var expectedTestCertificate = IS_TEST_INTYG;

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedTestCertificate, actualCertificate.getMetadata().isTestCertificate());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shallIncludeLatestMajorVersion(boolean expectedLatestMajorVersion) {
            doReturn(expectedLatestMajorVersion).when(intygTextsService).isLatestMajorVersion(any(), any());
            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedLatestMajorVersion, actualCertificate.getMetadata().isLatestMajorVersion());
        }
    }

    @Nested
    class ValidateUnit {

        @Test
        void shallContainCompleteUnitData() {
            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

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

        @Test
        void shallIncludeCareProviderId() {
            final var expectedCareProviderId = CARE_PROVIDER_ID;

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedCareProviderId, actualCertificate.getMetadata().getCareProvider().getUnitId());
        }

        @Test
        void shallIncludeCareProviderName() {
            final var expectedCareProviderName = CARE_PROVIDER_NAME;

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedCareProviderName, actualCertificate.getMetadata().getCareProvider().getUnitName());
        }
    }

    @Nested
    class ValidateCareUnit {

        @Test
        void shallIncludeCareUnitId() {
            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);
            assertEquals(CARE_UNIT_ID, actualCertificate.getMetadata().getCareUnit().getUnitId());
        }

        @Test
        void shallIncludeCareUnitName() {
            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);
            assertEquals(CARE_UNIT_NAME, actualCertificate.getMetadata().getCareUnit().getUnitName());
        }
    }

    @Nested
    class ValidateIssuedBy {

        @Test
        void shallIncludePersonId() {
            final var expectedPersonId = PERSON_ID_FROM_JSON;

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedPersonId, actualCertificate.getMetadata().getIssuedBy().getPersonId());
        }

        @Test
        void shallIncludeName() {
            final var expectedFullName = PERSON_NAME_FROM_JSON;

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedFullName, actualCertificate.getMetadata().getIssuedBy().getFullName());
        }
    }

    @Test
    void shallIncludePatient() {
        final var expectedPatient = getPatient();
        final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);
        assertEquals(expectedPatient, actualCertificate.getMetadata().getPatient());
    }

    @Test
    void shallIncludeCertificateRelations() {
        final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);
        assertEquals(certificateRelations, actualCertificate.getMetadata().getRelations());
    }

    @Nested
    class ValidateStatus {

        @Test
        void shallIncludeStatusSigned() {
            final var expectedStatus = CertificateStatus.SIGNED;

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }

        @Test
        void shallIncludeStatusRevoked() {
            final var expectedStatus = CertificateStatus.REVOKED;
            statusList.add(new Status(CertificateState.CANCELLED, "HSVARD", LocalDateTime.now()));

            final var actualCertificate = intygToCertificateConverter.convert(intygContentHolder);

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }
    }

    private IntygContentHolder createIntygContentHolder() {
        final var grundData = new GrundData();
        grundData.setPatient(new se.inera.intyg.common.support.model.common.internal.Patient());
        grundData.getPatient().setPersonId(PATIENT_PERSONNUMMER);
        grundData.setSkapadAv(new HoSPersonal());
        grundData.getSkapadAv().setPersonId(PERSON_ID);
        grundData.getSkapadAv().setFullstandigtNamn(PERSON_NAME);
        grundData.getSkapadAv().setVardenhet(new Vardenhet());
        grundData.getSkapadAv().getVardenhet().setEnhetsid(UNIT_ID);
        grundData.getSkapadAv().getVardenhet().setVardgivare(new Vardgivare());
        grundData.getSkapadAv().getVardenhet().getVardgivare().setVardgivarid(CARE_PROVIDER_ID);
        grundData.getSkapadAv().getVardenhet().getVardgivare().setVardgivarnamn(CARE_PROVIDER_NAME);

        final var mockUtlatande = mock(Utlatande.class);
        doReturn(CERTIFICATE_ID)
            .when(mockUtlatande).getId();
        doReturn(CERTIFICATE_TYPE)
            .when(mockUtlatande).getTyp();
        doReturn(CERTIFICATE_TYPE_VERSION)
            .when(mockUtlatande).getTextVersion();
        doReturn(grundData)
            .when(mockUtlatande).getGrundData();

        statusList.add(new Status(CertificateState.RECEIVED, "HSVARD", RECEIVED_DATE_TIME));

        return IntygContentHolder.builder()
            .setTestIntyg(IS_TEST_INTYG)
            .setPatientAddressChangedInPU(false)
            .setSekretessmarkering(false)
            .setDeceased(false)
            .setRevoked(false)
            .setContents(CONTENT_JSON)
            .setPatientNameChangedInPU(false)
            .setUtlatande(mockUtlatande)
            .setStatuses(statusList)
            .build();
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
        final var expectedPersonId = PersonId.builder()
            .id(PERSON_ID)
            .type("PERSON_NUMMER")
            .build();
        return expectedPersonId;
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
