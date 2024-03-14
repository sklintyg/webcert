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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

@ExtendWith(MockitoExtension.class)
class SignAggregatorTest {


    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "certificateType";
    private static final String TICKED_ID = "tickedId";
    @Mock
    private CertificateServiceProfile certificateServiceProfile;
    @Mock
    private UnderskriftService signatureServiceForWC;
    @Mock
    private UnderskriftService signatureServiceForCS;
    private UnderskriftService signCertificateAggregator;


    @BeforeEach
    void setUp() {
        signCertificateAggregator = new SignAggregator(
            certificateServiceProfile,
            signatureServiceForWC,
            signatureServiceForCS
        );
    }

    @Nested
    class StartSigningProcess {

        @Test
        void shallUseWebcertImplementationIfProfileNotActive() {
            doReturn(false).when(certificateServiceProfile).active();

            signCertificateAggregator.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE, TICKED_ID,
                false);

            verify(signatureServiceForWC, times(1)).startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                TICKED_ID,
                false);
        }

        @Test
        void shallUseWebcertImplementationIfCSReturnsNull() {
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(null).when(signatureServiceForCS).startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                TICKED_ID, false);

            signCertificateAggregator.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE, TICKED_ID, false);

            verify(signatureServiceForWC, times(1)).startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                TICKED_ID,
                false);
        }


        @Test
        void shallUseCertificateServiceImplementationIfProfileIsActiveAndSignatureBiljettIsReturned() {
            final var expectedResult = new SignaturBiljett();
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(expectedResult).when(signatureServiceForCS)
                .startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                    TICKED_ID, false);

            final var actualResult = signCertificateAggregator.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L,
                SignMethod.SIGN_SERVICE, TICKED_ID, false);

            verify(signatureServiceForWC, times(0)).startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                TICKED_ID,
                false);
            verify(signatureServiceForCS, times(1)).startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                TICKED_ID,
                false);
            assertEquals(expectedResult, actualResult);
        }
    }

    @Nested
    class FakeSignature {

        @Test
        void shallUseWebcertImplementationIfProfileNotActive() {
            doReturn(false).when(certificateServiceProfile).active();

            signCertificateAggregator.fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, TICKED_ID);

            verify(signatureServiceForWC, times(1)).fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, TICKED_ID);
        }

        @Test
        void shallUseWebcertImplementationIfCSReturnsNull() {
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(null).when(signatureServiceForCS).fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, TICKED_ID);

            signCertificateAggregator.fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, TICKED_ID);
            verify(signatureServiceForWC, times(1)).fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, TICKED_ID);
        }


        @Test
        void shallUseCertificateServiceImplementationIfProfileIsActiveAndSignatureBiljettIsReturned() {
            final var expectedResult = new SignaturBiljett();
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(expectedResult).when(signatureServiceForCS).fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, TICKED_ID);

            final var actualResult = signCertificateAggregator.fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, TICKED_ID);

            verify(signatureServiceForWC, times(0)).fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, TICKED_ID);
            verify(signatureServiceForCS, times(1)).fakeSignature(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, TICKED_ID);
            assertEquals(expectedResult, actualResult);
        }
    }
}
