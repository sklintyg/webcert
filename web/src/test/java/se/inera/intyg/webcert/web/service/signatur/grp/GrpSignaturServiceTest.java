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
package se.inera.intyg.webcert.web.service.signatur.grp;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import se.funktionstjanster.grp.v1.AuthenticateRequestType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.funktionstjanster.grp.v1.OrderResponseType;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.common.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.signatur.SignaturService;
import se.inera.intyg.webcert.web.service.signatur.SignaturTicketTracker;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.signatur.grp.factory.GrpCollectPollerFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

/**
 * Created by eriklupander on 2015-08-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class GrpSignaturServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final String INTYG_ID = "intyg-1";
    private static final long VERSION = 1L;
    private static final String PERSON_ID = "19121212-1212";
    private static final String TX_ID = "webcert-tx-1";
    private static final String ORDER_REF = "order-ref-1";

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    SignaturService signaturService;

    @Mock
    SignaturTicketTracker signaturTicketTracker;

    @Mock
    GrpServicePortType grpService;

    @Mock
    UtkastRepository utkastRepository;

    @Mock
    ThreadPoolTaskExecutor taskExecutor;

    @Mock
    GrpCollectPollerFactory grpCollectPollerFactory;

    @InjectMocks
    GrpSignaturServiceImpl grpSignaturService;

    private WebCertUser webCertUser;

    @Before
    public void setupTest() {
        webCertUser = createUser();
        webCertUser.setPersonId(PERSON_ID);
    }

    @Test
    public void testSuccessfulAuthenticationRequest() throws GrpFault {
        when(grpCollectPollerFactory.getInstance()).thenReturn(mock(GrpCollectPoller.class));
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(utkastRepository.findOne(INTYG_ID)).thenReturn(buildUtkast());
        when(signaturService.createDraftHash(INTYG_ID, VERSION)).thenReturn(buildSignaturTicket());
        when(grpService.authenticate(any(AuthenticateRequestType.class))).thenReturn(buildOrderResponse());

        grpSignaturService.startGrpAuthentication(INTYG_ID, VERSION);
        verify(taskExecutor, times(1)).execute(any(GrpCollectPoller.class), any(Long.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateRequestFailsWhenUtkastIsNotFound() {
        when(utkastRepository.findOne(INTYG_ID)).thenReturn(null);
        try {
            grpSignaturService.startGrpAuthentication(INTYG_ID, VERSION);
        } finally {
            verify(taskExecutor, times(0)).execute(any(GrpCollectPoller.class), any(Long.class));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateRequestFailsWhenNoWebCertUserIsFound() {
        when(utkastRepository.findOne(INTYG_ID)).thenReturn(buildUtkast());
        when(webCertUserService.getUser()).thenReturn(null);
        try {
            grpSignaturService.startGrpAuthentication(INTYG_ID, VERSION);
        } finally {
            verify(taskExecutor, times(0)).execute(any(GrpCollectPoller.class), any(Long.class));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAuthenticateRequestFailsWhenWebCertUserHasNoPersonId() {

        when(utkastRepository.findOne(INTYG_ID)).thenReturn(buildUtkast());
        when(webCertUserService.getUser()).thenReturn(createUser());

        try {
            grpSignaturService.startGrpAuthentication(INTYG_ID, VERSION);
        } finally {
            verify(taskExecutor, times(0)).execute(any(GrpCollectPoller.class), any(Long.class));
        }
    }

    @Test(expected = RuntimeException.class)
    public void testAuthenticateRequestThrowsExceptionWhenGrpCallFails() throws GrpFault {
        when(webCertUserService.getUser()).thenReturn(webCertUser);
        when(utkastRepository.findOne(INTYG_ID)).thenReturn(buildUtkast());
        when(signaturService.createDraftHash(INTYG_ID, VERSION)).thenReturn(buildSignaturTicket());
        when(grpService.authenticate(any(AuthenticateRequestType.class))).thenThrow(new GrpFault("grp-fault"));

        try {
            grpSignaturService.startGrpAuthentication(INTYG_ID, VERSION);
        } finally {
            verify(signaturTicketTracker, times(1)).updateStatus(TX_ID, SignaturTicket.Status.OKAND);
            verify(taskExecutor, times(0)).execute(any(GrpCollectPoller.class), any(Long.class));
        }
    }

    private OrderResponseType buildOrderResponse() {
        OrderResponseType resp = new OrderResponseType();
        resp.setTransactionId(TX_ID);
        resp.setOrderRef(ORDER_REF);
        return resp;
    }

    private SignaturTicket buildSignaturTicket() {
        SignaturTicket ticket = new SignaturTicket(TX_ID, SignaturTicket.Status.OKAND, INTYG_ID, VERSION, null, "hash", LocalDateTime.now());
        return ticket;
    }

    private Utkast buildUtkast() {
        Utkast utkast = new Utkast();
        utkast.setIntygsId(INTYG_ID);
        utkast.setIntygsTyp("fk7263");
        utkast.setStatus(UtkastStatus.DRAFT_COMPLETE);
        utkast.setVersion(VERSION);
        return utkast;
    }

    private WebCertUser createUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));

        return user;
    }

}
