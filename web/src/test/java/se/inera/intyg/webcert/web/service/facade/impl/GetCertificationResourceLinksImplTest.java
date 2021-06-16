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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessServiceHelper;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificationResourceLinksImplTest {

    @Mock
    private DraftAccessServiceHelper draftAccessServiceHelper;

    @Mock
    private LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper;

    @Mock
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    @InjectMocks
    private GetCertificationResourceLinksImpl getCertificationResourceLinks;

    @Nested
    class Draft {

        @Test
        void shallIncludeEditCertificate() {
            doReturn(true).when(draftAccessServiceHelper).isAllowToEditUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.EDIT_CERTIFICATE);
        }

        @Test
        void shallExcludeEditCertificate() {
            doReturn(false).when(draftAccessServiceHelper).isAllowToEditUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.UNSIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.EDIT_CERTIFICATE);
        }

        @Test
        void shallIncludePrintCertificate() {
            doReturn(true).when(draftAccessServiceHelper).isAllowToPrintUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallExcludePrintCertificate() {
            doReturn(false).when(draftAccessServiceHelper).isAllowToPrintUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.UNSIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeDeleteCertificate() {
            doReturn(true).when(draftAccessServiceHelper).isAllowToDeleteUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.REMOVE_CERTIFICATE);
        }

        @Test
        void shallExcludeDeleteCertificate() {
            doReturn(false).when(draftAccessServiceHelper).isAllowToDeleteUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.UNSIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.REMOVE_CERTIFICATE);
        }

        @Test
        void shallIncludeSignCertificate() {
            doReturn(true).when(draftAccessServiceHelper).isAllowToSign(any(AccessEvaluationParameters.class), anyString());
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        }

        @Test
        void shallExcludeSignCertificate() {
            doReturn(false).when(draftAccessServiceHelper).isAllowToSign(any(AccessEvaluationParameters.class), anyString());
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.UNSIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        }

        @Test
        void shallIncludeForwardCertificate() {
            doReturn(true).when(draftAccessServiceHelper).isAllowedToForwardUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
        }

        @Test
        void shallExcludeForwardCertificate() {
            doReturn(false).when(draftAccessServiceHelper).isAllowedToForwardUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.UNSIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
        }
    }

    @Nested
    class LockedDraft {

        @Test
        void shallIncludePrintCertificate() {
            doReturn(true).when(lockedDraftAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.LOCKED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallExcludePrintCertificate() {
            doReturn(false).when(lockedDraftAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.LOCKED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeCopyCertificate() {
            doReturn(true).when(lockedDraftAccessServiceHelper).isAllowToCopy(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.LOCKED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.COPY_CERTIFICATE);
        }

        @Test
        void shallExcludeCopyCertificate() {
            doReturn(false).when(lockedDraftAccessServiceHelper).isAllowToCopy(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.LOCKED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.COPY_CERTIFICATE);
        }

        @Test
        void shallIncludeInvalidateCertificate() {
            doReturn(true).when(lockedDraftAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.LOCKED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }

        @Test
        void shallExcludeInvalidateCertificate() {
            doReturn(false).when(lockedDraftAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.LOCKED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }
    }

    @Nested
    class Certificates {

        @Test
        void shallIncludePrintCertificate() {
            doReturn(true).when(certificateAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallExcludePrintCertificate() {
            doReturn(false).when(certificateAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeReplaceCertificate() {
            doReturn(true).when(certificateAccessServiceHelper).isAllowToReplace(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
        }

        @Test
        void shallExcludeReplaceCertificate() {
            doReturn(false).when(certificateAccessServiceHelper).isAllowToReplace(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
        }

        @Test
        void shallIncludeRenewCertificate() {
            doReturn(true).when(certificateAccessServiceHelper).isAllowToRenew(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallExcludeRenewCertificate() {
            doReturn(false).when(certificateAccessServiceHelper).isAllowToRenew(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallIncludeInvalidateCertificate() {
            doReturn(true).when(certificateAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }

        @Test
        void shallExcludeInvalidateCertificate() {
            doReturn(false).when(certificateAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks.get(createCertificate(CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }
    }

    private void assertInclude(ResourceLinkDTO[] actualResourceLinks, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(actualResourceLinks, type);
        assertNotNull(actualResourceLink, () -> String.format("Expected resource link with type '%s'", type));
    }

    private void assertExclude(ResourceLinkDTO[] actualResourceLinks, ResourceLinkTypeDTO type) {
        final var actualResourceLink = get(actualResourceLinks, type);
        assertNull(actualResourceLink, () -> String.format("Don't expect resource link with type '%s'", type));
    }

    private ResourceLinkDTO get(ResourceLinkDTO[] resourceLinks, ResourceLinkTypeDTO type) {
        return Stream.of(resourceLinks)
            .filter(resourceLinkDTO -> resourceLinkDTO.getType().equals(type))
            .findFirst()
            .orElse(null);
    }

    private Certificate createCertificate(CertificateStatus status) {
        return CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id("certificateId")
                    .type("certificateType")
                    .typeVersion("certificateTypeVersion")
                    .status(status)
                    .patient(
                        Patient.builder()
                            .personId(
                                PersonId.builder()
                                    .id("191212121212")
                                    .build()
                            )
                            .build()
                    )
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
                    .careProvider(
                        Unit.builder()
                            .unitId("careProviderId")
                            .unitName("careProviderName")
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