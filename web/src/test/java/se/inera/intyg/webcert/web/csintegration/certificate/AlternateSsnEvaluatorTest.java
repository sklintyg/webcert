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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.link.ResourceLink;
import se.inera.intyg.common.support.facade.model.link.ResourceLinkTypeEnum;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class AlternateSsnEvaluatorTest {

    private static final Patient PATIENT = Patient.builder()
        .personId(
            PersonId.builder()
                .id("191212121212")
                .build()
        )
        .build();
    private static final String VALID_PERSONAL_NUMER = "201212121212";
    @Mock
    private IntegrationParameters integrationParameters;
    @Mock
    private WebCertUser webCertUser;
    @Mock
    private Certificate certificate;
    @Mock
    private CertificateMetadata certificateMetadata;
    @InjectMocks
    private AlternateSsnEvaluator alternateSsnEvaluator;

    @BeforeEach
    void setUp() {
        doReturn(certificateMetadata).when(certificate).getMetadata();
    }

    @Test
    void shallReturnFalseIfCertificateIsNotDraft() {
        doReturn(CertificateStatus.SIGNED).when(certificateMetadata).getStatus();
        assertFalse(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
    }

    @Nested
    class CertificateIsDraftTests {

        @BeforeEach
        void setUp() {
            doReturn(CertificateStatus.UNSIGNED).when(certificateMetadata).getStatus();
        }

        @Test
        void shallReturnTrueWhenAlternateSsnIsProvidedAndDoesNotMatchPatientId() {
            doReturn(PATIENT).when(certificateMetadata).getPatient();
            doReturn(integrationParameters).when(webCertUser).getParameters();
            doReturn(VALID_PERSONAL_NUMER).when(integrationParameters).getAlternateSsn();
            doReturn(List.of(
                ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.EDIT_CERTIFICATE)
                    .enabled(true)
                    .build())
            ).when(certificate).getLinks();

            assertTrue(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
        }

        @Test
        void shallReturnTrueWhenAlternateSsnIsProvidedAndDoesNotMatchPatientIdWithoutDash() {
            doReturn(PATIENT).when(certificateMetadata).getPatient();
            doReturn(integrationParameters).when(webCertUser).getParameters();
            doReturn(VALID_PERSONAL_NUMER).when(integrationParameters).getAlternateSsn();
            doReturn(List.of(
                ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.EDIT_CERTIFICATE)
                    .enabled(true)
                    .build())
            ).when(certificate).getLinks();

            assertTrue(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
        }

        @Test
        void shouldReturnFalseWhenAlternateSsnIsProvidedAndMatchesPatientId() {
            doReturn(PATIENT).when(certificateMetadata).getPatient();
            doReturn(integrationParameters).when(webCertUser).getParameters();
            doReturn("191212121212").when(integrationParameters).getAlternateSsn();

            assertFalse(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
        }

        @Test
        void shouldReturnFalseWhenAlternateSsnIsNotProvided() {
            doReturn(integrationParameters).when(webCertUser).getParameters();
            doReturn(null).when(integrationParameters).getAlternateSsn();

            assertFalse(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
        }


        @Test
        void shouldReturnFalseWhenAlternateSsnIsEmpty() {
            doReturn(integrationParameters).when(webCertUser).getParameters();
            doReturn("").when(integrationParameters).getAlternateSsn();

            assertFalse(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
        }

        @Test
        void shouldReturnFalseWhenIntegrationParametersIsNull() {
            assertFalse(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
        }

        @Test
        void shallReturnFalseIfAlternateSsnIsInvalidFormat() {
            doReturn(integrationParameters).when(webCertUser).getParameters();
            doReturn("invalidPersonal").when(integrationParameters).getAlternateSsn();
            assertFalse(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
        }
    }

    @Nested
    class CertificateIsDraftAndAlternateSsnIsNotMatching {

        @BeforeEach
        void setUp() {
            doReturn(CertificateStatus.UNSIGNED).when(certificateMetadata).getStatus();
            doReturn(PATIENT).when(certificateMetadata).getPatient();
            doReturn(integrationParameters).when(webCertUser).getParameters();
            doReturn(VALID_PERSONAL_NUMER).when(integrationParameters).getAlternateSsn();
        }

        @Test
        void shallReturnTrueIfUserHasRightToEditAndEnabledIsTrue() {
            doReturn(List.of(
                ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.EDIT_CERTIFICATE)
                    .enabled(true)
                    .build())
            ).when(certificate).getLinks();
            assertTrue(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
        }

        @Test
        void shallReturnFalseIfUserHasRightToEditAndEnabledIsFalse() {
            doReturn(List.of(
                ResourceLink.builder()
                    .type(ResourceLinkTypeEnum.EDIT_CERTIFICATE)
                    .enabled(false)
                    .build())
            ).when(certificate).getLinks();
            assertFalse(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
        }

        @Test
        void shallReturnFalseIfUserDontHaveRightToEdit() {
            doReturn(List.of(ResourceLink.builder().type(ResourceLinkTypeEnum.SEND_CERTIFICATE).build())).when(certificate).getLinks();
            assertFalse(alternateSsnEvaluator.shouldUpdate(certificate, webCertUser));
        }
    }
}
