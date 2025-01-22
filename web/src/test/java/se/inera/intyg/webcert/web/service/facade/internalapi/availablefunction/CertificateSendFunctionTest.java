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

package se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRecipient;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;

@ExtendWith(MockitoExtension.class)
class CertificateSendFunctionTest {

    private static final String TYPE = "type";

    @Mock
    private AuthoritiesHelper authoritiesHelper;

    @InjectMocks
    private CertificateSendFunction certificateSendFunction;

    @Nested
    class TestFeatureSend {

        @Test
        void shouldReturnFunctionIfFeatureIsActive() {
            doReturn(true)
                .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_SKICKA_INTYG, TYPE);

            final var response = certificateSendFunction.get(getCertificate());

            assertEquals(1, response.size());
            assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, response.get(0).getType());
        }

        @Test
        void shouldReturnEmptyIfFeatureIsInactive() {
            final var response = certificateSendFunction.get(getCertificate());

            assertEquals(Collections.emptyList(), response);
        }
    }

    @Nested
    class TestFeatureInactiveOlderMajorVersions {

        @BeforeEach
        void setup() {
            doReturn(true)
                .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_SKICKA_INTYG, TYPE);
        }

        @Nested
        class FeatureInactive {

            @Test
            void shouldReturnFunctionIfLatestVersion() {
                final var response = certificateSendFunction.get(getCertificateWithLatestMajorVersion(true));

                assertEquals(1, response.size());
                assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, response.get(0).getType());
            }

            @Test
            void shouldReturnFunctionIfOlderVersion() {
                final var response = certificateSendFunction.get(getCertificateWithLatestMajorVersion(false));

                assertEquals(1, response.size());
                assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, response.get(0).getType());
            }
        }

        @Nested
        class FeatureActive {

            @Test
            void shouldReturnFunctionIfLatestVersion() {
                final var response = certificateSendFunction.get(getCertificateWithLatestMajorVersion(true));

                assertEquals(1, response.size());
                assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, response.get(0).getType());
            }

            @Test
            void shouldReturnEmptyIfOlderVersion() {
                doReturn(true)
                    .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_INACTIVATE_PREVIOUS_MAJOR_VERSION, TYPE);

                final var response = certificateSendFunction.get(getCertificateWithLatestMajorVersion(false));

                assertEquals(Collections.emptyList(), response);
            }
        }
    }

    @Nested
    class TestCertificateSent {

        @BeforeEach
        void setup() {
            doReturn(true)
                .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_SKICKA_INTYG, TYPE);
        }

        @Test
        void shouldReturnDisabledFunctionIfSent() {
            final var response = certificateSendFunction.get(getCertificateWithSent(true));

            assertFalse(response.get(0).isEnabled());
            assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, response.get(0).getType());
        }

        @Test
        void shouldReturnFunctionEnabledIfNotSent() {
            final var response = certificateSendFunction.get(getCertificateWithSent(false));

            assertTrue(response.get(0).isEnabled());
            assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, response.get(0).getType());
        }
    }

    @Nested
    class TestCertificateRecipient {

        @BeforeEach
        void setup() {
            doReturn(true)
                .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_SKICKA_INTYG, TYPE);
        }

        @Test
        void shouldReturnEmptyIfNoRecipient() {
            final var response = certificateSendFunction.get(getCertificateWithRecipient(false));

            assertEquals(Collections.emptyList(), response);
        }

        @Test
        void shouldReturnFunctionIfRecipient() {
            final var response = certificateSendFunction.get(getCertificateWithRecipient(true));

            assertEquals(1, response.size());
            assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, response.get(0).getType());
        }
    }

    @Nested
    class TestChildRelation {

        @BeforeEach
        void setup() {
            doReturn(true)
                .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_SKICKA_INTYG, TYPE);
        }

        @Test
        void shouldReturnEmptyIfReplaced() {
            final var response = certificateSendFunction.get(getCertificateWithRelation(CertificateRelationType.REPLACED));

            assertEquals(Collections.emptyList(), response);
        }

        @Test
        void shouldReturnEmptyIfComplemented() {
            final var response = certificateSendFunction.get(getCertificateWithRelation(CertificateRelationType.COMPLEMENTED));

            assertEquals(Collections.emptyList(), response);
        }

        @Test
        void shouldReturnFunctionIfCopied() {
            final var response = certificateSendFunction.get(getCertificateWithRelation(CertificateRelationType.COPIED));

            assertEquals(1, response.size());
            assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, response.get(0).getType());
        }
    }

    private static Certificate getCertificate() {
        return getCertificate(true, false, true, null);
    }

    private static Certificate getCertificateWithLatestMajorVersion(boolean isLatestMajorVersion) {
        return getCertificate(isLatestMajorVersion, false, true, null);
    }

    private static Certificate getCertificateWithSent(boolean sent) {
        return getCertificate(true, sent, true, null);
    }

    private static Certificate getCertificateWithRecipient(boolean hasRecipient) {
        return getCertificate(true, false, hasRecipient, null);
    }

    private static Certificate getCertificateWithRelation(CertificateRelationType relationType) {
        return getCertificate(true, false, true, relationType);
    }

    private static Certificate getCertificate(boolean isLatestMajorVersion,
        boolean sent,
        boolean hasRecipient,
        CertificateRelationType relationType) {
        CertificateRelation[] relationChildren = {
            CertificateRelation.builder()
                .type(relationType)
                .status(CertificateStatus.SIGNED)
                .build()
        };

        final var certificateMetadata = CertificateMetadata.builder()
            .type(TYPE)
            .latestMajorVersion(isLatestMajorVersion)
            .sent(sent)
            .recipient(
                hasRecipient ? CertificateRecipient.builder()
                    .id("ID")
                    .name("NAME")
                    .build() : null
            )
            .relations(relationType != null ? CertificateRelations.builder().children(relationChildren).build() : null
            )
            .build();

        final var certificate = new Certificate();
        certificate.setMetadata(certificateMetadata);

        return certificate;
    }
}
