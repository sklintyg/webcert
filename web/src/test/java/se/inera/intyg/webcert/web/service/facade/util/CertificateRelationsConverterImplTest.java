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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.service.relation.CertificateRelationService;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;

@ExtendWith(MockitoExtension.class)
class CertificateRelationsConverterImplTest {

    @Mock
    private CertificateRelationService certificateRelationService;

    @Mock
    private CertificateRelationsParentHelper certificateRelationsParentHelper;

    @InjectMocks
    private CertificateRelationsConverterImpl certificateRelationsConverter;

    private static final Relations relations = new Relations();
    private static final String CERTIFICATE_ID = "certificateId";

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

            doReturn(relations)
                .when(certificateRelationService).getRelations(CERTIFICATE_ID);
        }

        @Test
        void shallNotIncludeParent() {
            relations.setParent(null);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertNull(actualRelations.getParent());
        }

        @Test
        void shallIncludeParentRelationCertificateId() {
            final var expectedParentId = "ParentId";
            parentRelation.setIntygsId(expectedParentId);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedParentId, actualRelations.getParent().getCertificateId());
        }

        @Test
        void shallIncludeParentRelationCreateDateTime() {
            final var expectedCreatedDateTime = LocalDateTime.now();
            parentRelation.setSkapad(expectedCreatedDateTime);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedCreatedDateTime, actualRelations.getParent().getCreated());
        }

        @Test
        void shallIncludeParentRelationStatusUnsignedWhenComplete() {
            final var expectedStatus = CertificateStatus.UNSIGNED;
            parentRelation.setStatus(UtkastStatus.DRAFT_COMPLETE);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationStatusUnsignedWhenIncomplete() {
            final var expectedStatus = CertificateStatus.UNSIGNED;
            parentRelation.setStatus(UtkastStatus.DRAFT_INCOMPLETE);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationStatusSigned() {
            final var expectedStatus = CertificateStatus.SIGNED;
            parentRelation.setStatus(UtkastStatus.SIGNED);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationStatusRevoked() {
            final var expectedStatus = CertificateStatus.REVOKED;
            parentRelation.setStatus(UtkastStatus.SIGNED);
            parentRelation.setMakulerat(true);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationStatusLocked() {
            final var expectedStatus = CertificateStatus.LOCKED;
            parentRelation.setStatus(UtkastStatus.DRAFT_LOCKED);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationStatusLockedRevoked() {
            final var expectedStatus = CertificateStatus.LOCKED_REVOKED;
            parentRelation.setStatus(UtkastStatus.DRAFT_LOCKED);
            parentRelation.setMakulerat(true);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getParent().getStatus());
        }

        @Test
        void shallIncludeParentRelationReplaced() {
            final var expectedRelationsType = CertificateRelationType.REPLACED;
            parentRelation.setRelationKod(RelationKod.ERSATT);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedRelationsType, actualRelations.getParent().getType());
        }

        @Test
        void shallIncludeParentRelationCopied() {
            final var expectedRelationsType = CertificateRelationType.COPIED;
            parentRelation.setRelationKod(RelationKod.KOPIA);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedRelationsType, actualRelations.getParent().getType());
        }

        @Test
        void shallIncludeParentRelationExtended() {
            final var expectedRelationsType = CertificateRelationType.EXTENDED;
            parentRelation.setRelationKod(RelationKod.FRLANG);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedRelationsType, actualRelations.getParent().getType());
        }

        @Test
        void shallIncludeParentRelationComplemented() {
            final var expectedRelationsType = CertificateRelationType.COMPLEMENTED;
            parentRelation.setRelationKod(RelationKod.KOMPLT);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedRelationsType, actualRelations.getParent().getType());
        }

        @Test
        void shallGetParentRelationFromITIfNotExistsInWC() {
            relations.setParent(null);

            doReturn(new WebcertCertificateRelation(
                "ParentId",
                RelationKod.ERSATT,
                LocalDateTime.now(),
                UtkastStatus.SIGNED,
                false
            )).when(certificateRelationsParentHelper).getParentFromITIfExists(CERTIFICATE_ID);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertNotNull(actualRelations.getParent());
        }
    }

    @Nested
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

            doReturn(relations)
                .when(certificateRelationService).getRelations(CERTIFICATE_ID);
        }

        @Test
        void shallNotIncludeParent() {
            relations.setParent(null);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertNull(actualRelations.getParent());
        }

        @ParameterizedTest
        @ArgumentsSource(AllChildRelations.class)
        void shallIncludeChildRelationCreateDateTime(FrontendRelations childRelations) {
            final var expectedCreatedDateTime = LocalDateTime.now();

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setSkapad(expectedCreatedDateTime);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedCreatedDateTime, actualRelations.getChildren()[0].getCreated());
        }

        @ParameterizedTest
        @ArgumentsSource(AllChildRelations.class)
        void shallIncludeChildRelationStatusUnsignedWhenComplete(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.UNSIGNED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_COMPLETE);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @ArgumentsSource(AllChildRelations.class)
        void shallIncludeChildRelationStatusUnsignedWhenIncomplete(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.UNSIGNED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_INCOMPLETE);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @ArgumentsSource(AllChildRelations.class)
        void shallIncludeChildRelationStatusSigned(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.SIGNED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.SIGNED);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @ArgumentsSource(AllChildRelations.class)
        void shallIncludeChildRelationStatusRevoked(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.REVOKED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.SIGNED);
            childRelation.setMakulerat(true);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @ArgumentsSource(AllChildRelations.class)
        void shallIncludeChildRelationStatusLocked(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.LOCKED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_LOCKED);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @ArgumentsSource(AllChildRelations.class)
        void shallIncludeChildRelationStatusLockedRevoked(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.LOCKED_REVOKED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_LOCKED);
            childRelation.setMakulerat(true);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @ArgumentsSource(ReplacedChildRelations.class)
        void shallIncludeChildRelationReplaced(FrontendRelations childRelations) {
            final var expectedRelationsType = CertificateRelationType.REPLACED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setRelationKod(RelationKod.ERSATT);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedRelationsType, actualRelations.getChildren()[0].getType());
        }

        @ParameterizedTest
        @ArgumentsSource(CopiedChildRelations.class)
        void shallIncludeChildRelationCopied(FrontendRelations childRelations) {
            final var expectedRelationsType = CertificateRelationType.COPIED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setRelationKod(RelationKod.KOPIA);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedRelationsType, actualRelations.getChildren()[0].getType());
        }

        @ParameterizedTest
        @ArgumentsSource(ComplementedChildRelations.class)
        void shallIncludeChildRelationComplemented(FrontendRelations childRelations) {
            final var expectedRelationsType = CertificateRelationType.COMPLEMENTED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setRelationKod(RelationKod.KOMPLT);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedRelationsType, actualRelations.getChildren()[0].getType());
        }
    }

    @Nested
    class ValidateChildRelatinCertificateId {

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

        @ParameterizedTest
        @ArgumentsSource(AllChildRelations.class)
        void shallIncludeChildRelationCertificateId(FrontendRelations childRelations) {
            final var childRelation = getChildRelationToTest(childRelations);
            final var expectedCertificateId = childRelation.getIntygsId();
            childRelation.setIntygsId(expectedCertificateId);
            doReturn(relations).when(certificateRelationService).getRelations(CERTIFICATE_ID);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedCertificateId, actualRelations.getChildren()[0].getCertificateId());
        }
    }

    private static WebcertCertificateRelation getChildRelationToTest(FrontendRelations childRelations) {
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

    private static class AllChildRelations implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(RelationsProvider.getReplacedByDraft().getLatestChildRelations()),
                Arguments.of(RelationsProvider.getReplacedByCertificate().getLatestChildRelations()),
                Arguments.of(RelationsProvider.getComplementedByDraft().getLatestChildRelations()),
                Arguments.of(RelationsProvider.getComplementedByCertificate().getLatestChildRelations()),
                Arguments.of(RelationsProvider.getDraftCopy().getLatestChildRelations())
            );
        }
    }

    private static class ComplementedChildRelations implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(RelationsProvider.getComplementedByDraft().getLatestChildRelations()),
                Arguments.of(RelationsProvider.getComplementedByCertificate().getLatestChildRelations())
            );
        }
    }

    private static class ReplacedChildRelations implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(RelationsProvider.getReplacedByDraft().getLatestChildRelations()),
                Arguments.of(RelationsProvider.getReplacedByCertificate().getLatestChildRelations())
            );
        }
    }

    private static class CopiedChildRelations implements ArgumentsProvider {

        @Override
        public Stream<Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(RelationsProvider.getDraftCopy().getLatestChildRelations())
            );
        }
    }

    private static class RelationsProvider {

        private static Relations getReplacedByDraft() {
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

        private static Relations getReplacedByCertificate() {
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

        private static Relations getComplementedByDraft() {
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

        private static Relations getComplementedByCertificate() {
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

        private static Relations getDraftCopy() {
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

}
