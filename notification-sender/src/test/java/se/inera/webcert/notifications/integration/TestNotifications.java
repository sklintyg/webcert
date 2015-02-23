package se.inera.webcert.notifications.integration;

import static com.jayway.awaitility.Awaitility.await;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.modules.support.api.notification.FragaSvar;
import se.inera.certificate.modules.support.api.notification.HandelseType;
import se.inera.certificate.modules.support.api.notification.NotificationMessage;
import se.inera.webcert.notifications.stub.CertificateStatusUpdateForCareResponderStub;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/test-notification-sender-config.xml", "/spring/test-service-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TestNotifications {

    private static final int MESSAGES_EXPECTED = 3;

    private static final int SECONDS_TO_WAIT = 10;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private Queue queue;

    @Autowired
    private CertificateStatusUpdateForCareResponderStub certificateStatusUpdateForCareResponderStub;

    @Before
    public void resetStub() {
        this.certificateStatusUpdateForCareResponderStub.reset();
    }

    private void sendMessage(final NotificationMessage message) {
        jmsTemplate.send(queue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(message);
            }
        });
    }

    @Test
    public void ensureStubReceivedAllMessages() throws InterruptedException {
        NotificationMessage notificationMessage1 = new NotificationMessage("intyg1", "FK7263", new LocalDateTime(),
                HandelseType.INTYGSUTKAST_RADERAT, "address2", "{ }", new FragaSvar(0, 0, 0, 0));
        sendMessage(notificationMessage1);
        NotificationMessage notificationMessage2 = new NotificationMessage("intyg2", "FK7263", new LocalDateTime(),
                HandelseType.INTYGSUTKAST_SIGNERAT, "address2", "{ }", new FragaSvar(0, 0, 0, 0));
        sendMessage(notificationMessage2);
        NotificationMessage notificationMessage3 = new NotificationMessage("intyg3", "FK7263", new LocalDateTime(),
                HandelseType.INTYGSUTKAST_SKAPAT, "address2", "{ }", new FragaSvar(0, 0, 0, 0));
        sendMessage(notificationMessage3);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int numberOfReceivedMessages = certificateStatusUpdateForCareResponderStub.getNumberOfReceivedMessages();
                return (numberOfReceivedMessages == MESSAGES_EXPECTED);
            }
        });
        Map<String, CertificateStatusUpdateForCareType> exchange = certificateStatusUpdateForCareResponderStub.getExchange();

//        assertEquals("Expected INTYGSUTKAST_RADERAT (HAN4) for intyg1", HandelsekodKodRestriktion.HAN_4.value(), exchange.get("intyg1"));

//        assertEquals("Expected INTYG_SIGNERAT (HAN2) for intyg2", HandelsekodKodRestriktion.HAN_2.value(), exchange.get("intyg2"));

//        assertEquals("Expected INTYGSUTKAST_SKAPAT (HAN1) for intyg3", HandelsekodKodRestriktion.HAN_1.value(), exchange.get("intyg3"));
    }
}
