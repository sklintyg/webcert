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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.link.ResourceLink;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLinkType;

@ExtendWith(MockitoExtension.class)
class ListIntygEntryConverterTest {

    @InjectMocks
    private ListIntygEntryConverter listIntygEntryConverter;
    private static final Certificate CERTIFICATE = CertificateFacadeTestHelper.createCertificateTypeWithVersion(
        "type", CertificateStatus.UNSIGNED, true, "typeVersion");

    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer("191212121212").get();

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
    void shouldConvertCreated() {
        final var response = listIntygEntryConverter.convert(CERTIFICATE);
        assertEquals(CERTIFICATE.getMetadata().getCreated(), response.getLastUpdatedSigned());
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
}
