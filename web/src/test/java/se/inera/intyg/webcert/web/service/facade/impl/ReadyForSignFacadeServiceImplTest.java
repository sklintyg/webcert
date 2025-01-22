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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.webcert.web.service.facade.GetCertificateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;

@ExtendWith(MockitoExtension.class)
class ReadyForSignFacadeServiceImplTest {

    @Mock
    private UtkastService utkastService;

    @Mock
    private GetCertificateFacadeService getCertificateFacadeService;

    @InjectMocks
    private ReadyForSignFacadeServiceImpl readyForSignFacadeService;

    private final static String CERTIFICATE_ID = "XXXXX-YYYYY-ZZZZZ";
    private final static String CERTIFICATE_TYPE = "certificateType";
    private Certificate certificate;

    @BeforeEach
    void setup() {
        certificate = CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id(CERTIFICATE_ID)
                    .build()
            )
            .build();

        doReturn(CERTIFICATE_TYPE)
            .when(utkastService)
            .getCertificateType(CERTIFICATE_ID);

        doReturn(certificate)
            .when(getCertificateFacadeService)
            .getCertificate(CERTIFICATE_ID, false, true);
    }

    @Test
    void shallReadyForSign() {
        readyForSignFacadeService.readyForSign(CERTIFICATE_ID);

        verify(utkastService).setKlarForSigneraAndSendStatusMessage(CERTIFICATE_ID, CERTIFICATE_TYPE);
    }

    @Test
    void shallReturnCertificateWithReadyForSignDateTime() {
        final var expectedReadyForSign = LocalDateTime.now();

        certificate.getMetadata().setReadyForSign(expectedReadyForSign);

        final var actualCertificate = readyForSignFacadeService.readyForSign(CERTIFICATE_ID);

        assertEquals(expectedReadyForSign, actualCertificate.getMetadata().getReadyForSign());
    }
}
