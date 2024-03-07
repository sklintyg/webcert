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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.facade.SignCertificateFacadeService;

@ExtendWith(MockitoExtension.class)
class SignCertificateAggregatorTest {


    @Mock
    private CertificateServiceProfile certificateServiceProfile;
    @Mock
    private SignCertificateFacadeService signCertificateFacadeServiceWC;
    @Mock
    private SignCertificateFacadeService signCertificateFacadeServiceCS;
    private SignCertificateFacadeService signCertificateAggregator;

    private static final Certificate CERTIFICATE = new Certificate();

    @BeforeEach
    void setUp() {
        signCertificateAggregator = new SignCertificateAggregator(
            certificateServiceProfile,
            signCertificateFacadeServiceWC,
            signCertificateFacadeServiceCS
        );
    }

    @Test
    void shallReturnSignedCertificateFromWcIfCertificateServiceProfileIsInactive() {
        final var expectedCertificate = new Certificate();

        doReturn(false).when(certificateServiceProfile).active();
        doReturn(expectedCertificate).when(signCertificateFacadeServiceWC).signCertificate(CERTIFICATE);

        final var actualCertificaite = signCertificateAggregator.signCertificate(CERTIFICATE);

        verify(signCertificateFacadeServiceWC, times(1)).signCertificate(CERTIFICATE);
        assertEquals(expectedCertificate, actualCertificaite);
    }

    @Test
    void shallReturnSignedCertificateFromWcIfCertificateServiceProfileIsActiveButCertificateServiceReturnsNull() {
        final var expectedCertificate = new Certificate();

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(null).when(signCertificateFacadeServiceCS).signCertificate(CERTIFICATE);
        doReturn(expectedCertificate).when(signCertificateFacadeServiceWC).signCertificate(CERTIFICATE);

        final var actualCertificaite = signCertificateAggregator.signCertificate(CERTIFICATE);

        verify(signCertificateFacadeServiceWC, times(1)).signCertificate(CERTIFICATE);
        assertEquals(expectedCertificate, actualCertificaite);
    }

    @Test
    void shallReturnSignedCertificateFromCSIfCertificateServiceProfileIsActiveAndCertificateServiceReturnsCertificate() {
        final var expectedCertificate = new Certificate();

        doReturn(true).when(certificateServiceProfile).active();
        doReturn(expectedCertificate).when(signCertificateFacadeServiceCS).signCertificate(CERTIFICATE);

        final var actualCertificaite = signCertificateAggregator.signCertificate(CERTIFICATE);

        verify(signCertificateFacadeServiceWC, times(0)).signCertificate(CERTIFICATE);
        assertEquals(expectedCertificate, actualCertificaite);
    }
}
