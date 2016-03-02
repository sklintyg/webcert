/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.logsender.integration;

import static com.jayway.awaitility.Awaitility.await;

import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import se.inera.intyg.common.logmessages.ActivityType;
import se.inera.intyg.common.logmessages.type.LogMessageConstants;
import se.inera.intyg.common.logmessages.type.LogMessageType;
import se.inera.intyg.webcert.logsender.client.mock.MockLogSenderClientClientImpl;
import se.inera.intyg.webcert.logsender.helper.TestDataHelper;

import com.google.common.base.Throwables;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/logsender/integration-test-certificate-sender-config.xml")
@BootstrapWith(CamelTestContextBootstrapper.class)
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class}) // Suppresses warning
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RouteIntegrationTest {

    private static final int SECONDS_TO_WAIT = 10;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("newLogMessageQueue")
    private Queue sendQueue;

    @Autowired
    private Queue dlq;

    @Autowired
    private MockLogSenderClientClientImpl mockLogSenderClientClient;

    @Autowired
    @Qualifier("webcertLogMessageSender")
    CamelContext camelContext;

    @Before
    public void resetStub() throws Exception {
        mockLogSenderClientClient.reset();

        // Stopping and startng the camelContext clears out the aggregator between tests.
        // Not too elegant but works. stopRoute/startRoute does not work.
        camelContext.stop();
        camelContext.start();
    }

    @Test
    public void ensureStubReceivesOneMessageAfterSixHasBeenSent() throws Exception {

        for (int a = 0; a < 6; a++) {
            sendMessage(ActivityType.READ);
        }

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int numberOfReceivedMessages = mockLogSenderClientClient.getNumberOfReceivedMessages();
                System.out.println("numberOfReceivedMessages: " + numberOfReceivedMessages);
                return (numberOfReceivedMessages == 1);
            }
        });
    }

    @Test
    public void ensureStubReceivesTwoMessagesAfterTenHasBeenSent() throws Exception {

        for (int a = 0; a < 10; a++) {
            sendMessage(ActivityType.READ);
        }

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int numberOfReceivedMessages = mockLogSenderClientClient.getNumberOfReceivedMessages();
                System.out.println("numberOfReceivedMessages: " + numberOfReceivedMessages);
                return (numberOfReceivedMessages == 2);
            }
        });
    }

    @Test
    public void ensureStubReceivesZeroMessagesAfterThreeHasBeenSent() throws Exception {

        for (int a = 0; a < 3; a++) {
            sendMessage(ActivityType.READ);
        }

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int numberOfReceivedMessages = mockLogSenderClientClient.getNumberOfReceivedMessages();
                System.out.println("numberOfReceivedMessages: " + numberOfReceivedMessages);
                return (numberOfReceivedMessages == 0);
            }
        });
    }

//    @Test
//    public void ensureStubReceivesAllMessagesAfterResend() throws Exception {
//        sendMessage(ActivityType.EMERGENCY_ACCESS);
//        sendMessage(ActivityType.EMERGENCY_ACCESS);
//        sendMessage(ActivityType.EMERGENCY_ACCESS);
//        sendMessage(ActivityType.EMERGENCY_ACCESS);
//        sendMessage(ActivityType.EMERGENCY_ACCESS);
//        sendMessage(ActivityType.SEND);
//
//        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(new Callable<Boolean>() {
//            @Override
//            public Boolean call() throws Exception {
//                int numberOfSentMessages = mockLogSenderClientClient.getNumberOfSentMessages();
//                System.out.println("numberOfReceivedMessages: " + numberOfSentMessages);
//                return (numberOfSentMessages == 2);
//            }
//        });
//    }
//
    @Test
    public void ensureMessageEndsUpInDLQ() throws Exception {
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);

        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int numberOfDLQMessages = numberOfDLQMessages();
                System.out.println("numberOfDLQMessages: " + numberOfDLQMessages);
                return (numberOfDLQMessages == 1);
            }
        });
    }

    @Test
    public void ensureTwoMessagesEndsUpInDLQ() throws Exception {
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);

        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);
        sendMessage(ActivityType.EMERGENCY_ACCESS);

        await().atMost(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                int numberOfDLQMessages = numberOfDLQMessages();
                System.out.println("numberOfDLQMessages: " + numberOfDLQMessages);
                return (numberOfDLQMessages == 3);
            }
        });
    }

    private void sendMessage(final ActivityType activityType) throws Exception {
        jmsTemplate.send(sendQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                try {
                    ObjectMessage objectMessage = session.createObjectMessage(TestDataHelper.buildAbstractLogMessageList(activityType));
                    objectMessage.setStringProperty(LogMessageConstants.LOG_TYPE, LogMessageType.SINGLE.name());
                    return objectMessage;
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }


        });
    }

    private int numberOfDLQMessages() throws Exception {
        Integer count = (Integer) jmsTemplate.browse(dlq, new BrowserCallback<Object>() {

            @Override
            public Object doInJms(Session session, QueueBrowser browser) throws JMSException {
                int counter = 0;
                Enumeration<?> msgs = browser.getEnumeration();
                while (msgs.hasMoreElements()) {
                    msgs.nextElement();
                    counter++;
                }
                return counter;
            }
        });
        return count;
    }

//    private void resetQueue() throws Exception {
//        jmsTemplate.browse(sendQueue, new BrowserCallback<Object>() {
//
//            @Override
//            public Object doInJms(Session session, QueueBrowser browser) throws JMSException {
//
//                MessageConsumer consumer = session.createConsumer(jmsTemplate.getDefaultDestination());
//
//                Enumeration<?> msgs = browser.getEnumeration();
//                while (msgs.hasMoreElements()) {
//                    consumer.receive();
//                }
//                return 0;
//            }
//        });
//    }

}
