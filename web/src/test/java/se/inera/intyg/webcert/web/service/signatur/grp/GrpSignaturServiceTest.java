package se.inera.intyg.webcert.web.service.signatur.grp;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.joda.time.LocalDateTime;
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
import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.UtkastStatus;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.signatur.SignaturService;
import se.inera.intyg.webcert.web.service.signatur.SignaturTicketTracker;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.signatur.grp.factory.GrpCollectPollerFactory;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by eriklupander on 2015-08-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class GrpSignaturServiceTest {

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
        WebCertUser user = new WebCertUser();
        user.setRoles(getGrantedRole());
        user.setAuthorities(getGrantedPrivileges());
        return user;
    }

    private Map<String, UserRole> getGrantedRole() {
        Map<String, UserRole> map = new HashMap<>();
        map.put(UserRole.ROLE_LAKARE.name(), UserRole.ROLE_LAKARE);
        return map;
    }

    private Map<String, UserPrivilege> getGrantedPrivileges() {
        List<UserPrivilege> list = Arrays.asList(UserPrivilege.values());

        // convert list to map
        Map<String, UserPrivilege> privilegeMap = Maps.uniqueIndex(list, new Function<UserPrivilege, String>() {
            @Override
            public String apply(UserPrivilege userPrivilege) {
                return userPrivilege.name();
            }
        });

        return privilegeMap;
    }

}
