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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
import se.funktionstjanster.grp.v2.AuthenticateRequestTypeV23;
import se.funktionstjanster.grp.v2.GrpException;
import se.funktionstjanster.grp.v2.GrpServicePortType;
import se.funktionstjanster.grp.v2.OrderResponseTypeV23;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.csintegration.certificate.FinalizedCertificateSignature;
import se.inera.intyg.webcert.web.csintegration.certificate.SignCertificateService;
import se.inera.intyg.webcert.web.service.underskrift.grp.factory.GrpCollectPollerFactory;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

/**
 * Created by eriklupander on 2015-08-25.
 */
@ExtendWith(MockitoExtension.class)
class GrpUnderskriftServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final String INTYG_ID = "intyg-1";
    private static final long VERSION = 1L;
    private static final String PERSON_ID = "19121212-1212";
    private static final String TX_ID = "webcert-tx-1";
    private static final String ORDER_REF = "order-ref-1";
    private static final String CERTIFICATE_ID = "certificateId";

    @Mock
    RedisTicketTracker redisTicketTracker;
    @Mock
    GrpServicePortType grpService;
    @Mock
    ThreadPoolTaskExecutor taskExecutor;
    @Mock
    GrpCollectPollerFactory grpCollectPollerFactory;
    @Mock
    SignCertificateService signCertificateService;

    @InjectMocks
    GrpUnderskriftServiceImpl grpSignaturService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(grpSignaturService, "redisTicketTracker", redisTicketTracker);
    }

    @Test
    void testSuccessfulAuthenticationRequest() throws GrpException {
        when(grpCollectPollerFactory.getInstance()).thenReturn(mock(GrpCollectPoller.class));
        when(grpService.authenticate(any(AuthenticateRequestTypeV23.class))).thenReturn(buildOrderResponse());

        grpSignaturService.startGrpCollectPoller(PERSON_ID, buildSignaturBiljett());
        verify(taskExecutor, times(1)).execute(any(GrpCollectPoller.class));
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

    private OrderResponseTypeV23 buildOrderResponse() {
        OrderResponseTypeV23 resp = new OrderResponseTypeV23();
        resp.setTransactionId(TX_ID);
        resp.setOrderRef(ORDER_REF);
        return resp;
    }

    private SignaturBiljett buildSignaturBiljett() {
        return SignaturBiljett.SignaturBiljettBuilder
            .aSignaturBiljett(TX_ID, SignaturTyp.PKCS7, SignMethod.GRP)
            .withHash("hash")
            .withSkapad(LocalDateTime.now())
            .withStatus(SignaturStatus.OKAND)
            .withVersion(VERSION)
            .withIntygsId(INTYG_ID)
            .build();
    }

}
