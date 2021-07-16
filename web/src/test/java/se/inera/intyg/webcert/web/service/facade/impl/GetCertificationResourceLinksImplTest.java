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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper.createCertificate;

import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.webcert.web.service.access.AccessEvaluationParameters;
import se.inera.intyg.webcert.web.service.access.CertificateAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.DraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.access.LockedDraftAccessServiceHelper;
import se.inera.intyg.webcert.web.service.facade.GetCertificatesAvailableFunctions;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificationResourceLinksImplTest {

    @Mock
    private GetCertificatesAvailableFunctions certificatesAvailableFunctions;

    @Mock
    private DraftAccessServiceHelper draftAccessServiceHelper;

    @Mock
    private LockedDraftAccessServiceHelper lockedDraftAccessServiceHelper;

    @Mock
    private CertificateAccessServiceHelper certificateAccessServiceHelper;

    @InjectMocks
    private GetCertificationResourceLinksImpl getCertificationResourceLinks;

    private ResourceLinkDTO resourceLinkDTO;

    @BeforeEach
    void setUp() {
        resourceLinkDTO = new ResourceLinkDTO();

        doReturn(Collections.singletonList(resourceLinkDTO))
            .when(certificatesAvailableFunctions)
            .get(any(Certificate.class));
    }

    @Nested
    class Draft {

        @Test
        void shallIncludeEditCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.EDIT_CERTIFICATE);
            doReturn(true).when(draftAccessServiceHelper).isAllowToEditUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.EDIT_CERTIFICATE);
        }

        @Test
        void shallExcludeEditCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.EDIT_CERTIFICATE);
            doReturn(false).when(draftAccessServiceHelper).isAllowToEditUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.EDIT_CERTIFICATE);
        }

        @Test
        void shallIncludePrintCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            doReturn(true).when(draftAccessServiceHelper).isAllowToPrintUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallExcludePrintCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            doReturn(false).when(draftAccessServiceHelper).isAllowToPrintUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeDeleteCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.REMOVE_CERTIFICATE);
            doReturn(true).when(draftAccessServiceHelper).isAllowToDeleteUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.REMOVE_CERTIFICATE);
        }

        @Test
        void shallExcludeDeleteCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.REMOVE_CERTIFICATE);
            doReturn(false).when(draftAccessServiceHelper).isAllowToDeleteUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.REMOVE_CERTIFICATE);
        }

        @Test
        void shallIncludeSignCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            doReturn(true).when(draftAccessServiceHelper).isAllowToSign(any(AccessEvaluationParameters.class), anyString());
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        }

        @Test
        void shallExcludeSignCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            doReturn(false).when(draftAccessServiceHelper).isAllowToSign(any(AccessEvaluationParameters.class), anyString());
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
        }

        @Test
        void shallIncludeForwardCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
            doReturn(true).when(draftAccessServiceHelper).isAllowedToForwardUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
        }

        @Test
        void shallExcludeForwardCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
            doReturn(false).when(draftAccessServiceHelper).isAllowedToForwardUtkast(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
        }


        @Test
        void shallIncludeFMB() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.FMB);
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.FMB);
        }
    }

    @Nested
    class LockedDraft {

        @Test
        void shallIncludePrintCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            doReturn(true).when(lockedDraftAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallExcludePrintCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            doReturn(false).when(lockedDraftAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeCopyCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.COPY_CERTIFICATE);
            doReturn(true).when(lockedDraftAccessServiceHelper).isAllowToCopy(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.COPY_CERTIFICATE);
        }

        @Test
        void shallExcludeCopyCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.COPY_CERTIFICATE);
            doReturn(false).when(lockedDraftAccessServiceHelper).isAllowToCopy(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.COPY_CERTIFICATE);
        }

        @Test
        void shallIncludeInvalidateCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
            doReturn(true).when(lockedDraftAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }

        @Test
        void shallExcludeInvalidateCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
            doReturn(false).when(lockedDraftAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }
    }

    @Nested
    class Certificates {

        @Test
        void shallIncludePrintCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallExcludePrintCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToPrint(any(AccessEvaluationParameters.class), anyBoolean());
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeReplaceCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToReplace(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
        }

        @Test
        void shallExcludeReplaceCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToReplace(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
        }

        @Test
        void shallIncludeReplaceCertificateContinue() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.REPLACE_CERTIFICATE_CONTINUE);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToReplace(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.REPLACE_CERTIFICATE_CONTINUE);
        }

        @Test
        void shallExcludeReplaceCertificateContinue() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.REPLACE_CERTIFICATE_CONTINUE);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToReplace(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.REPLACE_CERTIFICATE_CONTINUE);
        }

        @Test
        void shallIncludeRenewCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.RENEW_CERTIFICATE);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToRenew(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallExcludeRenewCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.RENEW_CERTIFICATE);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToRenew(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallIncludeInvalidateCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }

        @Test
        void shallExcludeInvalidateCertificate() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToInvalidate(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }

        @Test
        void shallIncludeQuestions() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.QUESTIONS);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToReadQuestions(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.QUESTIONS);
        }

        @Test
        void shallExcludeQuestions() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.QUESTIONS);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToReadQuestions(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.QUESTIONS);
        }

        @Test
        void shallIncludeQuestionsNotAvailable() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToReadQuestions(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE);
        }

        @Test
        void shallExcludeQuestionsNotAvailable() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToReadQuestions(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE);
        }

        @Test
        void shallIncludeCreateQuestions() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.CREATE_QUESTIONS);
            doReturn(true).when(certificateAccessServiceHelper).isAllowToCreateQuestion(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertInclude(actualResourceLinks, ResourceLinkTypeDTO.CREATE_QUESTIONS);
        }

        @Test
        void shallExcludeCreateQuestions() {
            resourceLinkDTO.setType(ResourceLinkTypeDTO.CREATE_QUESTIONS);
            doReturn(false).when(certificateAccessServiceHelper).isAllowToCreateQuestion(any(AccessEvaluationParameters.class));
            final var actualResourceLinks = getCertificationResourceLinks
                .get(createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED));
            assertExclude(actualResourceLinks, ResourceLinkTypeDTO.CREATE_QUESTIONS);
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
}