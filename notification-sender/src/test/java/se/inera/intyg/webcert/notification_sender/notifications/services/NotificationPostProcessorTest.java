/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.notification_sender.notifications.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationPostProcessingService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;

@RunWith(MockitoJUnitRunner.class)
public class NotificationPostProcessorTest {

    @Mock
    private NotificationPostProcessingService notificationPostProcessingService;

    private NotificationPostProcessor notificationPostProcessor;

    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = new ObjectMapper();
        notificationPostProcessor = new NotificationPostProcessor(objectMapper, notificationPostProcessingService);
    }

    @Test
    public void shallProcessNotificationResultMessage() throws Exception {
        final var message = mock(Message.class);
        final var notificationResultMessage = createNotificationResultMessage();
        final var body = objectMapper.writeValueAsString(notificationResultMessage);
        final var argumentCaptor = ArgumentCaptor.forClass(NotificationResultMessage.class);

        doReturn(body).when(message).getBody(String.class);

        notificationPostProcessor.process(message);

        verify(notificationPostProcessingService).processNotificationResult(argumentCaptor.capture());
        assertEquals(notificationResultMessage.getCorrelationId(), argumentCaptor.getValue().getCorrelationId());
    }

    @Test
    public void shallNotProcessNotificationResultMessageIfCorrupt() {
        final var message = mock(Message.class);
        final var body = "JUST ADD SOME TEXT THAT WILL NOT SUCCESSFULLY BE PARSED!";

        doReturn(body).when(message).getBody(String.class);

        notificationPostProcessor.process(message);

        verifyNoInteractions(notificationPostProcessingService);
    }

    private NotificationResultMessage createNotificationResultMessage() {
        final var notificationResultMessage = new NotificationResultMessage();
        notificationResultMessage.setEvent(new Handelse());
        notificationResultMessage.setResultType(new NotificationResultType());
        notificationResultMessage.setRedeliveryMessageBytes("BYTES".getBytes());
        notificationResultMessage.setCorrelationId("CORRELATION_ID");
        return notificationResultMessage;
    }
}
