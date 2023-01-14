/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.service.facade.ResourceLinkFacadeTestHelper.assertExclude;
import static se.inera.intyg.webcert.web.service.facade.ResourceLinkFacadeTestHelper.assertInclude;
import static se.inera.intyg.webcert.web.service.facade.ResourceLinkFacadeTestHelper.get;
import static se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.CertificateRenewFunction.EVENTUAL_COMPLEMENTARY_REQUEST_WONT_BE_MARKED_READY;
import static se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.CertificateRenewFunction.EVENTUAL_COMPLEMENTARY_WILL_BE_MARKED_READY;
import static se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.GetCertificatesAvailableFunctionsImpl.REPLACE_DESCRIPTION;
import static se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.GetCertificatesAvailableFunctionsImpl.REPLACE_DESCRIPTION_DISABLED;

import java.time.LocalDateTime;
import java.util.Collections;
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
import se.inera.intyg.common.db.support.DbModuleEntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.common.support.facade.model.question.QuestionType;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.CertificateFacadeTestHelper;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.CertificateSignConfirmationFunction;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.CopyCertificateFunction;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.CreateCertificateFromCandidateFunction;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.CreateCertificateFromTemplateFunction;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.DisplayPatientAddressInCertificate;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.GetCertificatesAvailableFunctionsImpl;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.SendCertificateFunction;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.ShowRelatedCertificateFunction;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.service.facade.user.UserServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificatesAvailableFunctionsImplTest {

    @Mock
    AuthoritiesHelper authoritiesHelper;

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    UserServiceImpl userService;

    @Mock
    GetQuestionsFacadeService getQuestionsFacadeService;

    @Mock
    CertificateSignConfirmationFunction certificateSignConfirmationFunction;

    @Mock
    DisplayPatientAddressInCertificate displayPatientAddressInCertificate;

    @Mock
    SendCertificateFunction sendCertificateFunction;

    @Mock
    private CreateCertificateFromTemplateFunction createCertificateFromTemplateFunction;

    @Mock
    private ShowRelatedCertificateFunction showRelatedCertificateFunction;

    @Mock
    private CreateCertificateFromCandidateFunction createCertificateFromCandidateFunction;

    @Mock
    private CopyCertificateFunction copyCertificateFunction;

    @InjectMocks
    private GetCertificatesAvailableFunctionsImpl getCertificatesAvailableFunctions;

    private final WebCertUser user = mock(WebCertUser.class);

    @Nested
    class Draft {

        @BeforeEach
        void setup() {
            user.setOrigin("NORMAL");
            doReturn(user).when(webCertUserService).getUser();
        }

        @Test
        void shallIncludeEditCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.EDIT_CERTIFICATE);
        }

        @Test
        void shallIncludePrintCertificate() {
            final var certificate = getCertificateWithProtectedPatient(false, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            assertTrue(actualAvailableFunctions.stream().anyMatch(link -> link.getType()
                == ResourceLinkTypeDTO.PRINT_CERTIFICATE && link.getBody() == null));
        }

        @Test
        void shallIncludeBodyForPrintCertificateIfProtectedPerson() {
            final var certificate = getCertificateWithProtectedPatient(true, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            assertTrue(actualAvailableFunctions.stream().anyMatch(link -> link.getType()
                == ResourceLinkTypeDTO.PRINT_CERTIFICATE && link.getBody().length() > 0));
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
        void shallIncludeSignAndSendCertificateWithDescriptionSkatteverket() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, DbModuleEntryPoint.MODULE_ID))
                .thenReturn(true);
            when(webCertUserService.getUser()).thenReturn(getUserWithOrigin("DJUPINTEGRATION"));
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(
                actualAvailableFunctions.stream().anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Skatteverket.")));
        }

        @Test
        void shallIncludeSignAndSendCertificateWithDescriptionArbetsformedlingen() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, Af00213EntryPoint.MODULE_ID))
                .thenReturn(true);
            final var certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertTrue(
                actualAvailableFunctions.stream()
                    .anyMatch(r -> r.getDescription().contains("Intyget skickas direkt till Arbetsförmedlingen.")));
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
        void shallNotIncludeSignAndSendCertificateForTestIndicatedPatient() {
            when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_SIGNERA_SKICKA_DIREKT, Af00213EntryPoint.MODULE_ID))
                .thenReturn(true);
            final var certificate = CertificateFacadeTestHelper.createCertificate(Af00213EntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            certificate.getMetadata().setPatient(Patient.builder().testIndicated(true).build());
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SIGN_CERTIFICATE);
            assertFalse(actualAvailableFunctions.stream().anyMatch(r -> r.getName().contains("Signera och skicka")));
        }

        @Test
        void shallIncludeForwardCertificateIfUserIsNotDoctor() {
            doReturn(false).when(user).isLakare();
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
        }

        @Test
        void shallExcludeForwardCertificateIfUserIsDoctor() {
            doReturn(true).when(user).isLakare();
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
        }

        @Test
        void shallExcludeForwardCertificateIfUserIsPrivateDoctor() {
            doReturn(true).when(user).isPrivatLakare();
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.FORWARD_CERTIFICATE);
        }

        @Test
        void shallIncludeReadyForSigning() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.READY_FOR_SIGN);
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

        @Test
        void shallIncludeSignConfirmation() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            when(certificateSignConfirmationFunction.get(certificate, user)).thenReturn(Optional.of(ResourceLinkDTO.create(
                ResourceLinkTypeDTO.SIGN_CERTIFICATE_CONFIRMATION, "", "", "", true)));
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SIGN_CERTIFICATE_CONFIRMATION);
        }

        @Test
        void shallExcludeSignConfirmation() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            when(certificateSignConfirmationFunction.get(certificate, user)).thenReturn(Optional.empty());
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.SIGN_CERTIFICATE_CONFIRMATION);
        }

        @Test
        void shallIncludeDisplayPatientAddress() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            doReturn(
                Optional.of(
                    ResourceLinkDTO.create(ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE, "", "", "", true)
                ))
                .when(displayPatientAddressInCertificate).get(certificate);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE);
        }

        @Test
        void shallExcludeDisplayPatientAddress() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE);
        }

        @Test
        void shallIncludeWarningIntegrated() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            when(webCertUserService.getUser()).thenReturn(getUserWithOrigin("DJUPINTEGRATION"));
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.WARNING_DODSBEVIS_INTEGRATED);
        }

        @Test
        void shallExcludeWarningIntegrated() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            when(webCertUserService.getUser()).thenReturn(getUserWithOrigin("ORIGIN"));
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.WARNING_DODSBEVIS_INTEGRATED);
        }

        @Test
        void shallIncludeCreateCertificateFromCandidate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            doReturn(
                Optional.of(
                    ResourceLinkDTO.create(ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE, "", "", "", true)
                ))
                .when(createCertificateFromCandidateFunction).get(certificate);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE);
        }

        @Test
        void shallExcludeCreateCertificateFromCandidate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.UNSIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_CANDIDATE);
        }
    }

    private Certificate getCertificateWithProtectedPatient(boolean isProtectedPerson, CertificateStatus certificateStatus) {
        final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, certificateStatus);
        var metadata = certificate.getMetadata();
        var patient = Patient.builder()
            .protectedPerson(isProtectedPerson)
            .build();
        metadata.setPatient(patient);
        return certificate;
    }

    @Nested
    class LockedDraft {

        @BeforeEach
        void setup() {
            doReturn(user).when(webCertUserService).getUser();
        }

        @Test
        void shallIncludePrintCertificate() {
            final var certificate = getCertificateWithProtectedPatient(false, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            assertTrue(actualAvailableFunctions.stream().anyMatch(link -> link.getType()
                == ResourceLinkTypeDTO.PRINT_CERTIFICATE && link.getBody() == null));
        }

        @Test
        void shallIncludeBodyForPrintCertificateIfProtectedPerson() {
            final var certificate = getCertificateWithProtectedPatient(true, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            assertTrue(actualAvailableFunctions.stream().anyMatch(link -> link.getType()
                == ResourceLinkTypeDTO.PRINT_CERTIFICATE && link.getBody().length() > 0));
        }

        @Test
        void shallIncludeInvalidateCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REVOKE_CERTIFICATE);
        }

        @Test
        void shallExcludeSend() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }

        @Test
        void shallIncludeDisplayPatientAddress() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            doReturn(
                Optional.of(
                    ResourceLinkDTO.create(ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE, "", "", "", true)
                ))
                .when(displayPatientAddressInCertificate).get(certificate);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE);
        }

        @Test
        void shallExcludeDisplayPatientAddress() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE);
        }

        @Test
        void shallIncludeCopyCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            when(copyCertificateFunction.get(certificate)).thenReturn(Optional.of(ResourceLinkDTO.create(
                ResourceLinkTypeDTO.COPY_CERTIFICATE, "", "", "", true)));
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.COPY_CERTIFICATE);
        }

        @Test
        void shallExcludeCopyCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.LOCKED);
            when(copyCertificateFunction.get(certificate)).thenReturn(Optional.empty());
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.COPY_CERTIFICATE);
        }
    }

    @Nested
    class Certificates {

        @BeforeEach
        void setup() {
            doReturn(user).when(webCertUserService).getUser();
            doReturn("DJUPINTEGRATION").when(user).getOrigin();
        }

        @Test
        void shallIncludePrintCertificate() {
            final var certificate = getCertificateWithProtectedPatient(false, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            assertTrue(actualAvailableFunctions.stream().anyMatch(link -> link.getType()
                == ResourceLinkTypeDTO.PRINT_CERTIFICATE && link.getBody() == null));
        }

        @Test
        void shallIncludeBodyForPrintCertificateIfProtectedPerson() {
            final var certificate = getCertificateWithProtectedPatient(true, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.PRINT_CERTIFICATE);
            assertTrue(actualAvailableFunctions.stream().anyMatch(link -> link.getType()
                == ResourceLinkTypeDTO.PRINT_CERTIFICATE && link.getBody().length() > 0));
        }

        @Test
        void shallExcludeForwardQuestionIfUserIsPrivateDoctor() {
            doReturn(true).when(user).isPrivatLakare();
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.FORWARD_QUESTION);
        }

        @Test
        void shallExcludeForwardQuestionIfNoUnhandledComplementOrQuestions() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.FORWARD_QUESTION);
        }

        @Test
        void shallIncludeForwardQuestionIfUnhandledComplement() {
            when(getQuestionsFacadeService.getQuestions(any())).thenReturn(
                List.of(Question.builder().type(QuestionType.COMPLEMENT).isHandled(false).build()));
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.FORWARD_QUESTION);
        }

        @Test
        void shallIncludeForwardQuestionIfUnhandledQuestion() {
            when(getQuestionsFacadeService.getQuestions(any())).thenReturn(
                List.of(Question.builder().type(QuestionType.COORDINATION).isHandled(false).build()));
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.FORWARD_QUESTION);
        }

        @Test
        void shallIncludeDisplayPatientAddress() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            doReturn(
                Optional.of(
                    ResourceLinkDTO.create(ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE, "", "", "", true)
                ))
                .when(displayPatientAddressInCertificate).get(certificate);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE);
        }

        @Test
        void shallExcludeDisplayPatientAddress() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE);
        }

        @Test
        void shallIncludeCreateCertificateFromTemplate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            doReturn(
                Optional.of(
                    ResourceLinkDTO.create(ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_TEMPLATE, "", "", "", true)
                ))
                .when(createCertificateFromTemplateFunction).get(eq(certificate), any());
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_TEMPLATE);
        }

        @Test
        void shallExcludeCreateCertificateFromTemplate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_CERTIFICATE_FROM_TEMPLATE);
        }

        @Test
        void shallIncludeShowRelatedCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            doReturn(
                Optional.of(
                    ResourceLinkDTO.create(ResourceLinkTypeDTO.SHOW_RELATED_CERTIFICATE, "", "", "", true)
                ))
                .when(showRelatedCertificateFunction).get(eq(certificate), any());
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SHOW_RELATED_CERTIFICATE);
        }

        @Test
        void shallExcludeShowRelatedCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.SHOW_RELATED_CERTIFICATE);
        }

        @Test
        void shallIncludeSendCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            doReturn(
                Optional.of(
                    ResourceLinkDTO.create(ResourceLinkTypeDTO.SEND_CERTIFICATE, "", "", "", true)
                ))
                .when(sendCertificateFunction).get(eq(certificate));
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.SEND_CERTIFICATE);
        }
    }

    @Nested
    class CertificatesRevoked {

        @Test
        void shallIncludeDisplayPatientAddress() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(DbModuleEntryPoint.MODULE_ID, CertificateStatus.REVOKED);
            doReturn(
                Optional.of(
                    ResourceLinkDTO.create(ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE, "", "", "", true)
                ))
                .when(displayPatientAddressInCertificate).get(certificate);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE);
        }

        @Test
        void shallExcludeDisplayPatientAddress() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.REVOKED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.DISPLAY_PATIENT_ADDRESS_IN_CERTIFICATE);
        }
    }

    @Nested
    class RenewCertificates {

        void setupRenewData(String unitId) {
            final var webcertUser = mock(WebCertUser.class);
            final var careUnit = mock(Vardenhet.class);
            doReturn(webcertUser).when(webCertUserService).getUser();
            doReturn(careUnit).when(userService).getLoggedInCareUnit(any());
            doReturn(unitId).when(careUnit).getId();
        }

        @Test
        void shallIncludeRenewCertificate() {
            setupRenewData("unitId");
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, LisjpEntryPoint.MODULE_ID);

            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallIncludeComplementWontBeMarkedReadyTextIfDifferentUnit() {
            setupRenewData("non matching id");
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, LisjpEntryPoint.MODULE_ID);

            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            final var renewFunction = get(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
            assertTrue(renewFunction.getBody().contains(EVENTUAL_COMPLEMENTARY_REQUEST_WONT_BE_MARKED_READY));
        }

        @Test
        void shallIncludeComplementWillBeMarkedReadyTextIfSameUnit() {
            setupRenewData("unitId");
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, LisjpEntryPoint.MODULE_ID);

            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            final var renewFunction = get(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
            assertTrue(renewFunction.getBody().contains(EVENTUAL_COMPLEMENTARY_WILL_BE_MARKED_READY));
        }

        @Test
        void shallIncludeCorrectBodyIfLisjpCertificate() {
            setupRenewData("unitId");
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, LisjpEntryPoint.MODULE_ID);

            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            final var renewFunction = get(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
            assertTrue(renewFunction.getBody().contains("Valet om man vill ha kontakt med Försäkringskassan."));
        }

        @Test
        void shallIncludeCorrectBodyIfAg7804Certificate() {
            setupRenewData("unitId");
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, Ag7804EntryPoint.MODULE_ID);

            final var certificate = CertificateFacadeTestHelper.createCertificate(Ag7804EntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            final var renewFunction = get(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
            assertTrue(renewFunction.getBody().contains("Valet om diagnos ska förmedlas till arbetsgivaren"));
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

            setupRenewData("unitId");

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

        @Test
        void shallExcludeRenewIfComplementedBySignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.SIGNED)
                .type(CertificateRelationType.COMPLEMENTED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }

        @Test
        void shallIncludeRenewIfComplementedByUnsignedCertificate() {
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_FORNYA_INTYG, LisjpEntryPoint.MODULE_ID);

            setupRenewData("id");

            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.UNSIGNED)
                .type(CertificateRelationType.COMPLEMENTED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.RENEW_CERTIFICATE);
        }
    }

    @Nested
    class ReplaceCertificates {

        @Test
        void shallIncludeReplaceCertificateEnabledIfNotComplements() {
            when(getQuestionsFacadeService.getQuestions(any())).thenReturn(Collections.emptyList());
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            final var replaceFunction = actualAvailableFunctions.stream()
                .filter(fnc -> fnc.getType().equals(ResourceLinkTypeDTO.REPLACE_CERTIFICATE))
                .findFirst().orElseThrow();
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
            assertTrue(replaceFunction.isEnabled());
            assertEquals(REPLACE_DESCRIPTION, replaceFunction.getDescription());
        }

        @Test
        void shallIncludeReplaceCertificateDisabledIfUnhandledComplement() {
            when(getQuestionsFacadeService.getQuestions(any())).thenReturn(
                List.of(Question.builder().type(QuestionType.COMPLEMENT).isHandled(false).build()));
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            final var replaceFunction = actualAvailableFunctions.stream()
                .filter(fnc -> fnc.getType().equals(ResourceLinkTypeDTO.REPLACE_CERTIFICATE))
                .findFirst().orElseThrow();
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
            assertFalse(replaceFunction.isEnabled());
            assertEquals(REPLACE_DESCRIPTION_DISABLED, replaceFunction.getDescription());
        }

        @Test
        void shallIncludeReplaceCertificateEnabledIfHandledComplement() {
            when(getQuestionsFacadeService.getQuestions(any())).thenReturn(
                List.of(Question.builder().type(QuestionType.COMPLEMENT).isHandled(true).build()));
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            final var replaceFunction = actualAvailableFunctions.stream()
                .filter(fnc -> fnc.getType().equals(ResourceLinkTypeDTO.REPLACE_CERTIFICATE))
                .findFirst().orElseThrow();
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
            assertTrue(replaceFunction.isEnabled());
            assertEquals(REPLACE_DESCRIPTION, replaceFunction.getDescription());
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

        @Test
        void shallIncludeReplaceIfComplementedByUnsignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.UNSIGNED)
                .type(CertificateRelationType.COMPLEMENTED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
        }

        @Test
        void shallExcludeReplaceIfComplementedBySignedCertificate() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.SIGNED)
                .type(CertificateRelationType.COMPLEMENTED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);
            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);
            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.REPLACE_CERTIFICATE);
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

        @Test
        void shallExcludeCreateQuestionsWhenUnsignedComplement() {
            final var relation = CertificateRelation.builder()
                .certificateId("xxxxx-yyyyy-zzzzz-uuuuu")
                .created(LocalDateTime.now())
                .status(CertificateStatus.UNSIGNED)
                .type(CertificateRelationType.COMPLEMENTED)
                .build();

            final var certificate = CertificateFacadeTestHelper
                .createCertificateWithChildRelation(LisjpEntryPoint.MODULE_ID, CertificateStatus.SIGNED, relation);

            certificate.getMetadata().setSent(true);
            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(AuthoritiesConstants.FEATURE_HANTERA_FRAGOR, LisjpEntryPoint.MODULE_ID);

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertExclude(actualAvailableFunctions, ResourceLinkTypeDTO.CREATE_QUESTIONS);
        }
    }

    @Nested
    class QuestionsForDraft {

        @BeforeEach
        void setup() {
            WebCertUser user = new WebCertUser();
            user.setOrigin("normal");
            doReturn(user).when(webCertUserService).getUser();
        }

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

    @Nested
    class QuestionsForRevoked {

        @Test
        void shallIncludeQuestionsWhenRevokedAndSentCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.REVOKED);
            certificate.getMetadata().setRelations(
                CertificateRelations.builder()
                    .parent(CertificateRelation.builder()
                        .type(CertificateRelationType.COMPLEMENTED)
                        .build())
                    .build()
            );
            certificate.getMetadata().setSent(true);

            doReturn(true)
                .when(authoritiesHelper)
                .isFeatureActive(anyString(), eq(LisjpEntryPoint.MODULE_ID));

            final var actualAvailableFunctions = getCertificatesAvailableFunctions.get(certificate);

            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.QUESTIONS);
        }

        @Test
        void shallIncludeQuestionsNotAvailableWhenRevokedAndNotSentCertificate() {
            final var certificate = CertificateFacadeTestHelper.createCertificate(LisjpEntryPoint.MODULE_ID, CertificateStatus.REVOKED);
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

            assertInclude(actualAvailableFunctions, ResourceLinkTypeDTO.QUESTIONS_NOT_AVAILABLE);
        }
    }

    private WebCertUser getUserWithOrigin(String origin) {
        WebCertUser user = new WebCertUser();
        user.setOrigin(origin);
        return user;
    }
}

