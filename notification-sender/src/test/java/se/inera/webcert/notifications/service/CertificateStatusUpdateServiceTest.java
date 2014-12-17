package se.inera.webcert.notifications.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.ws.WebServiceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;

@RunWith(MockitoJUnitRunner.class)
public class CertificateStatusUpdateServiceTest {

    private static final String INTYGS_ID = "abc1234";

    private static final String LOGICAL_ADDR = "SE123456789";

    @Mock
    CertificateStatusUpdateForCareResponderInterface mockClient;
    
    @InjectMocks
    CertificateStatusUpdateServiceImpl service;
    
    @Test(expected = WebServiceException.class)
    public void test() throws Exception {
        
        WebServiceException wse = new WebServiceException("This is the wse", new IOException("This is the ioe"));
        when(mockClient.certificateStatusUpdateForCare(eq(LOGICAL_ADDR), any(CertificateStatusUpdateForCareType.class))).thenThrow(wse);
                
        CertificateStatusUpdateForCareType request = new CertificateStatusUpdateForCareType();
        service.sendStatusUpdate(INTYGS_ID, request, LOGICAL_ADDR);
        
    }
    
}
