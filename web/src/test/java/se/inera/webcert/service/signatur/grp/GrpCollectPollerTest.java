package se.inera.webcert.service.signatur.grp;

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
import org.springframework.security.core.GrantedAuthority;
import se.funktionstjanster.grp.v1.CollectRequestType;
import se.funktionstjanster.grp.v1.CollectResponseType;
import se.funktionstjanster.grp.v1.FaultStatusType;
import se.funktionstjanster.grp.v1.GrpFault;
import se.funktionstjanster.grp.v1.GrpFaultType;
import se.funktionstjanster.grp.v1.GrpServicePortType;
import se.funktionstjanster.grp.v1.ProgressStatusType;
import se.funktionstjanster.grp.v1.Property;
import se.inera.webcert.service.signatur.SignaturService;
import se.inera.webcert.service.signatur.SignaturTicketTracker;
import se.inera.webcert.service.signatur.dto.SignaturTicket;
import se.inera.webcert.service.user.dto.WebCertUser;

import java.util.ArrayList;

/**
 * Created by eriklupander on 2015-08-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class GrpCollectPollerTest {

    private static final String INTYG_ID = "intyg-1";
    private static final long VERSION = 1L;
    private static final String PERSON_ID = "19121212-1212";
    private static final String TX_ID = "webcert-tx-1";
    private static final String ORDER_REF = "order-ref-1";

    @Mock
    SignaturService signaturService;

    @Mock
    SignaturTicketTracker signaturTicketTracker;

    @Mock
    GrpServicePortType grpService;

    @InjectMocks
    GrpCollectPollerImpl grpCollectPoller;

    @Test
    public void testSingleSuccessfulCollect() throws GrpFault {

        when(grpService.collect(any(CollectRequestType.class))).thenReturn(buildResp(COMPLETE));
        //GrpPoller grpPoller = new GrpPoller(ORDER_REF, TX_ID, "policy", "text", buildWebCertUser(), grpService, signaturTicketTracker, signaturService);

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
                buildResp(COMPLETE)
        );
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
        WebCertUser webCertUser = new WebCertUser(new ArrayList<GrantedAuthority>());
        webCertUser.setPersonId(PERSON_ID);
        return webCertUser;
    }

}
