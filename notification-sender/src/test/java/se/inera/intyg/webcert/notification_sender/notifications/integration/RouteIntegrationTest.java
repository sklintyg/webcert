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

package se.inera.intyg.webcert.notification_sender.notifications.integration;

import static com.jayway.awaitility.Awaitility.await;
import static se.inera.intyg.webcert.notification_sender.mocks.v1.CertificateStatusUpdateForCareResponderStub.FALLERAT_MEDDELANDE;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jms.Queue;
import javax.jms.TextMessage;

import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import se.inera.intyg.common.support.modules.support.api.notification.*;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.notification_sender.mocks.v1.CertificateStatusUpdateForCareResponderStub;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/notifications/integration-test-notification-sender-config.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RouteIntegrationTest {
    private static final Logger LOG = LoggerFactory.getLogger(RouteIntegrationTest.class);

    private static final int SECONDS_TO_WAIT = 10;

    private static final String INTYG_JSON = "{\"id\":\"1234\",\"typ\":\"fk7263\"}";

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("notificationQueue")
    private Queue sendQueue;

    @Autowired
    private CertificateStatusUpdateForCareResponderStub certificateStatusUpdateForCareResponderStub;

    ObjectMapper objectMapper = new CustomObjectMapper();

    @Before
    public void resetStub() {
        this.certificateStatusUpdateForCareResponderStub.reset();
    }

    @Test
    public void ensureStubReceivedAllMessages() throws Exception {
        NotificationMessage notificationMessage1 = createNotificationMessage("intyg1", HandelseType.INTYGSUTKAST_SKAPAT);
        NotificationMessage notificationMessage2 = createNotificationMessage("intyg2", HandelseType.INTYGSUTKAST_ANDRAT);
        NotificationMessage notificationMessage3 = createNotificationMessage("intyg3", HandelseType.INTYGSUTKAST_SIGNERAT);

        sendMessage(notificationMessage1);
        sendMessage(notificationMessage2);
        sendMessage(notificationMessage3);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = certificateStatusUpdateForCareResponderStub.getNumberOfReceivedMessages();
            System.out.println("numberOfReceivedMessages: " + numberOfReceivedMessages);
            return (numberOfReceivedMessages == 3);
        });
    }

    @Test
    public void ensureMessagesAreResentAndDoNotBlockEachOther() throws Exception {
        final String intygsId1 = FALLERAT_MEDDELANDE + "2";
        final String intygsId2 = "korrekt-meddelande-1";
        NotificationMessage notificationMessage1 = createNotificationMessage(intygsId1, HandelseType.INTYGSUTKAST_SKAPAT);
        NotificationMessage notificationMessage2 = createNotificationMessage(intygsId2, HandelseType.INTYGSUTKAST_ANDRAT);

        sendMessage(notificationMessage1);
        LOG.info("Message 1 sent");
        sendMessage(notificationMessage2);
        LOG.info("Message 2 sent");

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfSuccessfulMessages = certificateStatusUpdateForCareResponderStub.getNumberOfSentMessages();
            LOG.debug("Number of sucessful messages: {}", numberOfSuccessfulMessages);
            if (numberOfSuccessfulMessages == 2) {
                List<String> utlatandeIds = certificateStatusUpdateForCareResponderStub.getIntygsIdsInOrder();
                LOG.debug("Number of utlatandeIds: {}", utlatandeIds.size());
                LOG.debug("First ID: {}", utlatandeIds.get(0));
                LOG.debug("Second ID: {}", utlatandeIds.get(1));
                return (utlatandeIds.size() == 2 &&
                        utlatandeIds.get(0).equals(intygsId2) &&
                        utlatandeIds.get(1).equals(intygsId1));
            }
            return false;
        });
    }

    private NotificationMessage createNotificationMessage(String intygsId1, HandelseType handelseType) {
        return new NotificationMessage(intygsId1, "FK7263", new LocalDateTime(),
                handelseType, "address2", INTYG_JSON, new FragorOchSvar(0, 0, 0, 0), NotificationVersion.VERSION_1);
    }

    private String notificationMessageToJson(NotificationMessage notificationMessage) throws Exception {
        return objectMapper.writeValueAsString(notificationMessage);
    }

    private void sendMessage(final NotificationMessage message) throws Exception {
        jmsTemplate.send(sendQueue, session -> {
            try {
                return session.createTextMessage(notificationMessageToJson(message));
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }

}
