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

package se.inera.intyg.webcert.web.service.facade.internalapi.availablefunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.AvailableFunctionTypeDTO;

@ExtendWith(MockitoExtension.class)
class CertificateSendFunctionTest {

    public static final String TYPE = "type";
    private static Certificate certificate;

    @InjectMocks
    private CertificateSendFunction certificateSendFunction;

    @Mock
    private AuthoritiesHelper authoritiesHelper;

    @BeforeEach
    void setup() {
        final var metadata = CertificateMetadata.builder()
            .type(TYPE)
            .build();
        certificate = new Certificate();
        certificate.setMetadata(metadata);
    }

    @Nested
    class TestFeatureSend {

        @Test
        void shouldReturnAvailableFunctionSendIfFeatureIsActive() {
            doReturn(true)
                .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_SKICKA_INTYG, TYPE);

            doReturn(true)
                .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_INACTIVATE_PREVIOUS_MAJOR_VERSION, TYPE);

            final var response = certificateSendFunction.get(certificate);

            assertEquals(1, response.size());
            assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, response.get(0).getType());
        }

        @Test
        void shouldReturnEmptyIfFeatureIsInactive() {
            final var response = certificateSendFunction.get(certificate);

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

        @Test
        void shouldReturnEmptyIfFeatureIsInactive() {
            final var response = certificateSendFunction.get(certificate);

            assertEquals(Collections.emptyList(), response);
        }

        @Test
        void shouldReturnAvailableFunctionSendIfFeatureIsActive() {
            doReturn(true)
                .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_INACTIVATE_PREVIOUS_MAJOR_VERSION, TYPE);

            final var response = certificateSendFunction.get(certificate);

            assertEquals(1, response.size());
            assertEquals(AvailableFunctionTypeDTO.SEND_CERTIFICATE, response.get(0).getType());
        }
    }
}
