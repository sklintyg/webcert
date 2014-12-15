package se.inera.webcert.notifications.integration;

import static com.jayway.awaitility.Awaitility.await;
import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;
import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HandelsekodCodeRestrictionType;
import se.inera.webcert.notifications.TestDataUtil;
import se.inera.webcert.notifications.stub.CertificateStatusUpdateForCareResponderStub;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-notification-sender-config.xml")
@ActiveProfiles(profiles = { "integration", "dev" })
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

    private void sendMessage(final String message) {
        jmsTemplate.send(queue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createTextMessage(message);
            }
        });
    }

    @Test
    public void testRaderatSkapat() throws InterruptedException {
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-raderat-notification.xml");
        sendMessage(requestPayload);
        assertIsSatisfied(camelContext);
    }

    @Test
    public void testSigneratSkapat() throws InterruptedException {
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");
        sendMessage(requestPayload);
        assertIsSatisfied(camelContext);
    }

    @Test
    public void testUtkastSkapat() throws InterruptedException {
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-skapat-notification.xml");
        sendMessage(requestPayload);
        assertIsSatisfied(camelContext);
    }

    @Test
    public void ensureStubReceivedAllMessages() throws InterruptedException {
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-raderat-notification.xml");
        sendMessage(requestPayload);
        String requestPayload2 = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");
        sendMessage(requestPayload2);
        String requestPayload3 = TestDataUtil.readRequestFromFile("data/intygsutkast-skapat-notification.xml");
        sendMessage(requestPayload3);
        assertIsSatisfied(camelContext);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int numberOfReceivedMessages = certificateStatusUpdateForCareResponderStub.getNumberOfReceivedMessages();
                return (numberOfReceivedMessages == MESSAGES_EXPECTED);
            }
        });
        Map<String, HandelsekodCodeRestrictionType> exchange = certificateStatusUpdateForCareResponderStub.getExchange();

        assertEquals("Expected INTYGSUTKAST_RADERAT for intyg-4", exchange.get("intyg-4"), HandelsekodCodeRestrictionType.HAN_4);

        assertEquals("Expected INTYG_SIGNERAT for intyg-2", exchange.get("intyg-2"), HandelsekodCodeRestrictionType.HAN_2);

        assertEquals("Expected INTYGSUTKAST_SKAPAT for intyg-1", exchange.get("intyg-1"), HandelsekodCodeRestrictionType.HAN_1);
    }
}
