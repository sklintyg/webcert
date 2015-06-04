package se.inera.webcert.certificatesender.integration;

import static com.jayway.awaitility.Awaitility.await;
import static se.inera.webcert.notifications.service.CertificateStatusUpdateForCareResponderStub.FALLERAT_MEDDELANDE;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.jms.*;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.certificate.modules.support.api.notification.FragorOchSvar;
import se.inera.certificate.modules.support.api.notification.HandelseType;
import se.inera.certificate.modules.support.api.notification.NotificationMessage;
import se.inera.webcert.certificatesender.services.mock.MockSendCertificateServiceClientImpl;
import se.inera.webcert.common.Constants;
import se.inera.webcert.notifications.service.CertificateStatusUpdateForCareResponderStub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/certificates/integration-test-certificate-sender-config.xml")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RouteIntegrationTest {
    private static final Logger LOG = LoggerFactory.getLogger(RouteIntegrationTest.class);

    private static final int SECONDS_TO_WAIT = 10;

    private static final String INTYGS_ID_1 = "intygsId1";

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private Queue queue;

    @Autowired
    MockSendCertificateServiceClientImpl sendCertificateServiceClient;

    @Test
    public void ensureStubReceivedAllMessages() throws Exception {
        sendMessage("notificationMessage1", INTYGS_ID_1, Constants.SEND_MESSAGE);
        sendMessage("notificationMessage2", INTYGS_ID_1, Constants.SEND_MESSAGE);
        sendMessage("notificationMessage3", INTYGS_ID_1, Constants.SEND_MESSAGE);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int numberOfReceivedMessages = sendCertificateServiceClient.getNumberOfReceivedMessages();
                System.out.println("numberOfReceivedMessages: " + numberOfReceivedMessages);
                return (numberOfReceivedMessages == 3);
            }
        });
    }

    @Test
    public void ensureStubReceivedAllMessages() throws Exception {
        sendMessage("notificationMessage1", INTYGS_ID_1, Constants.SEND_MESSAGE);
        sendMessage("notificationMessage2", INTYGS_ID_1, Constants.SEND_MESSAGE);
        sendMessage("notificationMessage3", INTYGS_ID_1, Constants.SEND_MESSAGE);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int numberOfReceivedMessages = sendCertificateServiceClient.getNumberOfReceivedMessages();
                System.out.println("numberOfReceivedMessages: " + numberOfReceivedMessages);
                return (numberOfReceivedMessages == 3);
            }
        });
    }

    private void sendMessage(final String message, final String groupId, final String messageType) throws Exception {
        jmsTemplate.send(queue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                try {
                    TextMessage textMessage = session.createTextMessage(message);
                    textMessage.setStringProperty("JMSXGroupID", groupId);
                    textMessage.setStringProperty(Constants.MESSAGE_TYPE, messageType);
                    return textMessage;
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        });
    }

}
