/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.DeleteCertificateRequestDTO;

@ExtendWith(MockitoExtension.class)
class DeleteCertificateFromCertificateServiceTest {

    private static final DeleteCertificateRequestDTO REQUEST = DeleteCertificateRequestDTO.builder().build();

    @Mock
    CSIntegrationService csIntegrationService;

    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;

    @InjectMocks
    DeleteCertificateFromCertificateService deleteCertificateFromCertificateService;

    @BeforeEach
    void setup() {
        when(csIntegrationRequestFactory.deleteCertificateRequest())
            .thenReturn(REQUEST);
    }

    @Test
    void shouldCallDeleteWithId() {
        final var captor = ArgumentCaptor.forClass(Long.class);
        deleteCertificateFromCertificateService.deleteCertificate("ID", 10);

        verify(csIntegrationService).deleteCertificate(anyString(), captor.capture(), any(DeleteCertificateRequestDTO.class));
        assertEquals(10, captor.getValue());
    }

    @Test
    void shouldCallDeleteWithVersion() {
        final var captor = ArgumentCaptor.forClass(String.class);
        deleteCertificateFromCertificateService.deleteCertificate("ID", 10);

        verify(csIntegrationService).deleteCertificate(captor.capture(), anyLong(), any(DeleteCertificateRequestDTO.class));
        assertEquals("ID", captor.getValue());
    }

    @Test
    void shouldCallDeleteWithRequest() {
        final var captor = ArgumentCaptor.forClass(DeleteCertificateRequestDTO.class);
        deleteCertificateFromCertificateService.deleteCertificate("ID", 10);

        verify(csIntegrationService).deleteCertificate(anyString(), anyLong(), captor.capture());
        assertEquals(REQUEST, captor.getValue());
    }

    @Test
    void shouldPdlLogDelete() {

    }


}