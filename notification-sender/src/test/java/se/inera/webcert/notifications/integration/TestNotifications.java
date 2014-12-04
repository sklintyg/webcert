package se.inera.webcert.notifications.integration;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.inera.webcert.notifications.TestDataUtil;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-notification-sender-config.xml")
@ActiveProfiles( profiles = "integration" )
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TestNotifications {


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
    public void test() throws InterruptedException {
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-skapat-notification.xml");
        sendMessage(requestPayload);
        assertIsSatisfied(camelContext);
    }

}
