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
package se.inera.intyg.webcert.notification_sender.notifications.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationPostProcessingService;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;

@RunWith(MockitoJUnitRunner.class)
public class NotificationPostProcessorTest {

    @Mock
    private NotificationPostProcessingService notificationPostProcessingService;
    @Spy
    private MdcHelper mdcHelper;
    @Mock
    private Message message;
    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationPostProcessor notificationPostProcessor;

    @Before
    public void setup() {
        when(message.getHeader(anyString())).thenReturn("headerValue");
    }

    @Test
    public void shallProcessNotificationResultMessage() throws Exception {
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
        final var body = "Text for parsing";

        doReturn(body).when(message).getBody(String.class);

        notificationPostProcessor.process(message);

        verifyNoInteractions(notificationPostProcessingService);
    }

    @Test(expected = Exception.class)
    public void shallNotCatchExceptionsExceptJsonProcessingExceptions() throws Exception {
        final var notificationResultMessage = createNotificationResultMessage();
        final var body = objectMapper.writeValueAsString(notificationResultMessage);

        doReturn(body).when(message).getBody(String.class);
        doThrow(new RuntimeException("Fail!"))
            .when(notificationPostProcessingService)
            .processNotificationResult(any(NotificationResultMessage.class));

        notificationPostProcessor.process(message);

        fail("Should never reach this assert!");
    }

    private NotificationResultMessage createNotificationResultMessage() {
        final var notificationResultMessage = new NotificationResultMessage();
        notificationResultMessage.setEvent(new Handelse());
        notificationResultMessage.setResultType(new NotificationResultType());
        notificationResultMessage.setStatusUpdateXml("STATUS_UPDATE_XML".getBytes());
        notificationResultMessage.setCorrelationId("CORRELATION_ID");
        return notificationResultMessage;
    }
}
