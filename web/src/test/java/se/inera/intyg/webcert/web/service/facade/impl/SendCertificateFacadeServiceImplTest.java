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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygTypeInfo;

@ExtendWith(MockitoExtension.class)
public class SendCertificateFacadeServiceImplTest {

    @Mock
    private IntygService intygService;
    @Mock
    private CertificateReceiverService certificateReceiverService;

    @InjectMocks
    private SendCertificateFacadeServiceImpl sendCertificateFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";
    private final static String CERTIFICATE_TYPE = "lisjp";
    private final static String MAIN_RECEIVER_ID = "FKASSA";
    private final static String ORDINARY_RECEIVER_ID = "RECEIVER";

    @BeforeEach
    void setup() {
        doReturn(new IntygTypeInfo(CERTIFICATE_ID, CERTIFICATE_TYPE, "certificateTypeVersion"))
            .when(intygService).getIntygTypeInfo(CERTIFICATE_ID);
        when(intygService.sendIntyg(eq(CERTIFICATE_ID), eq(CERTIFICATE_TYPE), eq(MAIN_RECEIVER_ID), eq(false)))
            .thenReturn(IntygServiceResult.OK);
    }

    @Test
    void shallSendCertificate() {
        List<IntygReceiver> receivers = new ArrayList<>();
        IntygReceiver receiver = new IntygReceiver();
        receiver.setId(MAIN_RECEIVER_ID);
        receiver.setLocked(true);
        receivers.add(receiver);

        when(certificateReceiverService.listPossibleReceivers(eq(CERTIFICATE_TYPE))).thenReturn(receivers);

        sendCertificateFacadeService.sendCertificate(CERTIFICATE_ID);
        verify(intygService).sendIntyg(CERTIFICATE_ID, CERTIFICATE_TYPE, MAIN_RECEIVER_ID, false);
    }

    @Test
    void onlySendsCertificateToMainReceivers() {
        List<IntygReceiver> receivers = new ArrayList<>();
        IntygReceiver mainReceiver = new IntygReceiver();
        mainReceiver.setId(MAIN_RECEIVER_ID);
        mainReceiver.setLocked(true);
        receivers.add(mainReceiver);
        IntygReceiver receiver = new IntygReceiver();
        receiver.setId(ORDINARY_RECEIVER_ID);
        receiver.setLocked(false);
        receivers.add(receiver);

        when(certificateReceiverService.listPossibleReceivers(eq(CERTIFICATE_TYPE))).thenReturn(receivers);

        sendCertificateFacadeService.sendCertificate(CERTIFICATE_ID);
        verify(intygService).sendIntyg(CERTIFICATE_ID, CERTIFICATE_TYPE, MAIN_RECEIVER_ID, false);
        verify(intygService, never()).sendIntyg(CERTIFICATE_ID, CERTIFICATE_TYPE, ORDINARY_RECEIVER_ID, false);
    }
}
