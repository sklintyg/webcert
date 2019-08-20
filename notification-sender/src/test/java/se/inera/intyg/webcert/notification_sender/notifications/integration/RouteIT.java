/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.notification_sender.mocks.v3.CertificateStatusUpdateForCareResponderStub.FALLERAT_MEDDELANDE;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.notification_sender.mocks.NotificationStubEntry;
import se.inera.intyg.webcert.notification_sender.notifications.helper.NotificationTestHelper;

@ContextConfiguration("/notifications/integration-test-notification-sender-config.xml")
public class RouteIT extends AbstractBaseIT {

    @Test
    public void ensureAggregatorFiltersOutOldestAndratMessages() throws Exception {
        LocalDateTime first = LocalDateTime.now().minusSeconds(15);
        LocalDateTime second = LocalDateTime.now().minusSeconds(10);
        LocalDateTime third = LocalDateTime.now().minusSeconds(5);

        NotificationMessage notificationMessage1 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.SKAPAT,
            "luae_fs", SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage2 = createNotificationMessage("intyg1", first, HandelsekodEnum.ANDRAT, "luae_fs",
            SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage3 = createNotificationMessage("intyg1", second, HandelsekodEnum.ANDRAT, "luae_fs",
            SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage4 = createNotificationMessage("intyg1", third, HandelsekodEnum.ANDRAT, "luae_fs",
            SchemaVersion.VERSION_3);

        sendMessage(notificationMessage1);
        sendMessage(notificationMessage2);
        sendMessage(notificationMessage3);
        sendMessage(notificationMessage4);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages();
            if (numberOfReceivedMessages == 2) {
                List<NotificationStubEntry> notificationMessages = certificateStatusUpdateForCareResponderV3.getNotificationMessages();
                for (NotificationStubEntry nse : notificationMessages) {
                    if (nse.handelseTyp.equals(HandelsekodEnum.ANDRAT.value())) {
                        assertEquals(third, nse.handelseTid);
                    }
                }
            }
            return (numberOfReceivedMessages == 2);
        });
    }

    @Test
    public void ensureUserIdExists() {
        NotificationMessage msg = createNotificationMessage("1", "fk7263", HandelsekodEnum.SKAPAT);

        sendMessage(msg);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int num = certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages();
            if (num == 1) {
                assertNotNull(certificateStatusUpdateForCareResponderV3.getNotificationMessages().get(0).userId);
            }
            return (num == 1);
        });
    }

    @Test
    public void ensureWiretapWorks() throws Exception {
        LocalDateTime first = LocalDateTime.now().minusSeconds(15);
        LocalDateTime second = LocalDateTime.now().minusSeconds(10);

        NotificationMessage notificationMessage2 = createNotificationMessage("intyg1", first, HandelsekodEnum.ANDRAT, "luae_fs",
            SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage3 = createNotificationMessage("intyg1", second, HandelsekodEnum.SIGNAT, "luae_fs",
            SchemaVersion.VERSION_3);

        sendMessage(notificationMessage2);
        sendMessage(notificationMessage3);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages();
            if (numberOfReceivedMessages == 1) {
                List<NotificationStubEntry> notificationMessages = certificateStatusUpdateForCareResponderV3.getNotificationMessages();
                for (NotificationStubEntry nse : notificationMessages) {
                    if (nse.handelseTyp.equals(HandelsekodEnum.SIGNAT.value())) {
                        assertEquals(second, nse.handelseTid);
                    }
                }
            }
            return (numberOfReceivedMessages == 1);
        });
    }

    @Test
    public void ensureAggregatorFiltersOutAndratMessagesWhenSigned() throws Exception {

        NotificationMessage notificationMessage1 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.SKAPAT,
            "luae_fs", SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage2 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.ANDRAT,
            "luae_fs", SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage3 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.ANDRAT,
            "luae_fs", SchemaVersion.VERSION_3);
        NotificationMessage notificationMessage4 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.SIGNAT,
            "luae_fs", SchemaVersion.VERSION_3);

        sendMessage(notificationMessage1);
        sendMessage(notificationMessage2);
        sendMessage(notificationMessage3);
        sendMessage(notificationMessage4);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages();
            if (numberOfReceivedMessages == 2) {
                List<NotificationStubEntry> notificationMessages = certificateStatusUpdateForCareResponderV3.getNotificationMessages();
                for (NotificationStubEntry nse : notificationMessages) {
                    if (nse.handelseTyp.equals(HandelsekodEnum.ANDRAT.value())) {
                        fail("No ANDRAT messages are allowed when intyg has been signed");
                    }
                }
            }
            return (numberOfReceivedMessages == 2);
        });
    }

    @Test
    public void ensureRouting() throws Exception {
        // 2 messages
        NotificationMessage luaefs1 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.SKAPAT, "luae_fs",
            SchemaVersion.VERSION_3);
        NotificationMessage luaefs2 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.ANDRAT, "luae_fs",
            SchemaVersion.VERSION_3);
        NotificationMessage luaefs3 = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.ANDRAT, "luae_fs",
            SchemaVersion.VERSION_3);

        // 3 messages
        NotificationMessage fk1 = createNotificationMessage("intyg2", "fk7263", HandelsekodEnum.SKAPAT);
        NotificationMessage fk2 = createNotificationMessage("intyg2", "fk7263", HandelsekodEnum.ANDRAT);
        NotificationMessage fk3 = createNotificationMessage("intyg2", "fk7263", HandelsekodEnum.SIGNAT);

        // 2 messages
        NotificationMessage luaefs4 = createNotificationMessage("intyg3", LocalDateTime.now(), HandelsekodEnum.MAKULE, "luae_fs",
            SchemaVersion.VERSION_3);
        NotificationMessage luaefs5 = createNotificationMessage("intyg4", LocalDateTime.now(), HandelsekodEnum.SKICKA, "luae_fs",
            SchemaVersion.VERSION_3);

        sendMessage(luaefs1);
        sendMessage(fk1);
        sendMessage(luaefs2);
        sendMessage(luaefs3);
        sendMessage(fk2);
        sendMessage(luaefs4);
        sendMessage(fk3);
        sendMessage(luaefs5);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessagesV3 = certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages();
            return (numberOfReceivedMessagesV3 == 7);
        });
    }

    @Test
    public void ensureStubReceivedAllMessages() throws Exception {
        NotificationMessage notificationMessage1 = createNotificationMessage("intyg1", "fk7263", HandelsekodEnum.SKAPAT);
        NotificationMessage notificationMessage2 = createNotificationMessage("intyg2", "fk7263", HandelsekodEnum.ANDRAT);
        NotificationMessage notificationMessage3 = createNotificationMessage("intyg3", "fk7263", HandelsekodEnum.SIGNAT);

        sendMessage(notificationMessage1);
        sendMessage(notificationMessage2);
        sendMessage(notificationMessage3);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfReceivedMessages = certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages();
            return (numberOfReceivedMessages == 3);
        });
    }

    @Test
    public void ensureMessagesAreResentAndDoNotBlockEachOther() throws Exception {
        final String intygsId1 = FALLERAT_MEDDELANDE + "2";
        final String intygsId2 = "korrekt-meddelande-1";

        when(fk7263ModuleApi.getIntygFromUtlatande(any()))
            .thenReturn(NotificationTestHelper.createIntyg("fk7263", "1.0", FALLERAT_MEDDELANDE + "2"))
            .thenReturn(NotificationTestHelper.createIntyg("fk7263", "1.0", "korrekt-meddelande-1"));

        NotificationMessage notificationMessage1 = createNotificationMessage(intygsId1, "fk7263", HandelsekodEnum.SKAPAT);
        NotificationMessage notificationMessage2 = createNotificationMessage(intygsId2, "fk7263", HandelsekodEnum.ANDRAT);

        sendMessage(notificationMessage1);
        sendMessage(notificationMessage2);

        await().atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS).until(() -> {
            int numberOfSuccessfulMessages = certificateStatusUpdateForCareResponderV3.getNumberOfSentMessages();
            if (numberOfSuccessfulMessages == 2) {
                List<String> utlatandeIds = certificateStatusUpdateForCareResponderV3.getIntygsIdsInOrder();
                return (utlatandeIds.size() == 2
                    && utlatandeIds.get(0).equals(intygsId2)
                    && utlatandeIds.get(1).equals(intygsId1));
            }
            return false;
        });
    }
}
