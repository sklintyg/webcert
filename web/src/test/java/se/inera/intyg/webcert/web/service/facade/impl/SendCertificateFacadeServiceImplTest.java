/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygServiceResult;
import se.inera.intyg.webcert.web.service.receiver.CertificateReceiverService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.web.controller.api.dto.IntygReceiver;

@ExtendWith(MockitoExtension.class)
public class SendCertificateFacadeServiceImplTest {

    @Mock
    private IntygService intygService;
    @Mock
    private UtkastService utkastService;
    @Mock
    private CertificateReceiverService certificateReceiverService;

    @InjectMocks
    private SendCertificateFacadeServiceImpl sendCertificateFacadeService;

    private final static String CERTIFICATE_ID = "certificateId";
    private final static String CERTIFICATE_TYPE = "lisjp";
    private final static String RECEIVER_ID = "FKASSA";

    @BeforeEach
    void setup() {
        final var draft = new Utkast();
        draft.setIntygsId(CERTIFICATE_ID);
        draft.setIntygsTyp(CERTIFICATE_TYPE);

        List<IntygReceiver> receivers = new ArrayList<>();
        IntygReceiver receiver = new IntygReceiver();
        receiver.setId(RECEIVER_ID);
        receiver.setLocked(true);
        receivers.add(receiver);

        when(utkastService.getDraft(eq(CERTIFICATE_ID), eq(false))).thenReturn(draft);
        when(certificateReceiverService.listPossibleReceivers(eq(CERTIFICATE_TYPE))).thenReturn(receivers);
        when(intygService.sendIntyg(eq(CERTIFICATE_ID), eq(CERTIFICATE_TYPE), eq(RECEIVER_ID), eq(false)))
            .thenReturn(IntygServiceResult.OK);
    }

    @Test
    void shallSendCertificate() {
        sendCertificateFacadeService.sendCertificate(CERTIFICATE_ID);
        verify(intygService).sendIntyg(CERTIFICATE_ID, CERTIFICATE_TYPE, RECEIVER_ID, false);
    }
}
