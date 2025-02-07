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
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationRequestFactory;
import se.inera.intyg.webcert.web.csintegration.integration.CSIntegrationService;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateRequestDTO;
import se.inera.intyg.webcert.web.csintegration.integration.dto.SignCertificateWithoutSignatureRequestDTO;

@ExtendWith(MockitoExtension.class)
class SignCertificateServiceTest {

    private static final SignCertificateRequestDTO SIGN_CERTIFICATE_REQUEST = SignCertificateRequestDTO.builder().build();
    private static final SignCertificateWithoutSignatureRequestDTO SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO =
        SignCertificateWithoutSignatureRequestDTO.builder()
            .build();
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String SIGNATURE_XML = "<signature></signature>";
    private static final long VERSION = 1L;
    @Mock
    private CSIntegrationService csIntegrationService;
    @Mock
    private CSIntegrationRequestFactory csIntegrationRequestFactory;
    @InjectMocks
    private SignCertificateService signCertificateService;

    @Nested
    class Sign {

        @Test
        void shallReturnSignedCertificate() {
            final var expectedCertificate = new Certificate();
            doReturn(SIGN_CERTIFICATE_REQUEST).when(csIntegrationRequestFactory).signCertificateRequest(SIGNATURE_XML);
            doReturn(expectedCertificate).when(csIntegrationService).signCertificate(SIGN_CERTIFICATE_REQUEST, CERTIFICATE_ID, VERSION);

            final var actualCertificate = signCertificateService.sign(CERTIFICATE_ID, SIGNATURE_XML, VERSION);
            assertEquals(expectedCertificate, actualCertificate);
        }

    }

    @Nested
    class SignWithoutSignature {

        @Test
        void shallReturnSignedCertificate() {
            final var expectedCertificate = new Certificate();
            doReturn(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO).when(csIntegrationRequestFactory)
                .signCertificateWithoutSignatureRequest();
            doReturn(expectedCertificate).when(csIntegrationService)
                .signCertificateWithoutSignature(SIGN_CERTIFICATE_WITHOUT_SIGNATURE_REQUEST_DTO, CERTIFICATE_ID, VERSION);

            final var actualCertificate = signCertificateService.signWithoutSignature(CERTIFICATE_ID, VERSION);
            assertEquals(expectedCertificate, actualCertificate);
        }
    }
}
