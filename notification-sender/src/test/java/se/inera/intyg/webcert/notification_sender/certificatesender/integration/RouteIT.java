/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

import static com.jayway.awaitility.Awaitility.await;

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import javax.jms.Queue;
import javax.jms.TextMessage;

import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.google.common.base.Throwables;

import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.notification_sender.certificatesender.services.mock.MockSendCertificateServiceClientImpl;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/certificates/integration-test-certificate-sender-config.xml")
@BootstrapWith(CamelTestContextBootstrapper.class)
@TestExecutionListeners(listeners = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class }) // Suppresses warning
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

    @Before
    public void resetStub() {
        sendCertificateServiceClient.reset();
    }

    @Test
    public void ensureStubReceivesAllMessages() throws Exception {
        sendMessage(INTYGS_ID_1, Constants.SEND_MESSAGE);
        sendMessage(INTYGS_ID_1, Constants.SEND_MESSAGE);
        sendMessage(INTYGS_ID_1, Constants.SEND_MESSAGE);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = sendCertificateServiceClient.getNumberOfReceivedMessages();
            return (numberOfReceivedMessages == 3);
        });
    }

    @Test
    public void ensureStubReceivesAllMessagesAfterResend() throws Exception {
        sendMessage(MockSendCertificateServiceClientImpl.FALLERAT_MEDDELANDE + "2", Constants.SEND_MESSAGE);
        sendMessage(INTYGS_ID_1, Constants.SEND_MESSAGE);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfSentMessages = sendCertificateServiceClient.getNumberOfSentMessages();
            return (numberOfSentMessages == 2);
        });
    }

    @Test
    public void ensureMessageEndsUpInDLQ() throws Exception {
        sendMessage(MockSendCertificateServiceClientImpl.FALLERAT_MEDDELANDE + "5", Constants.SEND_MESSAGE);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfDLQMessages = numberOfDLQMessages();
            return (numberOfDLQMessages == 1);
        });
    }

    private void sendMessage(final String intygsId, final String messageType) throws Exception {
        jmsTemplate.send(sendQueue, (MessageCreator) session -> {
            try {
                TextMessage textMessage = session.createTextMessage("body");
                textMessage.setStringProperty(Constants.INTYGS_ID, intygsId);
                textMessage.setStringProperty(Constants.MESSAGE_TYPE, messageType);
                return textMessage;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }

    private int numberOfDLQMessages() throws Exception {
        Integer count = jmsTemplate.browse(dlq,
                (session, browser) -> {
                    int counter = 0;
                    Enumeration<?> msgs = browser.getEnumeration();
                    while (msgs.hasMoreElements()) {
                        msgs.nextElement();
                        counter++;
                    }
                    return counter;
                });
        return count;
    }

}
