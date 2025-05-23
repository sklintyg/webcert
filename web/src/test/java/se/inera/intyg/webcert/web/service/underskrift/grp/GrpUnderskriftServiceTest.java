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
package se.inera.intyg.webcert.web.service.underskrift.grp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationJunit5TestSetup;
import se.inera.intyg.webcert.web.csintegration.certificate.FinalizedCertificateSignature;
import se.inera.intyg.webcert.web.csintegration.certificate.SignCertificateService;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpOrderResponse;
import se.inera.intyg.webcert.web.service.underskrift.grp.factory.GrpCollectPollerFactory;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

@ExtendWith(MockitoExtension.class)
class GrpUnderskriftServiceTest extends AuthoritiesConfigurationJunit5TestSetup {

    @Mock
    RedisTicketTracker redisTicketTracker;
    @Mock
    GrpRestClient grpRestClient;
    @Mock
    ThreadPoolTaskExecutor taskExecutor;
    @Mock
    GrpCollectPollerFactory grpCollectPollerFactory;
    @Mock
    SignCertificateService signCertificateService;
    @Mock
    GrpCollectPoller grpCollectPoller;

    @InjectMocks
    GrpUnderskriftServiceImpl grpSignaturService;

    private static final String INTYG_ID = "intyg-1";
    private static final long VERSION = 1L;
    private static final String PERSON_ID = "19121212-1212";
    private static final String TRANSACTION_ID = "transactionId";
    private static final String REF_ID = "refId";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String AUTO_START_TOKEN = "autoStartToken";
    private static final String QR_START_TOKEN = "qrStartToken";
    private static final String QR_START_SECRET = "qrStartSecret";

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(grpSignaturService, "redisTicketTracker", redisTicketTracker);
    }

    @Test
    void shouldHandleSuccessfulGrpSignatureInitialization() {
        final var ticket = buildSignaturBiljett();
        when(grpCollectPollerFactory.getInstance()).thenReturn(grpCollectPoller);
        when(grpRestClient.init(PERSON_ID, ticket)).thenReturn(grpOrderResponse());

        grpSignaturService.startGrpCollectPoller(PERSON_ID, ticket);
        verify(redisTicketTracker).updateAutoStartToken(TRANSACTION_ID, AUTO_START_TOKEN);
        verify(redisTicketTracker).updateQrCodeProperties(TRANSACTION_ID, QR_START_TOKEN, QR_START_SECRET);
        verify(taskExecutor, times(1)).execute(grpCollectPoller);
    }

    @Test
    void shouldThrowIllegalStateIfTicketIdAndOrderResponseTxIdDoNotMatch() {
        final var ticket = buildSignaturBiljett();
        ticket.setTicketId("wrongTicketId");
        when(grpRestClient.init(PERSON_ID, ticket)).thenReturn(grpOrderResponse());

        assertThrows(IllegalStateException.class, () -> grpSignaturService.startGrpCollectPoller(PERSON_ID, ticket));
    }

    @Test
    void shouldThrowWebcertServiceExceptionIfGrpSignInitFaiure() {
        final var ticket = buildSignaturBiljett();
        when(grpRestClient.init(PERSON_ID, ticket)).thenThrow(WebCertServiceException.class);

        assertThrows(WebCertServiceException.class, () -> grpSignaturService.startGrpCollectPoller(PERSON_ID, ticket));
    }

    @Test
    void shallFinalizeSignatureForCS() {
        final var certificate = new Certificate();
        final var signaturBiljett = new SignaturBiljett();
        signaturBiljett.setIntygsId(CERTIFICATE_ID);
        signaturBiljett.setVersion(VERSION);
        final var expectedResult = FinalizedCertificateSignature.builder()
            .certificate(certificate)
            .signaturBiljett(signaturBiljett)
            .build();

        doReturn(certificate).when(signCertificateService).signWithoutSignature(CERTIFICATE_ID, VERSION);
        final var actualResult = grpSignaturService.finalizeSignatureForCS(signaturBiljett, null, null);

        verify(redisTicketTracker).updateStatus(signaturBiljett.getTicketId(), signaturBiljett.getStatus());
        assertEquals(expectedResult, actualResult);
        assertEquals(SignaturStatus.SIGNERAD, signaturBiljett.getStatus());
    }

    private GrpOrderResponse grpOrderResponse() {
        return GrpOrderResponse.builder()
            .refId(REF_ID)
            .transactionId(TRANSACTION_ID)
            .autoStartToken(AUTO_START_TOKEN)
            .qrStartToken(QR_START_TOKEN)
            .qrStartSecret(QR_START_SECRET)
            .build();
    }

    private SignaturBiljett buildSignaturBiljett() {
        return SignaturBiljett.SignaturBiljettBuilder
            .aSignaturBiljett(TRANSACTION_ID, SignaturTyp.PKCS7, SignMethod.GRP)
            .withHash("hash")
            .withSkapad(LocalDateTime.now())
            .withStatus(SignaturStatus.OKAND)
            .withVersion(VERSION)
            .withIntygsId(INTYG_ID)
            .build();
    }

}
