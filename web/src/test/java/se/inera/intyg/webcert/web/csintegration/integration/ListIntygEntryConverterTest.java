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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.link.ResourceLink;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.WebcertCertificateRelation;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations;
import se.inera.intyg.webcert.web.web.controller.api.dto.Relations.FrontendRelations;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@ExtendWith(MockitoExtension.class)
class ListIntygEntryConverterTest {

    @InjectMocks
    private ListIntygEntryConverter listIntygEntryConverter;
    private static final Certificate CERTIFICATE = CertificateFacadeTestHelper.createCertificateTypeWithVersion(
        "type", CertificateStatus.UNSIGNED, true, "typeVersion");

    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer("191212121212").get();
    private static final String RELATION_ID = "relationId";
    private static final LocalDateTime CREATED = LocalDateTime.now();

    @Test
    void shouldConvertCertificateId() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getId(), response.getIntygId());
    }

    @Test
    void shouldConvertCertificateType() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getType(), response.getIntygType());
    }

    @Test
    void shouldConvertCertificateTypeName() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getName(), response.getIntygTypeName());
    }

    @Test
    void shouldConvertCertificateTypeVersion() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getTypeVersion(), response.getIntygTypeVersion());
    }

    @Test
    void shouldConvertCertificateVersion() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getVersion(), response.getVersion());
    }

    @Test
    void shouldConvertTestCertificate() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().isTestCertificate(), response.isTestIntyg());
    }

    @Test
    void shouldConvertStatusDraftAndValidForSignFalseToDraftIncomplete() {
        final var certificate = CertificateFacadeTestHelper.createCertificateTypeWithVersion("type",
            CertificateStatus.UNSIGNED, true, "typeVersion");
        certificate.getMetadata().setValidForSign(false);

        final var response = listIntygEntryConverter.convert(certificate);
        assertEquals(UtkastStatus.DRAFT_INCOMPLETE.toString(), response.getStatus());
    }

    @Test
    void shouldConvertStatusDraftAndValidForSignTrueToDraftComplete() {
        final var certificate = CertificateFacadeTestHelper.createCertificateTypeWithVersion("type",
            CertificateStatus.UNSIGNED, true, "typeVersion");
        certificate.getMetadata().setValidForSign(true);

        final var response = listIntygEntryConverter.convert(certificate);
        assertEquals(UtkastStatus.DRAFT_COMPLETE.toString(), response.getStatus());
    }

    @Test
    void shouldConvertStatusSignedToUtkastStatusSigned() {
        final var certificate = CertificateFacadeTestHelper.createCertificateTypeWithVersion("type",
            CertificateStatus.SIGNED, true, "typeVersion");
        certificate.getMetadata().setValidForSign(true);

        final var response = listIntygEntryConverter.convert(certificate);
        assertEquals(UtkastStatus.SIGNED.toString(), response.getStatus());
    }

    @Test
    void shouldConvertStatusSent() {
        final var certificate = CertificateFacadeTestHelper.createCertificateTypeWithVersion("type",
            CertificateStatus.SIGNED, true, "typeVersion");
        certificate.getMetadata().setSent(true);

        final var response = listIntygEntryConverter.convert(certificate);
        assertEquals("SENT", response.getStatus());
    }

    @Test
    void shouldConvertStatusLockedDraft() {
        final var certificate = CertificateFacadeTestHelper.createCertificateTypeWithVersion("type",
            CertificateStatus.LOCKED, true, "typeVersion");

        final var response = listIntygEntryConverter.convert(certificate);
        assertEquals(UtkastStatus.DRAFT_LOCKED.name(), response.getStatus());
    }

    @Test
    void shouldConvertStatusRevoked() {
        final var certificate = CertificateFacadeTestHelper.createCertificateTypeWithVersion("type",
            CertificateStatus.REVOKED, true, "typeVersion");
        certificate.getMetadata().setSent(true);

        final var response = listIntygEntryConverter.convert(certificate);
        assertEquals("CANCELLED", response.getStatus());
    }

    @Test
    void shouldConvertStatusName() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getStatus().name(), response.getStatusName());
    }

    @Test
    void shouldConvertPatientId() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(PERSONNUMMER, response.getPatientId());
    }

    @Test
    void shouldConvertDeceased() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getPatient().isDeceased(), response.isAvliden());
    }

    @Test
    void shouldConvertProtectedPerson() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getPatient().isProtectedPerson(), response.isSekretessmarkering());
    }

    @Test
    void shouldConvertForwarded() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().isForwarded(), response.isVidarebefordrad());
    }

    @Test
    void shouldConvertUpdatedSignedBy() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getIssuedBy().getFullName(), response.getUpdatedSignedBy());
    }

    @Test
    void shouldConvertUpdatedSignedById() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getIssuedBy().getPersonId(), response.getUpdatedSignedById());
    }

    @Test
    void shouldConvertSignedDate() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getSigned(), response.getSigned());
    }

    @Test
    void shouldConvertModifiedDate() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getModified(), response.getLastUpdated());
    }

    @Test
    void shouldConvertCareUnit() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getCareUnit().getUnitId(), response.getVardenhetId());
    }

    @Test
    void shouldConvertCareProviderId() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getCareProvider().getUnitId(), response.getVardgivarId());
    }

    @Nested
    class ResourceLinks {

        @Test
        void shouldConvertReadLink() {
            CERTIFICATE.setLinks(List.of(ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.READ_CERTIFICATE)
                    .build()
                )
            );

            final var response = listIntygEntryConverter.convert(CERTIFICATE);
            assertTrue(response.getLinks().stream().anyMatch(link -> link.getType() == ActionLinkType.LASA_INTYG));
        }

        @Test
        void shouldConvertForwardLink() {
            CERTIFICATE.setLinks(List.of(ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.FORWARD_CERTIFICATE)
                    .build()
                )
            );

            final var response = listIntygEntryConverter.convert(CERTIFICATE);
            assertTrue(response.getLinks().stream().anyMatch(link -> link.getType() == ActionLinkType.VIDAREBEFORDRA_UTKAST));
        }

        @Test
        void shouldConvertForwardCertificateFromListLink() {
            CERTIFICATE.setLinks(List.of(ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.FORWARD_CERTIFICATE_FROM_LIST)
                    .build()
                )
            );

            final var response = listIntygEntryConverter.convert(CERTIFICATE);
            assertTrue(response.getLinks().stream().anyMatch(link -> link.getType() == ActionLinkType.VIDAREBEFORDRA_UTKAST));
        }

        @Test
        void shouldConvertRenewLink() {
            CERTIFICATE.setLinks(List.of(ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.RENEW_CERTIFICATE)
                    .build()
                )
            );

            final var response = listIntygEntryConverter.convert(CERTIFICATE);
            assertTrue(
                response.getLinks().stream().anyMatch(link -> link.getType() == ActionLinkType.FORNYA_INTYG_FRAN_CERTIFICATE_SERVICE));
        }

        @Test
        void shouldNotIncludeLinksNotSupported() {
            CERTIFICATE.setLinks(List.of(ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.COPY_CERTIFICATE)
                    .build()
                )
            );

            final var response = listIntygEntryConverter.convert(CERTIFICATE);
            assertEquals(Collections.emptyList(), response.getLinks());
        }
    }

    @Nested
    class ConvertRelations {

        @Test
        void shouldReturnNullLatestChildRelationIfNoReplaceRelations() {
            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation);

            assertNull(response.getRelations().getLatestChildRelations().getReplacedByIntyg());
        }

        @Test
        void shouldConvertReplaceChildRelation() {
            final var replacedByIntyg = createWebcertCertificateRelation(RelationKod.ERSATT, UtkastStatus.SIGNED, false);

            final var relation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.REPLACED)
                .status(CertificateStatus.SIGNED)
                .created(CREATED)
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, relation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(replacedByIntyg.getIntygsId(), response.getReplacedByIntyg().getIntygsId()),
                () -> assertEquals(replacedByIntyg.getRelationKod(), response.getReplacedByIntyg().getRelationKod()),
                () -> assertEquals(replacedByIntyg.getSkapad(), response.getReplacedByIntyg().getSkapad()),
                () -> assertEquals(replacedByIntyg.getStatus(), response.getReplacedByIntyg().getStatus()),
                () -> assertEquals(replacedByIntyg.isMakulerat(), response.getReplacedByIntyg().isMakulerat())
            );
        }

        @Test
        void shouldConvertReplaceChildRelationButRevoked() {
            final var replacedByIntyg = createWebcertCertificateRelation(RelationKod.ERSATT, UtkastStatus.SIGNED, true);

            final var relation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.REPLACED)
                .status(CertificateStatus.REVOKED)
                .created(CREATED)
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, relation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(replacedByIntyg.getIntygsId(), response.getReplacedByIntyg().getIntygsId()),
                () -> assertEquals(replacedByIntyg.getRelationKod(), response.getReplacedByIntyg().getRelationKod()),
                () -> assertEquals(replacedByIntyg.getSkapad(), response.getReplacedByIntyg().getSkapad()),
                () -> assertEquals(replacedByIntyg.getStatus(), response.getReplacedByIntyg().getStatus()),
                () -> assertEquals(replacedByIntyg.isMakulerat(), response.getReplacedByIntyg().isMakulerat())
            );
        }

        @Test
        void shouldConvertReplaceChildRelationButDraft() {
            final var replacedByUtkast = createWebcertCertificateRelation(RelationKod.ERSATT, UtkastStatus.DRAFT_COMPLETE, false);

            final var relation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.REPLACED)
                .status(CertificateStatus.UNSIGNED)
                .created(CREATED)
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, relation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(replacedByUtkast.getIntygsId(), response.getReplacedByUtkast().getIntygsId()),
                () -> assertEquals(replacedByUtkast.getRelationKod(), response.getReplacedByUtkast().getRelationKod()),
                () -> assertEquals(replacedByUtkast.getSkapad(), response.getReplacedByUtkast().getSkapad()),
                () -> assertEquals(replacedByUtkast.getStatus(), response.getReplacedByUtkast().getStatus()),
                () -> assertEquals(replacedByUtkast.isMakulerat(), response.getReplacedByUtkast().isMakulerat())
            );
        }

        @Test
        void shouldConvertReplaceChildRelationButDraftLocked() {
            final var replacedByUtkast = createWebcertCertificateRelation(RelationKod.ERSATT, UtkastStatus.DRAFT_LOCKED, false);

            final var relation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.REPLACED)
                .status(CertificateStatus.LOCKED)
                .created(CREATED)
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, relation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(replacedByUtkast.getIntygsId(), response.getReplacedByUtkast().getIntygsId()),
                () -> assertEquals(replacedByUtkast.getRelationKod(), response.getReplacedByUtkast().getRelationKod()),
                () -> assertEquals(replacedByUtkast.getSkapad(), response.getReplacedByUtkast().getSkapad()),
                () -> assertEquals(replacedByUtkast.getStatus(), response.getReplacedByUtkast().getStatus()),
                () -> assertEquals(replacedByUtkast.isMakulerat(), response.getReplacedByUtkast().isMakulerat())
            );
        }

        @Test
        void shouldConvertReplaceChildRelationButDraftLockedRevoked() {
            final var replacedByUtkast = createWebcertCertificateRelation(RelationKod.ERSATT, UtkastStatus.DRAFT_LOCKED, true);

            final var relation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.REPLACED)
                .status(CertificateStatus.LOCKED_REVOKED)
                .created(CREATED)
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, relation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(replacedByUtkast.getIntygsId(), response.getReplacedByUtkast().getIntygsId()),
                () -> assertEquals(replacedByUtkast.getRelationKod(), response.getReplacedByUtkast().getRelationKod()),
                () -> assertEquals(replacedByUtkast.getSkapad(), response.getReplacedByUtkast().getSkapad()),
                () -> assertEquals(replacedByUtkast.getStatus(), response.getReplacedByUtkast().getStatus()),
                () -> assertEquals(replacedByUtkast.isMakulerat(), response.getReplacedByUtkast().isMakulerat())
            );
        }

        @Test
        void shouldAlwaysUseTheLatestReplaceChildRelation() {
            final var replacedByIntyg = createWebcertCertificateRelation(RelationKod.ERSATT, UtkastStatus.SIGNED, false);

            final var latestRelation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.REPLACED)
                .status(CertificateStatus.SIGNED)
                .created(CREATED)
                .build();

            final var otherRelation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.COMPLEMENTED)
                .status(CertificateStatus.SIGNED)
                .created(CREATED.minusDays(1))
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, otherRelation, latestRelation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(replacedByIntyg.getIntygsId(), response.getReplacedByIntyg().getIntygsId()),
                () -> assertEquals(replacedByIntyg.getRelationKod(), response.getReplacedByIntyg().getRelationKod()),
                () -> assertEquals(replacedByIntyg.getSkapad(), response.getReplacedByIntyg().getSkapad()),
                () -> assertEquals(replacedByIntyg.getStatus(), response.getReplacedByIntyg().getStatus()),
                () -> assertEquals(replacedByIntyg.isMakulerat(), response.getReplacedByIntyg().isMakulerat())
            );
        }

        @Test
        void shouldConvertComplementChildRelation() {
            final var complementedByIntyg = createWebcertCertificateRelation(RelationKod.KOMPLT, UtkastStatus.SIGNED, false);

            final var relation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.COMPLEMENTED)
                .status(CertificateStatus.SIGNED)
                .created(CREATED)
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, relation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(complementedByIntyg.getIntygsId(), response.getComplementedByIntyg().getIntygsId()),
                () -> assertEquals(complementedByIntyg.getRelationKod(), response.getComplementedByIntyg().getRelationKod()),
                () -> assertEquals(complementedByIntyg.getSkapad(), response.getComplementedByIntyg().getSkapad()),
                () -> assertEquals(complementedByIntyg.getStatus(), response.getComplementedByIntyg().getStatus()),
                () -> assertEquals(complementedByIntyg.isMakulerat(), response.getComplementedByIntyg().isMakulerat())
            );
        }

        @Test
        void shouldConvertComplementChildRelationButRevoked() {
            final var complementedByIntyg = createWebcertCertificateRelation(RelationKod.KOMPLT, UtkastStatus.SIGNED, true);

            final var relation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.COMPLEMENTED)
                .status(CertificateStatus.REVOKED)
                .created(CREATED)
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, relation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(complementedByIntyg.getIntygsId(), response.getComplementedByIntyg().getIntygsId()),
                () -> assertEquals(complementedByIntyg.getRelationKod(), response.getComplementedByIntyg().getRelationKod()),
                () -> assertEquals(complementedByIntyg.getSkapad(), response.getComplementedByIntyg().getSkapad()),
                () -> assertEquals(complementedByIntyg.getStatus(), response.getComplementedByIntyg().getStatus()),
                () -> assertEquals(complementedByIntyg.isMakulerat(), response.getComplementedByIntyg().isMakulerat())
            );
        }

        @Test
        void shouldConvertComplementChildRelationButDraft() {
            final var complementedByUtkast = createWebcertCertificateRelation(RelationKod.KOMPLT, UtkastStatus.DRAFT_COMPLETE, false);

            final var relation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.COMPLEMENTED)
                .status(CertificateStatus.UNSIGNED)
                .created(CREATED)
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, relation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(complementedByUtkast.getIntygsId(), response.getComplementedByUtkast().getIntygsId()),
                () -> assertEquals(complementedByUtkast.getRelationKod(), response.getComplementedByUtkast().getRelationKod()),
                () -> assertEquals(complementedByUtkast.getSkapad(), response.getComplementedByUtkast().getSkapad()),
                () -> assertEquals(complementedByUtkast.getStatus(), response.getComplementedByUtkast().getStatus()),
                () -> assertEquals(complementedByUtkast.isMakulerat(), response.getComplementedByUtkast().isMakulerat())
            );
        }

        @Test
        void shouldConvertComplementChildRelationButDraftLocked() {
            final var complementedByUtkast = createWebcertCertificateRelation(RelationKod.KOMPLT, UtkastStatus.DRAFT_LOCKED, false);

            final var relation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.COMPLEMENTED)
                .status(CertificateStatus.LOCKED)
                .created(CREATED)
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, relation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(complementedByUtkast.getIntygsId(), response.getComplementedByUtkast().getIntygsId()),
                () -> assertEquals(complementedByUtkast.getRelationKod(), response.getComplementedByUtkast().getRelationKod()),
                () -> assertEquals(complementedByUtkast.getSkapad(), response.getComplementedByUtkast().getSkapad()),
                () -> assertEquals(complementedByUtkast.getStatus(), response.getComplementedByUtkast().getStatus()),
                () -> assertEquals(complementedByUtkast.isMakulerat(), response.getComplementedByUtkast().isMakulerat())
            );
        }

        @Test
        void shouldConvertComplementChildRelationButDraftLockedRevoked() {
            final var complementedByUtkast = createWebcertCertificateRelation(RelationKod.KOMPLT, UtkastStatus.DRAFT_LOCKED, true);

            final var relation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.COMPLEMENTED)
                .status(CertificateStatus.LOCKED_REVOKED)
                .created(CREATED)
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, relation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(complementedByUtkast.getIntygsId(), response.getComplementedByUtkast().getIntygsId()),
                () -> assertEquals(complementedByUtkast.getRelationKod(), response.getComplementedByUtkast().getRelationKod()),
                () -> assertEquals(complementedByUtkast.getSkapad(), response.getComplementedByUtkast().getSkapad()),
                () -> assertEquals(complementedByUtkast.getStatus(), response.getComplementedByUtkast().getStatus()),
                () -> assertEquals(complementedByUtkast.isMakulerat(), response.getComplementedByUtkast().isMakulerat())
            );
        }

        @Test
        void shouldAlwaysUseTheLatestComplementChildRelation() {
            final var complementedByIntyg = createWebcertCertificateRelation(RelationKod.KOMPLT, UtkastStatus.SIGNED, false);

            final var latestRelation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.COMPLEMENTED)
                .status(CertificateStatus.SIGNED)
                .created(CREATED)
                .build();

            final var otherRelation = CertificateRelation.builder()
                .certificateId(RELATION_ID)
                .type(CertificateRelationType.REPLACED)
                .status(CertificateStatus.SIGNED)
                .created(CREATED.minusDays(1))
                .build();

            final var certificateWithChildRelation = CertificateFacadeTestHelper.createCertificateWithChildRelation("type",
                CertificateStatus.UNSIGNED, otherRelation, latestRelation);

            final var response = listIntygEntryConverter.convert(certificateWithChildRelation).getRelations().getLatestChildRelations();

            assertAll(
                () -> assertEquals(complementedByIntyg.getIntygsId(), response.getComplementedByIntyg().getIntygsId()),
                () -> assertEquals(complementedByIntyg.getRelationKod(), response.getComplementedByIntyg().getRelationKod()),
                () -> assertEquals(complementedByIntyg.getSkapad(), response.getComplementedByIntyg().getSkapad()),
                () -> assertEquals(complementedByIntyg.getStatus(), response.getComplementedByIntyg().getStatus()),
                () -> assertEquals(complementedByIntyg.isMakulerat(), response.getComplementedByIntyg().isMakulerat())
            );
        }

        private WebcertCertificateRelation createWebcertCertificateRelation(RelationKod relationKod, UtkastStatus utkastStatus,
            boolean makulerat) {
            final var expectedRelations = new Relations();
            expectedRelations.setLatestChildRelations(
                new FrontendRelations()
            );

            final var replacedByIntyg = new WebcertCertificateRelation();
            replacedByIntyg.setIntygsId(RELATION_ID);
            replacedByIntyg.setRelationKod(relationKod);
            replacedByIntyg.setStatus(utkastStatus);
            replacedByIntyg.setMakulerat(makulerat);
            replacedByIntyg.setSkapad(CREATED);
            expectedRelations.getLatestChildRelations().setReplacedByIntyg(
                replacedByIntyg
            );
            return replacedByIntyg;
        }
    }
}
