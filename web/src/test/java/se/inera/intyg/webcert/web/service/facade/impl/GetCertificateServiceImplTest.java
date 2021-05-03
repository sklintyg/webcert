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

package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.builder.CertificateMetadataBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.Unit;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.facade.dto.ResourceLinkDTO;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;
import se.inera.intyg.webcert.web.web.util.resourcelinks.ResourceLinkHelper;

@ExtendWith(MockitoExtension.class)
class GetCertificateServiceImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private CertificateRelationService certificateRelationService;

    @Mock
    private ResourceLinkHelper resourceLinkHelper;

    @InjectMocks
    private GetCertificateServiceImpl getCertificateService;

    private final Utkast draft = createDraft();
    private final Relations relations = new Relations();

    @BeforeEach
    void setupMocks() throws Exception {
        doReturn(draft)
            .when(utkastService).getDraft(draft.getIntygsId());

        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi)
            .when(moduleRegistry).getModuleApi(draft.getIntygsTyp(), draft.getIntygTypeVersion());

        doReturn(createCertificate())
            .when(moduleApi).getCertificateFromJson(draft.getModel());

        doReturn(relations)
            .when(certificateRelationService).getRelations(draft.getIntygsId());
    }

    @Nested
    class ValidateResourceLinks {

        @Test
        void shallIncludeResourceLinks() {
            final var expectedResourceLinks = Arrays.array(new ResourceLinkDTO(), new ResourceLinkDTO());

            doAnswer(invocation -> {
                invocation.getArgument(0, Certificate.class).setLinks(expectedResourceLinks);
                return null;
            }).when(resourceLinkHelper)
                .decorateCertificateWithValidActionLinks(any(Certificate.class));

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertAll(
                () -> assertEquals(expectedResourceLinks.length, actualCertificate.getLinks().length),
                () -> assertEquals(expectedResourceLinks[0], actualCertificate.getLinks()[0]),
                () -> assertEquals(expectedResourceLinks[1], actualCertificate.getLinks()[1])
            );
        }
    }

    @Nested
    class ValidateCommonMetadata {

        @Test
        void shallIncludeCreatedDateTime() {
            final var expectedCreated = draft.getSkapad();

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedCreated, actualCertificate.getMetadata().getCreated());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100})
        void shallIncludeVersion(int expectedVersion) {
            draft.setVersion(expectedVersion);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedVersion, actualCertificate.getMetadata().getVersion());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shallIncludeForwarded(boolean expectedForwarded) {
            draft.setVidarebefordrad(expectedForwarded);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedForwarded, actualCertificate.getMetadata().isForwarded());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shallIncludeTestCertificate(boolean expectedTestCertificate) {
            draft.setTestIntyg(expectedTestCertificate);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedTestCertificate, actualCertificate.getMetadata().isTestCertificate());
        }
    }

    @Nested
    class ValidateUnit {

        @Test
        void shallContainCompleteUnitData() {
            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

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
            final var expectedCareProviderId = "CareProviderId";
            draft.setVardgivarId(expectedCareProviderId);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedCareProviderId, actualCertificate.getMetadata().getCareProvider().getUnitId());
        }

        @Test
        void shallIncludeCareProviderName() {
            final var expectedCareProviderName = "CareProviderName";
            draft.setVardgivarNamn(expectedCareProviderName);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedCareProviderName, actualCertificate.getMetadata().getCareProvider().getUnitName());
        }
    }

    @Nested
    class ValidateIssuedBy {

        @Test
        void shallIncludePersonId() {
            final var expectedPersonId = "PersonId";
            draft.getSkapadAv().setHsaId(expectedPersonId);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedPersonId, actualCertificate.getMetadata().getIssuedBy().getPersonId());
        }

        @Test
        void shallIncludeName() {
            final var expectedFullName = "Doctor Alpha";
            draft.getSkapadAv().setNamn(expectedFullName);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedFullName, actualCertificate.getMetadata().getIssuedBy().getFullName());
        }
    }

    @Nested
    class ValidatePatient {

        @Test
        void shallIncludePersonId() {
            final var expectedPersonId = new PersonId();
            expectedPersonId.setId(draft.getPatientPersonnummer().getPersonnummer());
            expectedPersonId.setType("PERSON_NUMMER");

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertAll(
                () -> assertEquals(expectedPersonId.getId(), actualCertificate.getMetadata().getPatient().getPersonId().getId()),
                () -> assertEquals(expectedPersonId.getType(), actualCertificate.getMetadata().getPatient().getPersonId().getType())
            );
        }

        @Test
        void shallIncludeFirstName() {
            final var expectedFirstName = "Fornamnet";
            draft.setPatientFornamn(expectedFirstName);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedFirstName, actualCertificate.getMetadata().getPatient().getFirstName());
        }

        @Test
        void shallIncludeMiddleName() {
            final var expectedMiddleName = "Mellannamnet";
            draft.setPatientMellannamn(expectedMiddleName);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedMiddleName, actualCertificate.getMetadata().getPatient().getMiddleName());
        }

        @Test
        void shallIncludeLastName() {
            final var expectedLastName = "Efternamnet";
            draft.setPatientEfternamn(expectedLastName);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedLastName, actualCertificate.getMetadata().getPatient().getLastName());
        }

        @Test
        void shallIncludeFullNameWithoutMiddleName() {
            final var expectedFullName = "Fornamnet Efternamnet";
            draft.setPatientFornamn("Fornamnet");
            draft.setPatientEfternamn("Efternamnet");

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedFullName, actualCertificate.getMetadata().getPatient().getFullName());
        }

        @Test
        void shallIncludeFullNameWithMiddleName() {
            final var expectedFullName = "Fornamnet Mellannamnet Efternamnet";
            draft.setPatientFornamn("Fornamnet");
            draft.setPatientMellannamn("Mellannamnet");
            draft.setPatientEfternamn("Efternamnet");

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedFullName, actualCertificate.getMetadata().getPatient().getFullName());
        }
    }

    @Nested
    class ValidateStatus {

        @Test
        void shallIncludeStatusUnsigned() {
            final var expectedStatus = CertificateStatus.UNSIGNED;

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }

        @Test
        void shallIncludeStatusSigned() {
            final var expectedStatus = CertificateStatus.SIGNED;
            draft.setStatus(UtkastStatus.SIGNED);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }

        @Test
        void shallIncludeStatusRevoked() {
            final var expectedStatus = CertificateStatus.REVOKED;
            draft.setStatus(UtkastStatus.SIGNED);
            draft.setAterkalladDatum(LocalDateTime.now());

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }

        @Test
        void shallIncludeStatusLocked() {
            final var expectedStatus = CertificateStatus.LOCKED;
            draft.setStatus(UtkastStatus.DRAFT_LOCKED);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }

        @Test
        void shallIncludeStatusLockedRevoked() {
            final var expectedStatus = CertificateStatus.LOCKED_REVOKED;
            draft.setStatus(UtkastStatus.DRAFT_LOCKED);
            draft.setAterkalladDatum(LocalDateTime.now());

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }
    }

    @Nested
    class ValidateParentRelation {

        private WebcertCertificateRelation parentRelation;

        @BeforeEach
        void setup() {
            parentRelation = new WebcertCertificateRelation(
                "ParentId",
                RelationKod.ERSATT,
                LocalDateTime.now(),
                UtkastStatus.SIGNED,
                false
            );

            relations.setParent(parentRelation);
        }

        @Test
        void shallNotIncludeParent() {
            relations.setParent(null);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertNull(actualCertificate.getMetadata().getRelations().getParent());
        }

        @Test
        void shallIncludeParentRelationCertificateId() {
            final var expectedParentId = "ParentId";
            parentRelation.setIntygsId(expectedParentId);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedParentId, actualCertificate.getMetadata().getRelations().getParent().getCertificateId());
        }

        @Test
        void shallIncludeParentRelationCreateDateTime() {
            final var expectedCreatedDateTime = LocalDateTime.now();
            parentRelation.setSkapad(expectedCreatedDateTime);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedCreatedDateTime, actualCertificate.getMetadata().getRelations().getParent().getCreated());
        }

        @Test
        void shallIncludeParentRelationStatusUnsignedWhenComplete() {
            final var expectedStatus = CertificateStatus.UNSIGNED;
            parentRelation.setStatus(UtkastStatus.DRAFT_COMPLETE);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationStatusUnsignedWhenIncomplete() {
            final var expectedStatus = CertificateStatus.UNSIGNED;
            parentRelation.setStatus(UtkastStatus.DRAFT_INCOMPLETE);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationStatusSigned() {
            final var expectedStatus = CertificateStatus.SIGNED;
            parentRelation.setStatus(UtkastStatus.SIGNED);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationStatusRevoked() {
            final var expectedStatus = CertificateStatus.REVOKED;
            parentRelation.setStatus(UtkastStatus.SIGNED);
            parentRelation.setMakulerat(true);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationStatusLocked() {
            final var expectedStatus = CertificateStatus.LOCKED;
            parentRelation.setStatus(UtkastStatus.DRAFT_LOCKED);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationStatusLockedRevoked() {
            final var expectedStatus = CertificateStatus.LOCKED_REVOKED;
            parentRelation.setStatus(UtkastStatus.DRAFT_LOCKED);
            parentRelation.setMakulerat(true);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationReplaced() {
            final var expectedRelationsType = CertificateRelationType.REPLACED;
            parentRelation.setRelationKod(RelationKod.ERSATT);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedRelationsType, actualCertificate.getMetadata().getRelations().getParent().getType());
        }

        @Test
        void shallIncludeParentRelationCopied() {
            final var expectedRelationsType = CertificateRelationType.COPIED;
            parentRelation.setRelationKod(RelationKod.KOPIA);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedRelationsType, actualCertificate.getMetadata().getRelations().getParent().getType());
        }

        @Test
        void shallIncludeParentRelationExtended() {
            final var expectedRelationsType = CertificateRelationType.EXTENDED;
            parentRelation.setRelationKod(RelationKod.FRLANG);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedRelationsType, actualCertificate.getMetadata().getRelations().getParent().getType());
        }

        @Test
        void shallIncludeParentRelationComplemented() {
            final var expectedRelationsType = CertificateRelationType.COMPLEMENTED;
            parentRelation.setRelationKod(RelationKod.KOMPLT);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedRelationsType, actualCertificate.getMetadata().getRelations().getParent().getType());
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ValidateChildRelation {

        @BeforeEach
        void setup() {
            final var parentRelation = new WebcertCertificateRelation(
                "ParentId",
                RelationKod.ERSATT,
                LocalDateTime.now(),
                UtkastStatus.SIGNED,
                false
            );

            relations.setParent(parentRelation);
        }

        Stream<FrontendRelations> allChildRelations() {
            return Stream.of(
                getReplacedByDraft().getLatestChildRelations(),
                getReplacedByCertificate().getLatestChildRelations(),
                getComplementedByDraft().getLatestChildRelations(),
                getComplementedByCertificate().getLatestChildRelations(),
                getDraftCopy().getLatestChildRelations()
            );
        }

        Stream<FrontendRelations> complementedChildRelations() {
            return Stream.of(
                getComplementedByDraft().getLatestChildRelations(),
                getComplementedByCertificate().getLatestChildRelations()
            );
        }

        Stream<FrontendRelations> replacedChildRelations() {
            return Stream.of(
                getReplacedByDraft().getLatestChildRelations(),
                getReplacedByCertificate().getLatestChildRelations()
            );
        }

        Stream<FrontendRelations> copiedChildRelations() {
            return Stream.of(
                getDraftCopy().getLatestChildRelations()
            );
        }

        @Test
        void shallNotIncludeParent() {
            relations.setParent(null);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertNull(actualCertificate.getMetadata().getRelations().getParent());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationCertificateId(FrontendRelations childRelations) {
            final var childRelation = getChildRelationToTest(childRelations);
            final var expectedCertificateId = childRelation.getIntygsId();
            childRelation.setIntygsId(expectedCertificateId);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedCertificateId, actualCertificate.getMetadata().getRelations().getChildren()[0].getCertificateId());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationCreateDateTime(FrontendRelations childRelations) {
            final var expectedCreatedDateTime = LocalDateTime.now();

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setSkapad(expectedCreatedDateTime);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedCreatedDateTime, actualCertificate.getMetadata().getRelations().getChildren()[0].getCreated());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusUnsignedWhenComplete(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.UNSIGNED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_COMPLETE);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusUnsignedWhenIncomplete(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.UNSIGNED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_INCOMPLETE);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusSigned(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.SIGNED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.SIGNED);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusRevoked(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.REVOKED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.SIGNED);
            childRelation.setMakulerat(true);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusLocked(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.LOCKED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_LOCKED);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusLockedRevoked(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.LOCKED_REVOKED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_LOCKED);
            childRelation.setMakulerat(true);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedStatus, actualCertificate.getMetadata().getRelations().getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("replacedChildRelations")
        void shallIncludeChildRelationReplaced(FrontendRelations childRelations) {
            final var expectedRelationsType = CertificateRelationType.REPLACED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setRelationKod(RelationKod.ERSATT);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedRelationsType, actualCertificate.getMetadata().getRelations().getChildren()[0].getType());
        }

        @ParameterizedTest
        @MethodSource("copiedChildRelations")
        void shallIncludeChildRelationCopied(FrontendRelations childRelations) {
            final var expectedRelationsType = CertificateRelationType.COPIED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setRelationKod(RelationKod.KOPIA);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedRelationsType, actualCertificate.getMetadata().getRelations().getChildren()[0].getType());
        }

        @ParameterizedTest
        @MethodSource("complementedChildRelations")
        void shallIncludeChildRelationComplemented(FrontendRelations childRelations) {
            final var expectedRelationsType = CertificateRelationType.COMPLEMENTED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setRelationKod(RelationKod.KOMPLT);

            final var actualCertificate = getCertificateService.getCertificate(draft.getIntygsId());

            assertEquals(expectedRelationsType, actualCertificate.getMetadata().getRelations().getChildren()[0].getType());
        }

        private WebcertCertificateRelation getChildRelationToTest(FrontendRelations childRelations) {
            relations.setLatestChildRelations(childRelations);
            if (childRelations.getReplacedByUtkast() != null) {
                return childRelations.getReplacedByUtkast();
            }
            if (childRelations.getReplacedByIntyg() != null) {
                return childRelations.getReplacedByIntyg();
            }
            if (childRelations.getComplementedByUtkast() != null) {
                return childRelations.getComplementedByUtkast();
            }
            if (childRelations.getComplementedByIntyg() != null) {
                return childRelations.getComplementedByIntyg();
            }
            if (childRelations.getUtkastCopy() != null) {
                return childRelations.getUtkastCopy();
            }
            throw new IllegalArgumentException("Incorrect test data! Please verify!");
        }

        private Relations getReplacedByDraft() {
            final var replacedByDraft = new Relations();
            replacedByDraft.getLatestChildRelations().setReplacedByUtkast(
                new WebcertCertificateRelation(
                    null,
                    RelationKod.ERSATT,
                    LocalDateTime.now(),
                    UtkastStatus.SIGNED,
                    false
                )
            );
            return replacedByDraft;
        }

        private Relations getReplacedByCertificate() {
            final var replacedByIntyg = new Relations();
            replacedByIntyg.getLatestChildRelations().setReplacedByIntyg(
                new WebcertCertificateRelation(
                    null,
                    RelationKod.ERSATT,
                    LocalDateTime.now(),
                    UtkastStatus.SIGNED,
                    false
                )
            );
            return replacedByIntyg;
        }

        private Relations getComplementedByDraft() {
            final var complementedByDraft = new Relations();
            complementedByDraft.getLatestChildRelations().setComplementedByUtkast(
                new WebcertCertificateRelation(
                    null,
                    RelationKod.KOMPLT,
                    LocalDateTime.now(),
                    UtkastStatus.SIGNED,
                    false
                )
            );
            return complementedByDraft;
        }

        private Relations getComplementedByCertificate() {
            final var complementedByIntyg = new Relations();
            complementedByIntyg.getLatestChildRelations().setComplementedByIntyg(
                new WebcertCertificateRelation(
                    null,
                    RelationKod.KOMPLT,
                    LocalDateTime.now(),
                    UtkastStatus.SIGNED,
                    false
                )
            );
            return complementedByIntyg;
        }

        private Relations getDraftCopy() {
            final var draftCopy = new Relations();
            draftCopy.getLatestChildRelations().setUtkastCopy(
                new WebcertCertificateRelation(
                    null,
                    RelationKod.KOPIA,
                    LocalDateTime.now(),
                    UtkastStatus.SIGNED,
                    false
                )
            );
            return draftCopy;
        }
    }

    private Utkast createDraft() {
        final var draft = new Utkast();
        draft.setIntygsId("certificateId");
        draft.setIntygsTyp("certificateType");
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setModel("draftJson");
        draft.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        draft.setSkapad(LocalDateTime.now());
        draft.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").orElseThrow());
        draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
        return draft;
    }

    private Certificate createCertificate() {
        return CertificateBuilder.create()
            .metadata(
                CertificateMetadataBuilder.create()
                    .id("certificateId")
                    .type("certificateType")
                    .typeVersion("certificateTypeVersion")
                    .unit(Unit.create()
                        .unitId("unitId")
                        .unitName("unitName")
                        .address("address")
                        .zipCode("zipCode")
                        .city("city")
                        .email("email")
                        .phoneNumber("phoneNumber")
                        .build()
                    )
                    .build()
            )
            .build();
    }

}