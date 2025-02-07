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
import se.inera.intyg.common.support.modules.support.facade.dto.CertificateEventDTO;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateEventsRequestDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificateEventsFromCertificateServiceTest {

    private static final GetCertificateEventsRequestDTO REQUEST = GetCertificateEventsRequestDTO.builder().build();
    private static final String ID = "ID";
    private static final CertificateEventDTO[] EVENTS = {new CertificateEventDTO()};

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;


    @InjectMocks
    GetCertificateEventsFromCertificateService getCertificateEventsFromCertificateService;

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        final var response = getCertificateEventsFromCertificateService.getCertificateEvents(ID);

        assertNull(response);
    }

    @Nested
    class CertificateExistsInCS {

        @BeforeEach
        void setup() {
            when(csIntegrationService.certificateExists(ID))
                .thenReturn(true);

            when(csIntegrationRequestFactory.getCertificateEventsRequest())
                .thenReturn(REQUEST);
        }

        @Nested
        class CertificateIsForwardFromCS {

            @BeforeEach
            void setup() {
                when(csIntegrationService.getCertificateEvents(ID, REQUEST))
                    .thenReturn(EVENTS);
            }

            @Test
            void shouldCallMethodWithId() {
                final var captor = ArgumentCaptor.forClass(String.class);
                getCertificateEventsFromCertificateService.getCertificateEvents(ID);

                verify(csIntegrationService).getCertificateEvents(captor.capture(), any(GetCertificateEventsRequestDTO.class));
                assertEquals(ID, captor.getValue());
            }

            @Test
            void shouldCallMethodWithRequest() {
                final var captor = ArgumentCaptor.forClass(GetCertificateEventsRequestDTO.class);
                getCertificateEventsFromCertificateService.getCertificateEvents(ID);

                verify(csIntegrationService).getCertificateEvents(anyString(), captor.capture());
                assertEquals(REQUEST, captor.getValue());
            }

        }
    }
}
