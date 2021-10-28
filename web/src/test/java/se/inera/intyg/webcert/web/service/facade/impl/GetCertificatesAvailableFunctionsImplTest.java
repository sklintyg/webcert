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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.af00213.support.Af00213EntryPoint;
import se.inera.intyg.common.ag7804.support.Ag7804EntryPoint;
import se.inera.intyg.common.ag7804.v1.rest.Ag7804ModuleApiV1;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.model.common.internal.Patient;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.utkast.UtkastCandidateServiceImpl;
import se.inera.intyg.webcert.web.service.utkast.dto.UtkastCandidateMetaData;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificatesAvailableFunctionsImplTest {

    @Mock
    AuthoritiesHelper authoritiesHelper;

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    IntygModuleRegistry moduleRegistry;

    @Mock
    UtkastCandidateServiceImpl utkastCandidateService;

    @Mock
    PatientDetailsResolver patientDetailsResolver;

    @InjectMocks
    private GetCertificatesAvailableFunctionsImpl getCertificatesAvailableFunctions;

    @Nested
    class Draft {

        @Test
        void shallIncludeEditCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.EDIT_CERTIFICATE);
        }

        @Test
        void shallIncludePrintCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeDeleteCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REMOVE_CERTIFICATE);
        }

        @Test
        void shallIncludeSignCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertFalse(actualAvailableFunctions.stream().anyMatch(r -> r.getName().contains("Signera och skicka")));
        }

        @Test
        void shallIncludeSignAndSendCertificate() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, Af00213EntryPoint.MODULE_ID))
                .thenReturn(true);
            final var certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(actualAvailableFunctions.stream().anyMatch(r -> r.getName().contains("Signera och skicka")));
        }

        @Test
        void shallIncludeSignAndSendCertificateIfDraftIsComplementing() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, LisjpEntryPoint.MODULE_ID))
                .thenReturn(false);
            final var relation = CertificateRelation.builder()
                .type(CertificateRelationType.COMPLEMENTED)
                .build();
            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithParentRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED, relation);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(actualAvailableFunctions.stream().anyMatch(r -> r.getName().contains("Signera och skicka")));
        }

        @Test
        void shallIncludeForwardCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
        }

        @Test
        void shallIncludeFMB() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.FMB);
        }

        @Test
        void shallExcludeFMB() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.FMB);
        }

        @Test
        void shallExcludeSend() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }
    }

    @Nested
    class CreateCertificateFromCandidate {

        void setUpPatient() {
            final var patient = new Patient();
            patient.setPersonId(Personnummer.createPersonnummer("191212121212").get());
            doReturn(patient).when(patientDetailsResolver).resolvePatient(any(), any(), any());
        }

        void setUpModuleApi() throws ModuleNotFoundException {
            final var moduleApi = mock(Ag7804ModuleApiV1.class);
            doReturn(moduleApi).when(moduleRegistry).getModuleApi(anyString(), anyString());
        }

        void setUpMetadata() {
            when(utkastCandidateService
                .getCandidateMetaData(any(ModuleApi.class), anyString(), anyString(), any(Patient.class), anyBoolean()))
                .thenReturn(Optional.of(createCandidateMetaData("candidateId", "candidateType", "version")));
        }

        @Test
        void shallIncludeCreateCertificateFromCandidate() throws ModuleNotFoundException {
            setUpPatient();
            setUpModuleApi();
            setUpMetadata();
            final var certificate = CertificateFacadeTestHelper
                .createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE);
        }

        @Test
        void shallExcludeCreateCertificateFromCandidateIfNoCandidate() throws ModuleNotFoundException {
            setUpPatient();
            setUpModuleApi();
            final var certificate = CertificateFacadeTestHelper
                .createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE);
        }

        @Test
        void shallExcludeCreateCertificateFromCandidateIfNotVersion0() {
            final var certificate = CertificateFacadeTestHelper
                .createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var metadata = certificate.getMetadata();
            metadata.setVersion(1);
            certificate.setMetadata(metadata);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE);
        }

        @Test
        void shallExcludeCreateCertificateFromCandidateIfLisjp() {
            final var certificate = CertificateFacadeTestHelper
                .createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE);
        }
    }

    @Nested
    class LockedDraft {

        @Test
        void shallIncludePrintCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeCopyCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.COPY_CERTIFICATE);
        }

        @Test
        void shallIncludeInvalidateCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }

        @Test
        void shallExcludeSend() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }
    }

    @Nested
    class Certificates {

        @Test
        void shallIncludePrintCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
        }

        @Test
        void shallIncludeRevokeCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }
    }

    @Nested
    class RenewCertificates {

        @Test
        void shallIncludeRenewCertificate() {
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, LisjpEntryPoint.MODULE_ID);

            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallExcludeRenewCertificate() {
            doReturn(false)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, Af00213EntryPoint.MODULE_ID);

            final var certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallExcludeRenewIfReplacedBySignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.SIGNED)
                .type(CertificateRelationType.REPLACED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallIncludeRenewIfReplacedByUnsignedCertificate() {
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, LisjpEntryPoint.MODULE_ID);

            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.UNSIGNED)
                .type(CertificateRelationType.REPLACED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }
    }

    @Nested
    class SendCertificates {

        @Test
        void shallIncludeSend() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }

        @Test
        void shallExcludeSendIfAlreadySent() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            certificate.getMetadata().setSent(true);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }

        @Test
        void shallExcludeSendIfReplacedBySignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.SIGNED)
                .type(CertificateRelationType.REPLACED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }

        @Test
        void shallIncludeSendIfReplacedByUnsignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.UNSIGNED)
                .type(CertificateRelationType.REPLACED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }
    }

    @Nested
    class ReplaceCertificates {

        @Test
        void shallIncludeReplaceCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
        }

        @Test
        void shallExcludeReplaceCertificateIfAlreadyReplacedBySignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.SIGNED)
                .type(CertificateRelationType.REPLACED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
        }

        @Test
        void shallExcludeReplaceCertificateIfAlreadyReplacedByUnsignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.UNSIGNED)
                .type(CertificateRelationType.REPLACED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
        }

        @Test
        void shallIncludeContinueReplaceCertificateIfAlreadyReplacedByUnsignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.UNSIGNED)
                .type(CertificateRelationType.REPLACED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REPLACE_CERTIFICATE_CONTINUE);
        }
    }

    @Nested
    class Questions {

        @BeforeEach
        void handleMockingIssue() {
            doReturn(false)
                .when(authoritiesHelper)
                .isFeatureActive(eq(AuthoritiesConstants.FEATURE_FORNYA_INTYG), anyString());
        }

        @Test
        void shallIncludeQuestionsWhenCertificateIsSent() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            certificate.getMetadata().setSent(true);
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, LisjpEntryPoint.MODULE_ID);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.QUESTIONS);
        }

        @Test
        void shallExcludeQuestionsWhenCertificateIsNotSent() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.QUESTIONS);
        }

        @Test
        void shallExcludeQuestionsWhenCertificateDoesntSupportQuestions() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            certificate.getMetadata().setSent(true);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.QUESTIONS);
        }

        @Test
        void shallIncludeQuestionsNotAvailableWhenCertificateIsNotSent() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, LisjpEntryPoint.MODULE_ID);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE);
        }

        @Test
        void shallExcludeQuestionsNotAvailableWhenCertificateIsSent() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            certificate.getMetadata().setSent(true);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE);
        }

        @Test
        void shallExcludeQuestionsNotAvailableWhenCertificateDoesntSupportQuestions() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.SIGNED);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE);
        }

        @Test
        void shallIncludeCreateQuestionsWhenCertificateIsSent() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            certificate.getMetadata().setSent(true);
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, LisjpEntryPoint.MODULE_ID);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_QUESTIONS);
        }

        @Test
        void shallExcludeCreateQuestionsWhenCertificateIsNotSent() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_QUESTIONS);
        }

        @Test
        void shallExcludeCreateQuestionsWhenCertificateDoesntSupportQuestions() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            certificate.getMetadata().setSent(true);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_QUESTIONS);
        }
    }

    @Nested
    class QuestionsForDraft {

        @Test
        void shallIncludeQuestionsWhenComplementDraft() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            certificate.getMetadata().setRelations(
                CertificateRelations.builder()
                    .parent(CertificateRelation.builder()
                        .type(CertificateRelationType.COMPLEMENTED)
                        .build())
                    .build()
            );

            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(anyString(), eq(LisjpEntryPoint.MODULE_ID));

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.QUESTIONS);
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

    private UtkastCandidateMetaData createCandidateMetaData(String intygId, String intygType, String intygTypeVersion) {
        return new UtkastCandidateMetaData.Builder()
            .with(builder -> {
                builder.intygId = intygId;
                builder.intygType = intygType;
                builder.intygTypeVersion = intygTypeVersion;
                builder.intygCreated = LocalDateTime.now();
            })
            .create();
    }
}

