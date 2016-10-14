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

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import javax.jms.Queue;
import javax.jms.TextMessage;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import se.inera.intyg.common.logmessages.ActivityType;
import se.inera.intyg.common.logmessages.PdlLogMessage;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.logsender.client.mock.MockLogSenderClientImpl;
import se.inera.intyg.webcert.logsender.helper.TestDataHelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Throwables;

/**
 * Tests the full LogMessage route {@link se.inera.intyg.webcert.logsender.routes.LogSenderRouteBuilder} using Camel
 * and Spring contexts.
 *
 * This test is quite slow due to {@link DirtiesContext.ClassMode#AFTER_EACH_TEST_METHOD} causing the context to be reset
 * fully after each test. Otherwise, artifacts produced be previous tests could affect subsequent tests. (E.g. messages left on DLQ etc.)
 *
 * <strong>Note that all tests use 5 as batch size.</strong>
 *
 * @author eriklupander
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/logsender/integration-test-certificate-sender-config.xml")
@BootstrapWith(CamelTestContextBootstrapper.class)
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class}) // Suppresses warning
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RouteIT {

    private static final Logger LOG = LoggerFactory.getLogger(RouteIT.class);

    private static final int SECONDS_TO_WAIT = 5;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("newLogMessageQueue")
    private Queue sendQueue;

    @Autowired
    @Qualifier("newAggregatedLogMessageQueue")
    private Queue newAggregatedLogMessageQueue;

    @Autowired
    @Qualifier("newAggregatedLogMessageDLQ")
    private Queue newAggregatedLogMessageDLQ;

    @Autowired
    private MockLogSenderClientImpl mockLogSenderClient;

    @Autowired
    @Qualifier("webcertLogMessageSender")
    CamelContext camelContext;

    @Before
    public void resetStub() throws Exception {
        mockLogSenderClient.reset();
    }

    @Test
    public void ensureStubReceivesOneMessageAfterSixHasBeenSent() throws Exception {

        for (int a = 0; a < 6; a++) {
            sendMessage(ActivityType.READ);
        }

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> messagesReceived(1));
    }

    @Test
    public void ensureStubReceivesTwoMessagesAfterTenHasBeenSent() throws Exception {

        for (int a = 0; a < 10; a++) {
            sendMessage(ActivityType.READ);
        }

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> messagesReceived(2));
    }

    @Test
    public void ensureStubReceivesZeroMessagesAfterThreeHasBeenSent() throws Exception {

        for (int a = 0; a < 3; a++) {
            sendMessage(ActivityType.READ);
        }

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> messagesReceived(0));
    }

    @Test
    public void ensureStubReceivesOneMessagesAfterOneWithFiveResourcesHasBeenSent() throws Exception {

        for (int a = 0; a < 1; a++) {
            sendMessage(ActivityType.READ, 5);
        }

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> messagesReceived(1));
    }

    @Test
    public void ensureStubReceivesSixMessagesAfterThreeTimesTenHasBeenSent() throws Exception {

        for (int a = 0; a < 3; a++) {
            sendMessage(ActivityType.READ, 10);
        }

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> messagesReceived(6));
    }

    @Test
    public void ensureStubReceivesZeroMessagesAfterFiveWithNoResourcesHasBeenSent() throws Exception {

        for (int a = 0; a < 5; a++) {
            sendMessage(ActivityType.READ, 0);
        }

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> messagesReceived(0));
    }

    @Test
    public void ensureStubReceivesNoMessageAfterOneWithSixWhereOnIsInvalidHasBeenSent() throws Exception {
        String body = buildPdlLogMessageWithInvalidResourceJson();
        jmsTemplate.send(sendQueue, session -> {
            try {
                TextMessage textMessage = session.createTextMessage(body);
                return textMessage;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        });

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> messagesReceived(0));
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
//                int numberOfSentMessages = mockLogSenderClient.getNumberOfSentMessages();
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

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> expectedDLQMessages(1));
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

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> expectedDLQMessages(2));
    }

    @Test
    public void ensureMessageEndsUpInDlqWithOneInvalidSystemInBatch() throws Exception {

        sendMessage(ActivityType.READ, 2);

        jmsTemplate.send(sendQueue, session -> {
            try {
                PdlLogMessage pdlLogMessage = TestDataHelper.buildBasePdlLogMessage(ActivityType.READ, 1);
                pdlLogMessage.setSystemId("invalid");
                TextMessage textMessage = session.createTextMessage(new CustomObjectMapper().writeValueAsString(pdlLogMessage));
                return textMessage;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        });

        sendMessage(ActivityType.READ, 2);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> expectedDLQMessages(1));
    }

    @Test
    public void ensureStubReceivesOneMessageDueToTimeoutWhenTwoMessagesHaveBeenSent() throws Exception {

        for (int a = 0; a < 2; a++) {
            sendMessage(ActivityType.READ);
        }

        await().atMost(2, TimeUnit.SECONDS).until(() -> messagesReceived(1));
    }

    @Test
    public void ensureStubReceivesZeroMessagesDueToTimeoutNotExpiredWhenTwoMessagesHaveBeenSent() throws Exception {

        for (int a = 0; a < 2; a++) {
            sendMessage(ActivityType.READ);
        }

        await().atMost(500, TimeUnit.MILLISECONDS).until(() -> messagesReceived(0));
    }


    private void sendMessage(final ActivityType activityType, int numberOfResources) throws Exception {
        jmsTemplate.send(sendQueue, session -> {
            try {
                TextMessage textMessage = session.createTextMessage(TestDataHelper.buildBasePdlLogMessageAsJson(activityType, numberOfResources));
                return textMessage;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }

    private void sendMessage(final ActivityType activityType) throws Exception {
        sendMessage(activityType, 1);
    }

    private int numberOfDLQMessages() throws Exception {
        Integer count = (Integer) jmsTemplate.browse(newAggregatedLogMessageDLQ, (BrowserCallback<Object>) (session, browser) -> {
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

    private Boolean messagesReceived(int expected) {
        int numberOfReceivedMessages = mockLogSenderClient.getNumberOfReceivedMessages();
        LOG.info("numberOfReceivedMessages: {}", numberOfReceivedMessages);
        return (numberOfReceivedMessages == expected);
    }

    private Boolean expectedDLQMessages(int expectedDlqMessages) throws Exception {
        int numberOfDLQMessages = numberOfDLQMessages();
        LOG.info("numberOfDLQMessages: {}", numberOfDLQMessages);
        return (numberOfDLQMessages == expectedDlqMessages);
    }

    private String buildPdlLogMessageWithInvalidResourceJson() throws IOException {
        String bodyOfSix = TestDataHelper.buildBasePdlLogMessageAsJson(ActivityType.READ, 6);
        ObjectNode jsonNode = (ObjectNode) new CustomObjectMapper().readTree(bodyOfSix);
        ArrayNode pdlResourceList = (ArrayNode) jsonNode.get("pdlResourceList");

        JsonNode invalidJsonNode = new TextNode("Some text that doesn't belong here");
        ObjectNode resourceNode = (ObjectNode) pdlResourceList.get(2);
        resourceNode.set("resourceOwner", invalidJsonNode);

        return new CustomObjectMapper().writeValueAsString(jsonNode);
    }
}
