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
package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class SrsFunctionImplTest {

    @InjectMocks
    private SrsFunctionImpl srsFunction;

    @Nested
    class SrsFunctionTest {

        Certificate certificate;
        WebCertUser user = new WebCertUser();
        Map<String, Feature> features = new HashMap<>();

        @Nested
        class TestDraft {

            @BeforeEach
            void setup() {
                certificate = new Certificate();
                final var metadata = CertificateMetadata.builder()
                    .type("lisjp")
                    .status(CertificateStatus.UNSIGNED)
                    .build();
                certificate.setMetadata(metadata);
            }

            @Nested
            class SrsFullView {

                @Test
                void shouldReturnResourceLinkIfFeatureIsActivated() {
                    updateUserFeatures(getFeature(true, "lisjp"));
                    assertTrue(srsFunction.getSRSFullView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfFeatureIsInactive() {
                    updateUserFeatures(getFeature(false, "lisjp"));
                    assertFalse(srsFunction.getSRSFullView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfTypeIsWrong() {
                    updateUserFeatures(getFeature(true, "wrongType"));
                    assertFalse(srsFunction.getSRSFullView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfFeatureIsMissing() {
                    updateUserFeatures(null);
                    assertFalse(srsFunction.getSRSFullView(certificate, user).isPresent());
                }
            }

            @Nested
            class SrsMinimizedView {

                @Test
                void shouldNotReturnResourceLinkForDraft() {
                    updateUserFeatures(getFeature(true, "lisjp"));
                    assertFalse(srsFunction.getSRSMinimizedView(certificate, user).isPresent());
                }
            }
        }

        @Nested
        class TestNotDraft {

            void setup(CertificateStatus status) {
                certificate = new Certificate();
                final var metadata = CertificateMetadata.builder()
                    .type("lisjp")
                    .status(status)
                    .build();
                certificate.setMetadata(metadata);
            }

            @Nested
            class SrsFullView {

                @Test
                void shouldNotReturnResourceLinkIfSigned() {
                    setup(CertificateStatus.SIGNED);
                    updateUserFeatures(getFeature(true, "lisjp"));
                    assertFalse(srsFunction.getSRSFullView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfRevoked() {
                    setup(CertificateStatus.REVOKED);
                    updateUserFeatures(getFeature(true, "lisjp"));
                    assertFalse(srsFunction.getSRSFullView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfLocked() {
                    setup(CertificateStatus.LOCKED);
                    updateUserFeatures(getFeature(true, "lisjp"));
                    assertFalse(srsFunction.getSRSFullView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfLockedRevoked() {
                    setup(CertificateStatus.LOCKED_REVOKED);
                    updateUserFeatures(getFeature(true, "lisjp"));
                    assertFalse(srsFunction.getSRSFullView(certificate, user).isPresent());
                }
            }

            @Nested
            class SrsMinimizedView {

                @Test
                void shouldReturnResourceLinkIfSigned() {
                    setup(CertificateStatus.SIGNED);
                    updateUserFeatures(getFeature(true, "lisjp"));
                    assertTrue(srsFunction.getSRSMinimizedView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfRevoked() {
                    setup(CertificateStatus.REVOKED);
                    updateUserFeatures(getFeature(true, "lisjp"));
                    assertFalse(srsFunction.getSRSMinimizedView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfLocked() {
                    setup(CertificateStatus.LOCKED);
                    updateUserFeatures(getFeature(true, "lisjp"));
                    assertFalse(srsFunction.getSRSMinimizedView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfLockedRevoked() {
                    setup(CertificateStatus.LOCKED_REVOKED);
                    updateUserFeatures(getFeature(true, "lisjp"));
                    assertFalse(srsFunction.getSRSMinimizedView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfFeatureIsInactive() {
                    setup(CertificateStatus.SIGNED);
                    updateUserFeatures(getFeature(false, "lisjp"));
                    assertFalse(srsFunction.getSRSMinimizedView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfTypeIsWrong() {
                    setup(CertificateStatus.SIGNED);
                    updateUserFeatures(getFeature(true, "wrongType"));
                    assertFalse(srsFunction.getSRSMinimizedView(certificate, user).isPresent());
                }

                @Test
                void shouldNotReturnResourceLinkIfFeatureIsMissing() {
                    setup(CertificateStatus.SIGNED);
                    updateUserFeatures(null);
                    assertFalse(srsFunction.getSRSMinimizedView(certificate, user).isPresent());
                }
            }
        }

        private void updateUserFeatures(Feature feature) {
            if (feature != null) {
                features.put(feature.getName(), feature);
            }
            user.setFeatures(features);
        }

        private Feature getFeature(boolean global, String certificateType) {
            final var feature = new Feature();
            feature.setName(AuthoritiesConstants.FEATURE_SRS);
            feature.setIntygstyper(Collections.singletonList(certificateType));
            feature.setGlobal(global);
            return feature;
        }
    }
}
