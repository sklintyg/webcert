package se.inera.intyg.webcert.web.service.signatur.grp;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.funktionstjanster.grp.v1.ProgressStatusType.COMPLETE;
import static se.funktionstjanster.grp.v1.ProgressStatusType.OUTSTANDING_TRANSACTION;
import static se.funktionstjanster.grp.v1.ProgressStatusType.STARTED;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.funktionstjanster.grp.v1.CollectRequestType;
import se.funktionstjanster.grp.v1.CollectResponseType;
import se.funktionstjanster.grp.v1.FaultStatusType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpFaultType;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.funktionstjanster.grp.v1.ProgressStatusType;
import se.funktionstjanster.grp.v1.Property;
import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.signatur.SignaturService;
import se.inera.intyg.webcert.web.service.signatur.SignaturTicketTracker;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

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
        grpCollectPoller.setWebCertUser(buildWebCertUser());
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
        grpCollectPoller.setWebCertUser(buildWebCertUser());
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
        grpCollectPoller.setWebCertUser(buildWebCertUser());
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
        grpCollectPoller.setWebCertUser(buildWebCertUser());
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

    private WebCertUser buildWebCertUser() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
        user.setPersonId(PERSON_ID);

        return user;
    }

}
