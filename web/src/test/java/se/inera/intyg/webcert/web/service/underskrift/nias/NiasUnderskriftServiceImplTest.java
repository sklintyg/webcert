/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift.nias;

import com.secmaker.netid.nias.v1.NetiDAccessServerSoap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.nias.factory.NiasCollectPollerFactory;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.PERSON_ID;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.createSignaturBiljett;

@RunWith(MockitoJUnitRunner.class)
public class NiasUnderskriftServiceImplTest {

    @Mock
    private NetiDAccessServerSoap netiDAccessServerSoap;

    @Mock
    private NiasCollectPollerFactory niasCollectPollerFactory;

    @Mock
    private ThreadPoolTaskExecutor taskExecutor;

    @Mock
    private RedisTicketTracker redisTicketTracker;

    @InjectMocks
    private NiasUnderskriftServiceImpl testee;

    @Test
    public void testSignOk() {
        when(netiDAccessServerSoap.sign(anyString(),anyString(), anyString(), ArgumentMatchers.isNull()))
                .thenReturn(buildSignResponse());
        when(niasCollectPollerFactory.getInstance()).thenReturn(mock(NiasCollectPoller.class));

        testee.startNiasCollectPoller(PERSON_ID, createSignaturBiljett(SignaturStatus.BEARBETAR));
        verifyZeroInteractions(redisTicketTracker);
        verify(taskExecutor, times(1)).execute(any(Runnable.class), anyLong());
    }

    @Test(expected = WebCertServiceException.class)
    public void testSignFails() {
        when(netiDAccessServerSoap.sign(anyString(),anyString(), anyString(), ArgumentMatchers.isNull()))
                .thenThrow(new RuntimeException("some exception"));

        try {
            testee.startNiasCollectPoller(PERSON_ID, createSignaturBiljett(SignaturStatus.BEARBETAR));
        } finally {
            verify(redisTicketTracker, times(1)).updateStatus(anyString(), eq(SignaturStatus.OKAND));
            verify(taskExecutor, times(0)).execute(any(Runnable.class), anyLong());
        }
    }

    private String buildSignResponse() {
        return "<SignResponse><signResult>OK</signResult></SignResponse>";
    }
}
