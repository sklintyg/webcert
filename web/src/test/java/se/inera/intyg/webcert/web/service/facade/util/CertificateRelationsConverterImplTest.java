package se.inera.intyg.webcert.web.service.facade.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

    @InjectMocks
    private CertificateRelationsConverterImpl certificateRelationsConverter;

    private final Relations relations = new Relations();
    private final String CERTIFICATE_ID = "certificateId";

    @BeforeEach
    void setupMocks() throws Exception {
        doReturn(relations)
            .when(certificateRelationService).getRelations(CERTIFICATE_ID);
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

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertNull(actualRelations.getParent());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationCertificateId(FrontendRelations childRelations) {
            final var childRelation = getChildRelationToTest(childRelations);
            final var expectedCertificateId = childRelation.getIntygsId();
            childRelation.setIntygsId(expectedCertificateId);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedCertificateId, actualRelations.getChildren()[0].getCertificateId());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationCreateDateTime(FrontendRelations childRelations) {
            final var expectedCreatedDateTime = LocalDateTime.now();

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setSkapad(expectedCreatedDateTime);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedCreatedDateTime, actualRelations.getChildren()[0].getCreated());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusUnsignedWhenComplete(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.UNSIGNED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_COMPLETE);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusUnsignedWhenIncomplete(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.UNSIGNED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_INCOMPLETE);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusSigned(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.SIGNED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.SIGNED);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusRevoked(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.REVOKED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.SIGNED);
            childRelation.setMakulerat(true);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusLocked(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.LOCKED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_LOCKED);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("allChildRelations")
        void shallIncludeChildRelationStatusLockedRevoked(FrontendRelations childRelations) {
            final var expectedStatus = CertificateStatus.LOCKED_REVOKED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setStatus(UtkastStatus.DRAFT_LOCKED);
            childRelation.setMakulerat(true);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedStatus, actualRelations.getChildren()[0].getStatus());
        }

        @ParameterizedTest
        @MethodSource("replacedChildRelations")
        void shallIncludeChildRelationReplaced(FrontendRelations childRelations) {
            final var expectedRelationsType = CertificateRelationType.REPLACED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setRelationKod(RelationKod.ERSATT);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedRelationsType, actualRelations.getChildren()[0].getType());
        }

        @ParameterizedTest
        @MethodSource("copiedChildRelations")
        void shallIncludeChildRelationCopied(FrontendRelations childRelations) {
            final var expectedRelationsType = CertificateRelationType.COPIED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setRelationKod(RelationKod.KOPIA);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedRelationsType, actualRelations.getChildren()[0].getType());
        }

        @ParameterizedTest
        @MethodSource("complementedChildRelations")
        void shallIncludeChildRelationComplemented(FrontendRelations childRelations) {
            final var expectedRelationsType = CertificateRelationType.COMPLEMENTED;

            final var childRelation = getChildRelationToTest(childRelations);
            childRelation.setRelationKod(RelationKod.KOMPLT);

            final var actualRelations = certificateRelationsConverter.convert(CERTIFICATE_ID);

            assertEquals(expectedRelationsType, actualRelations.getChildren()[0].getType());
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
}