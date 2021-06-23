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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper.createCertificate;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificatesAvailableFunctionsImplTest {

    @InjectMocks
    private GetCertificatesAvailableFunctionsImpl getCertificatesAvailableFunctions;

    @Nested
    class Draft {

        @Test
        void shallIncludeEditCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.EDIT_CERTIFICATE);
        }

        @Test
        void shallIncludePrintCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeDeleteCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REMOVE_CERTIFICATE);
        }

        @Test
        void shallIncludeSignCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        }

        @Test
        void shallIncludeForwardCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
        }

        @Test
        void shallIncludeFMB() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.FMB);
        }

        @Test
        void shallExcludeFMB() {
            final var certificate = createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.FMB);
        }

        @Test
        void shallExcludeSend() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }
    }

    @Nested
    class LockedDraft {

        @Test
        void shallIncludePrintCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeCopyCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.COPY_CERTIFICATE);
        }

        @Test
        void shallIncludeInvalidateCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }

        @Test
        void shallExcludeSend() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }
    }

    @Nested
    class Certificates {

        @Test
        void shallIncludePrintCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeReplaceCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
        }

        @Test
        void shallIncludeRenewCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallExcludeRenewCertificate() {
            final var certificate = createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallIncludeInvalidateCertificate() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }

        @Test
        void shallIncludeSend() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }

        @Test
        void shallExcludeSend() {
            final var certificate = createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            certificate.getMetadata().setSent(true);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }
    }

    private void assertInclude(List<ResourceLinkDTO> availableFunctions, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(availableFunctions, type);
        assertNotNull(actualResourceLink, () -> String.format("Expected resource link with type '%s'", type));
    }

    private void assertExclude(List<ResourceLinkDTO> availableFunctions, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(availableFunctions, type);
        assertNull(actualResourceLink, () -> String.format("Don't expect resource link with type '%s'", type));
    }

    private ResourceLinkDTO get(List<ResourceLinkDTO> resourceLinks, ResourceLinkTypeDTO type) {
        return resourceLinks.stream()
            .filter(resourceLinkDTO -> resourceLinkDTO.getType().equals(type))
            .findFirst()
            .orElse(null);
    }
}