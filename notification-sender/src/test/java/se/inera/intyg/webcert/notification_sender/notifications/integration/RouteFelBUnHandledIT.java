/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import static com.google.common.collect.MoreCollectors.onlyElement;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.notificationstub.v3.NotificationStubStateBean;

@DirtiesContext
@ContextConfiguration("/notifications/integration-test-notification-sender-config-felb.xml")
public class RouteFelBUnHandledIT extends AbstractBaseIT {

    @Autowired
    private NotificationStubStateBean notificationStubStateBean;

    @Test
    public void testFelBUnhandled() throws Exception {
        String orgErrorCode = notificationStubStateBean.getErrorCode();
        notificationStubStateBean.setErrorCode("1");

        NotificationMessage message = createNotificationMessage("intyg1", LocalDateTime.now(), HandelsekodEnum.ANDRAT, "luae_fs", SchemaVersion.VERSION_3);

        sendMessage(message);

        AtomicInteger nbr = new AtomicInteger(0);
        await()
                .atMost(SECONDS_TO_WAIT, TimeUnit.SECONDS)
                .until(() -> {
                    nbr.set(certificateStatusUpdateForCareResponderV3.getNumberOfReceivedMessages());
                    return nbr.get() == 4;
                });

        final ActiveMQConnection connection = (ActiveMQConnection) activeMQConnectionFactory.createConnection();
        connection.start();

        final Set<ActiveMQQueue> queues = connection.getDestinationSource().getQueues();
        final List<Pair<String, Integer>> status = getAmqStatus(queues);
        final int nbrMessages = status.stream().filter(pair -> pair.getLeft().equals("DLQ.sendNotificationToWS")).map(Pair::getRight).collect(onlyElement());

        assertEquals(1, nbrMessages);
        notificationStubStateBean.setErrorCode(orgErrorCode);
    }
}
