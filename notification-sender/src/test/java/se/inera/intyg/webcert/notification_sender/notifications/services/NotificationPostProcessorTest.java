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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;


@RunWith(MockitoJUnitRunner.class)
public class NotificationPostProcessorTest {

    @Mock
    private NotificationRedeliveryService notificationRedeliveryService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationPostProcessor postProcessor;

    @Test
    public void processNotificationResultFromMessage() throws Exception {
        final var message = mock(Message.class);
        final var body = "BODY";
        final var notificationResultMessage = mock(NotificationResultMessage.class);
        final var argumentCaptor = ArgumentCaptor.forClass(NotificationResultMessage.class);

        doReturn(body).when(message).getBody(String.class);
        doReturn(notificationResultMessage).when(objectMapper).readValue(body, NotificationResultMessage.class);

        postProcessor.process(message);

        verify(notificationRedeliveryService).processNotificationResult(argumentCaptor.capture());
        assertEquals(notificationResultMessage, argumentCaptor.getValue());
    }

    @Test
    public void dontProcessAnythingIfMessageIsCorrupt() throws Exception {
        final var message = mock(Message.class);
        final var body = "BODY";

        doReturn(body).when(message).getBody(String.class);
        doThrow(new RuntimeException()).when(objectMapper).readValue(anyString(), eq(NotificationResultMessage.class));

        postProcessor.process(message);

        verifyNoInteractions(notificationRedeliveryService);
    }
}
