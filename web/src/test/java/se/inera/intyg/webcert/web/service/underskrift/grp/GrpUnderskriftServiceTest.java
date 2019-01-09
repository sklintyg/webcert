/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import se.funktionstjanster.grp.v1.AuthenticateRequestType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.funktionstjanster.grp.v1.OrderResponseType;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.underskrift.grp.factory.GrpCollectPollerFactory;
import se.inera.intyg.webcert.web.service.underskrift.model.SignMethod;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturStatus;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2015-08-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class GrpUnderskriftServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final String INTYG_ID = "intyg-1";
    private static final long VERSION = 1L;
    private static final String PERSON_ID = "19121212-1212";
    private static final String TX_ID = "webcert-tx-1";
    private static final String ORDER_REF = "order-ref-1";
    private static final Long PAGAENDE_SIG_ID = 1L;

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    RedisTicketTracker redisTicketTracker;

    @Mock
    GrpServicePortType grpService;

    @Mock
    ThreadPoolTaskExecutor taskExecutor;

    @Mock
    GrpCollectPollerFactory grpCollectPollerFactory;

    @InjectMocks
    GrpUnderskriftServiceImpl grpSignaturService;


    @Test
    public void testSuccessfulAuthenticationRequest() throws GrpFault {
        when(grpCollectPollerFactory.getInstance()).thenReturn(mock(GrpCollectPoller.class));
        when(grpService.authenticate(any(AuthenticateRequestType.class))).thenReturn(buildOrderResponse());

        grpSignaturService.startGrpCollectPoller(PERSON_ID, buildSignaturBiljett());
        verify(taskExecutor, times(1)).execute(any(GrpCollectPoller.class), any(Long.class));
    }

//    @Test(expected = IllegalArgumentException.class)
//    public void testAuthenticateRequestFailsWhenUtkastIsNotFound() {
//        when(utkastRepository.findOne(INTYG_ID)).thenReturn(null);
//        try {
//            grpSignaturService.startGrpAuthentication(INTYG_ID, VERSION);
//        } finally {
//            verify(taskExecutor, times(0)).execute(any(GrpCollectPoller.class), any(Long.class));
//        }
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testAuthenticateRequestFailsWhenNoWebCertUserIsFound() {
//        when(utkastRepository.findOne(INTYG_ID)).thenReturn(buildUtkast());
//        when(webCertUserService.getUser()).thenReturn(null);
//        try {
//            grpSignaturService.startGrpAuthentication(INTYG_ID, VERSION);
//        } finally {
//            verify(taskExecutor, times(0)).execute(any(GrpCollectPoller.class), any(Long.class));
//        }
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testAuthenticateRequestFailsWhenWebCertUserHasNoPersonId() {
//
//        when(utkastRepository.findOne(INTYG_ID)).thenReturn(buildUtkast());
//        when(webCertUserService.getUser()).thenReturn(createUser());
//
//        try {
//            grpSignaturService.startGrpAuthentication(INTYG_ID, VERSION);
//        } finally {
//            verify(taskExecutor, times(0)).execute(any(GrpCollectPoller.class), any(Long.class));
//        }
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void testAuthenticateRequestThrowsExceptionWhenGrpCallFails() throws GrpFault {
//        when(webCertUserService.getUser()).thenReturn(webCertUser);
//        when(utkastRepository.findOne(INTYG_ID)).thenReturn(buildUtkast());
//        when(signaturService.createDraftHash(INTYG_ID, VERSION)).thenReturn(buildSignaturBiljett());
//        when(grpService.authenticate(any(AuthenticateRequestType.class))).thenThrow(new GrpFault("grp-fault"));
//
//        try {
//            grpSignaturService.startGrpAuthentication(INTYG_ID, VERSION);
//        } finally {
//            verify(redisTicketTracker, times(1)).updateStatus(TX_ID, SignaturStatus.OKAND);
//            verify(taskExecutor, times(0)).execute(any(GrpCollectPoller.class), any(Long.class));
//        }
//    }

    private OrderResponseType buildOrderResponse() {
        OrderResponseType resp = new OrderResponseType();
        resp.setTransactionId(TX_ID);
        resp.setOrderRef(ORDER_REF);
        return resp;
    }

    private SignaturBiljett buildSignaturBiljett() {
        SignaturBiljett ticket = SignaturBiljett.SignaturBiljettBuilder
                .aSignaturBiljett(TX_ID, SignaturTyp.PKCS7, SignMethod.GRP)
                .withHash("hash")
                .withSkapad(LocalDateTime.now())
                .withStatus(SignaturStatus.OKAND)
                .withVersion(VERSION)
                .withIntygsId(INTYG_ID)
                .build();
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
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));

        return user;
    }

}
