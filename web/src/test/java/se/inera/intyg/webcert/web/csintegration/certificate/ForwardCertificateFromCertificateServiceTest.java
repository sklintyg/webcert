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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.ForwardCertificateRequestDTO;

@ExtendWith(MockitoExtension.class)
class ForwardCertificateFromCertificateServiceTest {

    private static final ForwardCertificateRequestDTO REQUEST = ForwardCertificateRequestDTO.builder().build();
    private static final String ID = "ID";
    private static final boolean FORWARDED = false;
    private static final Certificate CERTIFICATE = new Certificate();
    private static final String TYPE = "TYPE";

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @Mock
    DecorateCertificateFromCSWithInformationFromWC decorateCertificateFromCSWithInformationFromWC;

    @InjectMocks
    ForwardCertificateFromCertificateService forwardCertificateFromCertificateService;

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        final var response = forwardCertificateFromCertificateService.forwardCertificate(ID, FORWARDED);

        assertNull(response);
    }

    @Nested
    class CertificateExistsInCS {

        @BeforeEach
        void setup() {

            CERTIFICATE.setMetadata(CertificateMetadata.builder()
                .id(ID)
                .type(TYPE)
                .build());

            when(csIntegrationService.certificateExists(ID))
                .thenReturn(true);

            when(csIntegrationRequestFactory.forwardCertificateRequest())
                .thenReturn(REQUEST);
        }

        @Nested
        class CertificateIsForwardFromCS {

            @BeforeEach
            void setup() {
                when(csIntegrationService.forwardCertificate(ID, REQUEST))
                    .thenReturn(CERTIFICATE);
            }

            @Test
            void shouldCallForwardWithId() {
                final var captor = ArgumentCaptor.forClass(String.class);
                forwardCertificateFromCertificateService.forwardCertificate(ID, FORWARDED);

                verify(csIntegrationService).forwardCertificate(captor.capture(), any(ForwardCertificateRequestDTO.class));
                assertEquals(ID, captor.getValue());
            }

            @Test
            void shouldCallForwardWithRequest() {
                final var captor = ArgumentCaptor.forClass(ForwardCertificateRequestDTO.class);
                forwardCertificateFromCertificateService.forwardCertificate(ID, FORWARDED);

                verify(csIntegrationService).forwardCertificate(anyString(), captor.capture());
                assertEquals(REQUEST, captor.getValue());
            }

            @Test
            void shouldDecorateCertificateFromCSWithInformationFromWC() {
                forwardCertificateFromCertificateService.forwardCertificate(ID, FORWARDED);
                verify(decorateCertificateFromCSWithInformationFromWC, times(1)).decorate(CERTIFICATE);
            }
        }
    }
}
