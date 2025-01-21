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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.GetCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.util.PDLLogService;

@ExtendWith(MockitoExtension.class)
class GetCertificateFromCertificateServiceTest {

    @Mock
    CSIntegrationService csIntegrationService;
    @Mock
    CSIntegrationRequestFactory csIntegrationRequestFactory;
    @Mock
    PDLLogService pdlLogService;
    @Mock
    DecorateCertificateFromCSWithInformationFromWC decorateCertificateFromCSWithInformationFromWC;
    @InjectMocks
    GetCertificateFromCertificateService getCertificateFromCertificateService;

    private static final String CERTIFICATE_ID = "ID";
    private static final Certificate CERTIFICATE = new Certificate();
    private static final GetCertificateRequestDTO REQUEST = GetCertificateRequestDTO.builder().build();

    @Test
    void shouldReturnNullIfCertificateDoesNotExistInCS() {
        assertNull(
            getCertificateFromCertificateService.getCertificate(CERTIFICATE_ID, true, true)
        );
    }

    @Test
    void shouldNotPerformPDLLogIfTypeWasNotRetrievedFromCS() {
        getCertificateFromCertificateService.getCertificate(CERTIFICATE_ID, true, true);
        verifyNoInteractions(pdlLogService);
    }

    @Nested
    class CertificateServiceHasCertificate {

        @BeforeEach
        void setup() {
            when(csIntegrationService.certificateExists(CERTIFICATE_ID))
                .thenReturn(true);
            when(csIntegrationService.getCertificate(CERTIFICATE_ID, REQUEST))
                .thenReturn(CERTIFICATE);
            when(csIntegrationRequestFactory.getCertificateRequest())
                .thenReturn(REQUEST);
        }

        @Test
        void shouldReturnCertificate() {
            assertEquals(CERTIFICATE,
                getCertificateFromCertificateService.getCertificate(CERTIFICATE_ID, true, true)
            );
        }

        @Test
        void shouldPerformPDLForCreateCertificateIfPdlLogIsTrue() {
            getCertificateFromCertificateService.getCertificate(CERTIFICATE_ID, true, true);
            verify(pdlLogService, times(1)).logRead(CERTIFICATE);
        }

        @Test
        void shouldNotPerformPDLForCreateCertificateIfPdlLogIsFalse() {
            getCertificateFromCertificateService.getCertificate(CERTIFICATE_ID, false, true);
            verifyNoInteractions(pdlLogService);
        }
        
        @Test
        void shouldDecorateCertificateFromCSWithInformationFromWC() {
            getCertificateFromCertificateService.getCertificate(CERTIFICATE_ID, true, true);
            verify(decorateCertificateFromCSWithInformationFromWC, times(1)).decorate(CERTIFICATE);
        }
    }

}
