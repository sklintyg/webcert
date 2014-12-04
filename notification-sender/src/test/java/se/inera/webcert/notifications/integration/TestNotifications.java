package se.inera.webcert.notifications.integration;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.webcert.notifications.TestDataUtil;
import se.inera.webcert.notifications.service.CertificateStatusUpdateServiceImpl;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-notification-sender-config.xml")
@ActiveProfiles( profiles = {"integration", "dev"} )
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TestNotifications {


    private static final String LOGICAL_ADDRESS = "1234567890";

    @Autowired
    CertificateStatusUpdateForCareResponderInterface mockCertificateStatusUpdateForCareResponder;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private CamelContext camelContext;
    
    @Autowired
    private Queue queue;

    private void sendMessage(final String message) {
        jmsTemplate.send(queue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(message);
            }
        });
    }

    @Test
    public void testUtkastSkapat() throws InterruptedException {
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-raderat-notification.xml");
        sendMessage(requestPayload);
        when(mockCertificateStatusUpdateForCareResponder.certificateStatusUpdateForCare(Mockito.anyString(), Mockito.any(CertificateStatusUpdateForCareType.class)))
            .thenReturn(certificateStatusUpdateForCareResponse(ResultTypeUtil.okResult()));

        Mockito.verify(mockCertificateStatusUpdateForCareResponder).certificateStatusUpdateForCare(Mockito.anyString(), Mockito.any(CertificateStatusUpdateForCareType.class));

        assertIsSatisfied(camelContext);
    }

//    @Test
//    public void testUtkastSignerat() throws InterruptedException {
//        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");
//
//        ArgumentCaptor<CertificateStatusUpdateForCareType> capture = ArgumentCaptor.forClass(CertificateStatusUpdateForCareType.class);
//
//        sendMessage(requestPayload);
//
//        Mockito.verify(mockCertificateStatusUpdateForCareResponder).certificateStatusUpdateForCare(eq(LOGICAL_ADDRESS), capture.capture());
//
//        CertificateStatusUpdateForCareType request = capture.getValue();
//        System.out.println();
//        System.out.println(request.getUtlatande().getUtlatandeId());
//        System.out.println();
//        assertIsSatisfied(camelContext);
//    }

    private CertificateStatusUpdateForCareResponseType certificateStatusUpdateForCareResponse(ResultType result) {
        CertificateStatusUpdateForCareResponseType response = new CertificateStatusUpdateForCareResponseType();
        response.setResult(result);
        return response;
    }
    
}
