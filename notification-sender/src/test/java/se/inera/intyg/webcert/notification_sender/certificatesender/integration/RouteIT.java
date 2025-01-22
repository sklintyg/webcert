/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.certificatesender.integration;

import static org.awaitility.Awaitility.await;

import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.mock.MockSendCertificateServiceClientImpl;
import se.inera.intyg.webcert.notification_sender.certificatesender.testconfig.CertificateCamelIntegrationTestConfig;

@CamelSpringTest
@ContextConfiguration(classes = CertificateCamelIntegrationTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RouteIT {

    private static final int SECONDS_TO_WAIT = 20;

    private static final String INTYGS_ID_1 = "intygsId1";

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("certificateQueue")
    private Queue sendQueue;

    @Autowired
    private Queue dlq;

    @Autowired
    private MockSendCertificateServiceClientImpl sendCertificateServiceClient;

    @BeforeEach
    public void resetStub() {
        sendCertificateServiceClient.reset();
    }

    @Test
    public void ensureStubReceivesAllMessages() {
        sendMessage(INTYGS_ID_1);
        sendMessage(INTYGS_ID_1);
        sendMessage(INTYGS_ID_1);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = sendCertificateServiceClient.getNumberOfReceivedMessages();
            return (numberOfReceivedMessages == 3);
        });
    }

    @Test
    public void ensureStubReceivesAllMessagesAfterResend() {
        sendMessage(MockSendCertificateServiceClientImpl.FALLERAT_MEDDELANDE + "2");
        sendMessage(INTYGS_ID_1);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfSentMessages = sendCertificateServiceClient.getNumberOfSentMessages();
            return (numberOfSentMessages == 2);
        });
    }

    @Test
    public void ensureMessageEndsUpInDLQ() {
        sendMessage(MockSendCertificateServiceClientImpl.FALLERAT_MEDDELANDE + "5");

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfDLQMessages = numberOfDLQMessages();
            return (numberOfDLQMessages == 1);
        });
    }

    private void sendMessage(final String intygsId) {
        jmsTemplate.send(sendQueue, session -> {
            try {
                TextMessage textMessage = session.createTextMessage("body");
                textMessage.setStringProperty(Constants.INTYGS_ID, intygsId);
                textMessage.setStringProperty(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE);
                textMessage.setStringProperty("DELAY_MESSAGE", "true");
                return textMessage;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Integer numberOfDLQMessages() {
        return jmsTemplate.browse(dlq, (session, browser) -> {
            int counter = 0;
            Enumeration<?> msgs = browser.getEnumeration();
            while (msgs.hasMoreElements()) {
                msgs.nextElement();
                counter++;
            }
            return counter;
        });
    }

}
