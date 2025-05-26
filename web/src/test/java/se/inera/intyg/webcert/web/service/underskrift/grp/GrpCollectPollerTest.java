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

import static com.mobilityguard.grp.service.v2.ProgressStatusType.COMPLETE;
import static com.mobilityguard.grp.service.v2.ProgressStatusType.OUTSTANDING_TRANSACTION;
import static com.mobilityguard.grp.service.v2.ProgressStatusType.STARTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mobilityguard.grp.service.v2.CollectRequestType;
import com.mobilityguard.grp.service.v2.CollectResponseType;
import com.mobilityguard.grp.service.v2.FaultStatusType;
import com.mobilityguard.grp.service.v2.GrpFaultType;
import com.mobilityguard.grp.service.v2.ProgressStatusType;
import com.mobilityguard.grp.service.v2.Property;
import com.mobilityguard.grp.service.v2.ValidationInfoType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import se.funktionstjanster.grp.v2.GrpException;
import se.funktionstjanster.grp.v2.GrpServicePortType;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.underskrift.UnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class GrpCollectPollerTest extends AuthoritiesConfigurationTestSetup {

    private static final String PERSON_ID = "19121212-1212";
    private static final String TX_ID = "webcert-tx-1";
    private static final String ORDER_REF = "order-ref-1";

    @Mock
    private UnderskriftService underskriftService;
    @Mock
    private RedisTicketTracker redisTicketTracker;
    @Mock
    private GrpServicePortType grpService;

    @InjectMocks
    private GrpCollectPollerImpl grpCollectPoller;

    @Test
    public void testSingleSuccessfulCollect() throws GrpException {

        when(grpService.collect(any(CollectRequestType.class))).thenReturn(buildResp(COMPLETE));

        grpCollectPoller.setRefId(ORDER_REF);
        grpCollectPoller.setTransactionId(TX_ID);
        grpCollectPoller.setSecurityContext(buildAuthentication());
        grpCollectPoller.setMs(50L);
        grpCollectPoller.run();

        verify(underskriftService, times(1)).grpSignature(anyString(), any(byte[].class));
        verify(redisTicketTracker, times(0)).updateStatus(TX_ID, SignaturStatus.OKAND);
    }

    @Test
    public void testSuccessfulCollectAfterTwoOngoingPlusOneComplete() throws GrpException {

        when(grpService.collect(any(CollectRequestType.class))).thenReturn(
            buildResp(STARTED),
            buildResp(OUTSTANDING_TRANSACTION),
            buildResp(COMPLETE));
        grpCollectPoller.setRefId(ORDER_REF);
        grpCollectPoller.setTransactionId(TX_ID);
        grpCollectPoller.setSecurityContext(buildAuthentication());
        grpCollectPoller.setMs(50L);
        grpCollectPoller.run();

        verify(underskriftService, times(1)).grpSignature(anyString(), any(byte[].class));
        verify(redisTicketTracker, times(0)).updateStatus(TX_ID, SignaturStatus.OKAND);
    }

    @Test
    public void testCollectFailsOnGrpFaultWhenUserCancelled() throws GrpException {

        when(grpService.collect(any(CollectRequestType.class))).thenThrow(buildException(FaultStatusType.USER_CANCEL));
        grpCollectPoller.setRefId(ORDER_REF);
        grpCollectPoller.setTransactionId(TX_ID);
        grpCollectPoller.setSecurityContext(buildAuthentication());
        grpCollectPoller.setMs(50L);
        grpCollectPoller.run();

        verify(underskriftService, times(0)).grpSignature(anyString(), any(byte[].class));
        verify(redisTicketTracker, times(1)).updateStatus(TX_ID, SignaturStatus.OKAND);
    }

    @Test
    public void testCollectFailsOnGrpFaultWhenGrpTxExpires() throws GrpException {

        when(grpService.collect(any(CollectRequestType.class))).thenThrow(buildException(FaultStatusType.EXPIRED_TRANSACTION));
        grpCollectPoller.setRefId(ORDER_REF);
        grpCollectPoller.setTransactionId(TX_ID);
        grpCollectPoller.setSecurityContext(buildAuthentication());
        grpCollectPoller.setMs(50L);
        grpCollectPoller.run();

        verify(underskriftService, times(0)).grpSignature(anyString(), any(byte[].class));
        verify(redisTicketTracker, times(1)).updateStatus(TX_ID, SignaturStatus.OKAND);
    }

    private GrpException buildException(FaultStatusType faultStatusType) {
        final var grpFaultType = new GrpFaultType();
        grpFaultType.setFaultStatus(faultStatusType);
        grpFaultType.setDetailedDescription("detailed-desc");
        return new GrpException("", grpFaultType);
    }

    private CollectResponseType buildResp(ProgressStatusType progressStatusType) {
        final var resp = new CollectResponseType();
        resp.setProgressStatus(progressStatusType);
        Property p = new Property();
        p.setName("Subject.SerialNumber");
        p.setValue(PERSON_ID);
        resp.getAttributes().add(p);
        final var validationInfoType = new ValidationInfoType();
        validationInfoType.setSignature("signature");
        resp.setValidationInfo(validationInfoType);
        return resp;
    }

    private SecurityContext buildAuthentication() {
        final var role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        final var user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setPersonId(PERSON_ID);

        final var authentication = new TestingAuthenticationToken(user, null);
        final var securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        return securityContext;
    }

}