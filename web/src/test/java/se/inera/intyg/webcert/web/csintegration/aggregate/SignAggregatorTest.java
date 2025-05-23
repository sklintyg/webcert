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

package se.inera.intyg.webcert.web.csintegration.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
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
    private static final byte[] SIGN_BYTE = "signature".getBytes(StandardCharsets.UTF_8);
    private static final String SIGN_CERTIFICATE = "sign-certificate";
    private static final String USER_IP_ADDRESS = "user-ip-address";
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

            signCertificateAggregator.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE, TICKED_ID, USER_IP_ADDRESS);

            verify(signatureServiceForWC, times(1)).startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                TICKED_ID, USER_IP_ADDRESS);
        }

        @Test
        void shallUseWebcertImplementationIfCSReturnsNull() {
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(null).when(signatureServiceForCS).startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                TICKED_ID, USER_IP_ADDRESS);

            signCertificateAggregator.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE, TICKED_ID, USER_IP_ADDRESS);

            verify(signatureServiceForWC, times(1)).startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                TICKED_ID, USER_IP_ADDRESS);
        }


        @Test
        void shallUseCertificateServiceImplementationIfProfileIsActiveAndSignatureBiljettIsReturned() {
            final var expectedResult = new SignaturBiljett();
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(expectedResult).when(signatureServiceForCS)
                .startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                    TICKED_ID, USER_IP_ADDRESS);

            final var actualResult = signCertificateAggregator.startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L,
                SignMethod.SIGN_SERVICE, TICKED_ID, USER_IP_ADDRESS);

            verify(signatureServiceForWC, times(0)).startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                TICKED_ID, USER_IP_ADDRESS);
            verify(signatureServiceForCS, times(1)).startSigningProcess(CERTIFICATE_ID, CERTIFICATE_TYPE, 1L, SignMethod.SIGN_SERVICE,
                TICKED_ID, USER_IP_ADDRESS);
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

    @Nested
    class NetidSignature {

        @Test
        void shallUseWebcertImplementationIfProfileNotActive() {
            doReturn(false).when(certificateServiceProfile).active();

            signCertificateAggregator.netidSignature(TICKED_ID, SIGN_BYTE, SIGN_CERTIFICATE);

            verify(signatureServiceForWC, times(1)).netidSignature(TICKED_ID, SIGN_BYTE, SIGN_CERTIFICATE);
        }

        @Test
        void shallUseWebcertImplementationIfCSReturnsNull() {
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(null).when(signatureServiceForCS).netidSignature(TICKED_ID, SIGN_BYTE, SIGN_CERTIFICATE);

            signCertificateAggregator.netidSignature(TICKED_ID, SIGN_BYTE, SIGN_CERTIFICATE);
            verify(signatureServiceForWC, times(1)).netidSignature(TICKED_ID, SIGN_BYTE, SIGN_CERTIFICATE);
        }


        @Test
        void shallUseCertificateServiceImplementationIfProfileIsActiveAndSignatureBiljettIsReturned() {
            final var expectedResult = new SignaturBiljett();
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(expectedResult).when(signatureServiceForCS).netidSignature(TICKED_ID, SIGN_BYTE, SIGN_CERTIFICATE);

            final var actualResult = signCertificateAggregator.netidSignature(TICKED_ID, SIGN_BYTE, SIGN_CERTIFICATE);

            verify(signatureServiceForWC, times(0)).netidSignature(TICKED_ID, SIGN_BYTE, SIGN_CERTIFICATE);
            verify(signatureServiceForCS, times(1)).netidSignature(TICKED_ID, SIGN_BYTE, SIGN_CERTIFICATE);
            assertEquals(expectedResult, actualResult);
        }
    }

    @Nested
    class GrpSignature {

        @Test
        void shallUseWebcertImplementationIfProfileNotActive() {
            doReturn(false).when(certificateServiceProfile).active();

            signCertificateAggregator.grpSignature(TICKED_ID, SIGN_BYTE);

            verify(signatureServiceForWC, times(1)).grpSignature(TICKED_ID, SIGN_BYTE);
        }

        @Test
        void shallUseWebcertImplementationIfCSReturnsNull() {
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(null).when(signatureServiceForCS).grpSignature(TICKED_ID, SIGN_BYTE);

            signCertificateAggregator.grpSignature(TICKED_ID, SIGN_BYTE);
            verify(signatureServiceForWC, times(1)).grpSignature(TICKED_ID, SIGN_BYTE);
        }


        @Test
        void shallUseCertificateServiceImplementationIfProfileIsActiveAndSignatureBiljettIsReturned() {
            final var expectedResult = new SignaturBiljett();
            doReturn(true).when(certificateServiceProfile).active();
            doReturn(expectedResult).when(signatureServiceForCS).grpSignature(TICKED_ID, SIGN_BYTE);

            final var actualResult = signCertificateAggregator.grpSignature(TICKED_ID, SIGN_BYTE);

            verify(signatureServiceForWC, times(0)).grpSignature(TICKED_ID, SIGN_BYTE);
            verify(signatureServiceForCS, times(1)).grpSignature(TICKED_ID, SIGN_BYTE);
            assertEquals(expectedResult, actualResult);
        }
    }

    @Nested
    class SigneringsStatus {

        @Test
        void shallUseWebcertImplementation() {
            signCertificateAggregator.signeringsStatus(TICKED_ID);
            verify(signatureServiceForWC).signeringsStatus(TICKED_ID);
        }
    }
}
