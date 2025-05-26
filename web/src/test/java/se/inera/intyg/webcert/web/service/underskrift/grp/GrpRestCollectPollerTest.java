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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.service.underskrift.grp.GrpRestCollectPollerImpl.STATUS_CANCELLED;
import static se.inera.intyg.webcert.web.service.underskrift.grp.GrpRestCollectPollerImpl.STATUS_COMPLETE;
import static se.inera.intyg.webcert.web.service.underskrift.grp.GrpRestCollectPollerImpl.STATUS_FAILED;
import static se.inera.intyg.webcert.web.service.underskrift.grp.GrpRestCollectPollerImpl.STATUS_PENDING;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationJunit5TestSetup;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpCollectResponse;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpProgressStatus;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpUserInfo;
import se.inera.intyg.webcert.web.service.underskrift.grp.dto.GrpValidationInfo;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class GrpRestCollectPollerTest extends AuthoritiesConfigurationJunit5TestSetup {

    @Mock
    private RedisTicketTracker redisTicketTracker;
    @Mock
    private UnderskriftService underskriftService;
    @Mock
    private GrpRestClient grpRestClient;

    @InjectMocks
    private GrpRestCollectPollerImpl grpCollectPoller;

    private static final int ONE = 1;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final String REF_ID = "refId";
    private static final String SIGNATURE = "signature";
    private static final String PERSON_ID = "191212121212";
    private static final String TRANSACTION_ID = "transactionId";

    private static final GrpCollectResponse RESPONSE_COMPLETED = GrpCollectResponse.builder()
        .progressStatus(GrpProgressStatus.builder().status(STATUS_COMPLETE).build())
        .userInfo(GrpUserInfo.builder().tin(PERSON_ID).build())
        .validationInfo(GrpValidationInfo.builder().signature(SIGNATURE).build())
        .build();

    private static final GrpCollectResponse RESPONSE_PENDING = GrpCollectResponse.builder()
        .progressStatus(GrpProgressStatus.builder()
            .status(STATUS_PENDING)
            .build())
        .build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(grpCollectPoller, "grpPollingTimeout", 24000L);
        ReflectionTestUtils.setField(grpCollectPoller, "pollingInterval", 50L);
        grpCollectPoller.setRefId(REF_ID);
        grpCollectPoller.setTransactionId(TRANSACTION_ID);
        grpCollectPoller.setSecurityContext(buildAuthentication());
    }

    @Test
    void shouldSignWhenProgressStatusComplete() {
        when(grpRestClient.collect(REF_ID, TRANSACTION_ID)).thenReturn(RESPONSE_COMPLETED);

        grpCollectPoller.run();
        verifyNoInteractions(redisTicketTracker);
        verify(grpRestClient, times(ONE)).collect(REF_ID, TRANSACTION_ID);
        verify(underskriftService, times(ONE)).grpSignature(TRANSACTION_ID, SIGNATURE.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldUpdateRedisTrackerWhenProgressStatusFailed() {
        final var collectResponse = GrpCollectResponse.builder()
            .progressStatus(GrpProgressStatus.builder().status(STATUS_FAILED).build()).build();

        when(grpRestClient.collect(REF_ID, TRANSACTION_ID)).thenReturn(collectResponse);

        grpCollectPoller.run();
        verifyNoInteractions(underskriftService);
        verify(grpRestClient, times(ONE)).collect(REF_ID, TRANSACTION_ID);
        verify(redisTicketTracker, times(ONE)).updateStatus(TRANSACTION_ID, SignaturStatus.OKAND);
    }

    @Test
    void shouldUpdateRedisTrackerWhenProgressStatusCanceled() {
        final var collectResponse = GrpCollectResponse.builder()
            .progressStatus(GrpProgressStatus.builder().status(STATUS_CANCELLED).build()).build();

        when(grpRestClient.collect(REF_ID, TRANSACTION_ID)).thenReturn(collectResponse);

        grpCollectPoller.run();
        verifyNoInteractions(underskriftService);
        verify(grpRestClient, times(ONE)).collect(REF_ID, TRANSACTION_ID);
        verify(redisTicketTracker, times(ONE)).updateStatus(TRANSACTION_ID, SignaturStatus.AVBRUTEN);
    }

    @Test
    void shouldUpdateRedisTrackerWhenProgressStatusPending() {
        ReflectionTestUtils.setField(grpCollectPoller, "grpPollingTimeout", 200);
        when(grpRestClient.collect(REF_ID, TRANSACTION_ID)).thenReturn(RESPONSE_PENDING);

        grpCollectPoller.run();
        verifyNoInteractions(underskriftService);
        verify(grpRestClient, atLeastOnce()).collect(REF_ID, TRANSACTION_ID);
        verify(redisTicketTracker, atLeastOnce()).updateStatus(TRANSACTION_ID, SignaturStatus.VANTA_SIGN);
    }

    @Test
    void shouldThrowIllegalStateWhenPersonIdsDontMatch() {
        final var collectResponse = GrpCollectResponse.builder()
            .progressStatus(GrpProgressStatus.builder().status(STATUS_COMPLETE).build())
            .userInfo(GrpUserInfo.builder().tin("201212121212").build())
            .validationInfo(GrpValidationInfo.builder().signature(SIGNATURE).build())
            .build();

        when(grpRestClient.collect(anyString(), anyString())).thenReturn(collectResponse);

        assertThrows(IllegalStateException.class, () -> grpCollectPoller.run());
    }

    @Test
    void shuoldExitWithoutInteractionsWhenGrpClientRequestReturnsNull() {
        when(grpRestClient.collect(REF_ID, TRANSACTION_ID)).thenReturn(null);

        grpCollectPoller.run();
        verify(grpRestClient, times(ONE)).collect(REF_ID, TRANSACTION_ID);
        verifyNoInteractions(redisTicketTracker);
        verifyNoInteractions(underskriftService);
    }

    @Test
    void shuoldHandleExpectedSequenceOfCollectResponses() {
        when(grpRestClient.collect(REF_ID, TRANSACTION_ID)).thenReturn(RESPONSE_PENDING, RESPONSE_PENDING, RESPONSE_PENDING,
            RESPONSE_COMPLETED);

        grpCollectPoller.run();
        verify(grpRestClient, times(FOUR)).collect(REF_ID, TRANSACTION_ID);
        verify(redisTicketTracker, times(THREE)).updateStatus(TRANSACTION_ID, SignaturStatus.VANTA_SIGN);
        verify(underskriftService, times(ONE)).grpSignature(TRANSACTION_ID, SIGNATURE.getBytes(StandardCharsets.UTF_8));
    }

    private SecurityContext buildAuthentication() {
        final var role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        final var user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setPersonId("19121212-1212");

        final var authentication = new TestingAuthenticationToken(user, null);
        final var securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        return securityContext;
    }

}
