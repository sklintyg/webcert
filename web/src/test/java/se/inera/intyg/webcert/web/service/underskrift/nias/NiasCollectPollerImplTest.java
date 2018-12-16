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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import com.secmaker.netid.nias.v1.NetiDAccessServerSoap;
import com.secmaker.netid.nias.v1.ResultCollect;
import com.secmaker.netid.nias.v1.UserInfoType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.ORDER_REF;
import static se.inera.intyg.webcert.web.service.underskrift.testutil.UnderskriftTestUtil.TICKET_ID;

@RunWith(MockitoJUnitRunner.class)
public class NiasCollectPollerImplTest {

    private static final String HSA_ID = "user-1";
    private static final String HSA_ID_2 = "user-2";

    @Mock
    private RedisTicketTracker redisTicketTracker;

    @Mock
    private UnderskriftService underskriftService;

    @Mock
    private NetiDAccessServerSoap netiDAccessServerSoap;

    @InjectMocks
    private NiasCollectPollerImpl testee;

    @Mock
    private SecurityContext securityContext;

    @Before
    public void init() {
        WebCertUser webCertUser = mock(WebCertUser.class);
        when(webCertUser.getHsaId()).thenReturn(HSA_ID);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(webCertUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        testee.setOrderRef(ORDER_REF);
        testee.setTicketId(TICKET_ID);
        testee.setSecurityContext(securityContext);
    }

    @Test
    public void testPollCompleteResponse() {

        when(netiDAccessServerSoap.collect(ORDER_REF)).thenReturn(buildCollectResponse(HSA_ID));

        testee.run();
        verify(underskriftService, times(1)).netidSignature(eq(TICKET_ID), any(byte[].class), anyString());

        // when all is OK, the collect poller should NOT update the ticket, that's done inside the underskriftService
        verifyZeroInteractions(redisTicketTracker);
    }

    @Test
    public void testPollCompleteResponseForWrongHsaId() {

        when(netiDAccessServerSoap.collect(ORDER_REF)).thenReturn(buildCollectResponse(HSA_ID_2));

        testee.run();
        verifyZeroInteractions(underskriftService);
        verify(redisTicketTracker, times(1)).updateStatus(TICKET_ID, SignaturStatus.ERROR);
    }

    @Test
    public void testPollCompleteResponseForWrongHsaIdUpdatesBiljettWithError() {

        when(underskriftService.netidSignature(eq(TICKET_ID), any(byte[].class), anyString()))
                .thenThrow(new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, "some problem"));
        when(netiDAccessServerSoap.collect(ORDER_REF)).thenReturn(buildCollectResponse(HSA_ID));

        testee.run();
        verify(underskriftService, times(1)).netidSignature(eq(TICKET_ID), any(byte[].class), anyString());
        verify(redisTicketTracker, times(1)).updateStatus(TICKET_ID, SignaturStatus.ERROR);
    }

    @Test
    public void testPollCancelledResponse() {

        when(netiDAccessServerSoap.collect(ORDER_REF)).thenReturn(buildErrorCollectResponse(HSA_ID, "CANCELLED"));

        testee.run();
        verifyZeroInteractions(underskriftService);

        verify(redisTicketTracker, times(1)).updateStatus(TICKET_ID, SignaturStatus.AVBRUTEN);
    }

    @Test
    public void testPollUnknownUserUpdatesBiljettWithError() {

        when(netiDAccessServerSoap.collect(ORDER_REF)).thenReturn(buildErrorCollectResponse(HSA_ID, "UNKNOWN_USER"));

        testee.run();
        verifyZeroInteractions(underskriftService);

        verify(redisTicketTracker, times(1)).updateStatus(TICKET_ID, SignaturStatus.ERROR);
    }

    private ResultCollect buildErrorCollectResponse(String hsaId, String progressStatus) {
        ResultCollect rc = new ResultCollect();
        rc.setProgressStatus(progressStatus);
        rc.setSignature("signature");
        UserInfoType userInfo = new UserInfoType();
        userInfo.setPersonalNumber(hsaId);
        userInfo.setCertificate("certificate");
        rc.setUserInfo(userInfo);
        return rc;
    }

    private ResultCollect buildCollectResponse(String hsaId) {
        return buildErrorCollectResponse(hsaId, "COMPLETE");
    }
}
