/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.signatur.grp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import se.funktionstjanster.grp.v1.CollectRequestType;
import se.funktionstjanster.grp.v1.CollectResponseType;
import se.funktionstjanster.grp.v1.FaultStatusType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpFaultType;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.funktionstjanster.grp.v1.ProgressStatusType;
import se.funktionstjanster.grp.v1.Property;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.signatur.SignaturService;
import se.inera.intyg.webcert.web.service.signatur.SignaturTicketTracker;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.funktionstjanster.grp.v1.ProgressStatusType.COMPLETE;
import static se.funktionstjanster.grp.v1.ProgressStatusType.OUTSTANDING_TRANSACTION;
import static se.funktionstjanster.grp.v1.ProgressStatusType.STARTED;

/**
 * Created by eriklupander on 2015-08-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class GrpCollectPollerTest extends AuthoritiesConfigurationTestSetup {

    private static final String PERSON_ID = "19121212-1212";
    private static final String TX_ID = "webcert-tx-1";
    private static final String ORDER_REF = "order-ref-1";

    @Mock
    private SignaturService signaturService;

    @Mock
    private SignaturTicketTracker signaturTicketTracker;

    @Mock
    private GrpServicePortType grpService;

    @InjectMocks
    private GrpCollectPollerImpl grpCollectPoller;

    @Test
    public void testSingleSuccessfulCollect() throws GrpFault {

        when(grpService.collect(any(CollectRequestType.class))).thenReturn(buildResp(COMPLETE));

        grpCollectPoller.setOrderRef(ORDER_REF);
        grpCollectPoller.setTransactionId(TX_ID);
        grpCollectPoller.setSecurityContext(buildAuthentication());
        grpCollectPoller.setMs(50L);
        grpCollectPoller.run();

        verify(signaturService, times(1)).clientGrpSignature(anyString(), anyString(), any(WebCertUser.class));
        verify(signaturTicketTracker, times(0)).updateStatus(TX_ID, SignaturTicket.Status.OKAND);
    }

    @Test
    public void testSuccessfulCollectAfterTwoOngoingPlusOneComplete() throws GrpFault {

        when(grpService.collect(any(CollectRequestType.class))).thenReturn(
                buildResp(STARTED),
                buildResp(OUTSTANDING_TRANSACTION),
                buildResp(COMPLETE));
        grpCollectPoller.setOrderRef(ORDER_REF);
        grpCollectPoller.setTransactionId(TX_ID);
        grpCollectPoller.setSecurityContext(buildAuthentication());
        grpCollectPoller.setMs(50L);
        grpCollectPoller.run();

        verify(signaturService, times(1)).clientGrpSignature(anyString(), anyString(), any(WebCertUser.class));
        verify(signaturTicketTracker, times(0)).updateStatus(TX_ID, SignaturTicket.Status.OKAND);
    }

    @Test
    public void testCollectFailsOnGrpFaultWhenUserCancelled() throws GrpFault {

        when(grpService.collect(any(CollectRequestType.class))).thenThrow(buildFault(FaultStatusType.USER_CANCEL));
        grpCollectPoller.setOrderRef(ORDER_REF);
        grpCollectPoller.setTransactionId(TX_ID);
        grpCollectPoller.setSecurityContext(buildAuthentication());
        grpCollectPoller.setMs(50L);
        grpCollectPoller.run();

        verify(signaturService, times(0)).clientGrpSignature(anyString(), anyString(), any(WebCertUser.class));
        verify(signaturTicketTracker, times(1)).updateStatus(TX_ID, SignaturTicket.Status.OKAND);
    }

    @Test
    public void testCollectFailsOnGrpFaultWhenGrpTxExpires() throws GrpFault {

        when(grpService.collect(any(CollectRequestType.class))).thenThrow(buildFault(FaultStatusType.EXPIRED_TRANSACTION));
        grpCollectPoller.setOrderRef(ORDER_REF);
        grpCollectPoller.setTransactionId(TX_ID);
        grpCollectPoller.setSecurityContext(buildAuthentication());
        grpCollectPoller.setMs(50L);
        grpCollectPoller.run();

        verify(signaturService, times(0)).clientGrpSignature(anyString(), anyString(), any(WebCertUser.class));
        verify(signaturTicketTracker, times(1)).updateStatus(TX_ID, SignaturTicket.Status.OKAND);
    }

    private GrpFault buildFault(FaultStatusType faultStatusType) {
        GrpFaultType grpFaultType = new GrpFaultType();
        grpFaultType.setFaultStatus(faultStatusType);
        grpFaultType.setDetailedDescription("detailed-desc");
        GrpFault fault = new GrpFault("", grpFaultType);
        return fault;
    }

    private CollectResponseType buildResp(ProgressStatusType progressStatusType) {
        CollectResponseType resp = new CollectResponseType();
        resp.setProgressStatus(progressStatusType);
        Property p = new Property();
        p.setName("Subject.SerialNumber");
        p.setValue(PERSON_ID);
        resp.getAttributes().add(p);
        return resp;
    }

    private SecurityContext buildAuthentication() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));
        user.setPersonId(PERSON_ID);

        Authentication authentication = new TestingAuthenticationToken(user, null);
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        return securityContext;
    }

}
