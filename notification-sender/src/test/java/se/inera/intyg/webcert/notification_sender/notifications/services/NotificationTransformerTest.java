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

package se.inera.intyg.webcert.notification_sender.notifications.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mockito;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.notification.*;
import se.inera.intyg.webcert.notification_sender.notifications.routes.RouteHeaders;

public class NotificationTransformerTest {

    private static final String EXPECTED_BODY = "Body";
    private static final String INTYGS_ID = "intyg1";
    private static final String LOGISK_ADRESS = "address1";
    private static final String FK7263 = "FK7263";

    @Test
    public void testSend() throws Exception {
        // Given
        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, "FK7263", new LocalDateTime(),
                HandelseType.INTYGSUTKAST_SKAPAT, LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), NotificationVersion.VERSION_1);
        Message message = spy(new DefaultMessage());
        message.setBody(notificationMessage);

        IntygModuleRegistry moduleRegistry = mock(IntygModuleRegistry.class);
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(Mockito.anyString())).thenReturn(moduleApi);
        when(moduleApi.createNotification(Mockito.any(NotificationMessage.class))).thenReturn(EXPECTED_BODY);

        NotificationTransformer processor = new NotificationTransformer();
        processor.setModuleRegistry(moduleRegistry);

        // When
        processor.process(message);

        // Then
        assertEquals(EXPECTED_BODY, message.getBody());
        assertEquals(HandelseType.INTYGSUTKAST_SKAPAT.value(), message.getHeader(RouteHeaders.HANDELSE));
        assertEquals(INTYGS_ID, message.getHeader(RouteHeaders.INTYGS_ID));
        assertEquals(LOGISK_ADRESS, message.getHeader(RouteHeaders.LOGISK_ADRESS));
        assertEquals(NotificationVersion.VERSION_1.name(), message.getHeader(RouteHeaders.VERSION));

        verify(message, times(1)).setHeader(eq(RouteHeaders.LOGISK_ADRESS), eq(LOGISK_ADRESS));
        verify(message, times(1)).setHeader(eq(RouteHeaders.INTYGS_ID), eq(INTYGS_ID));
        verify(message, times(1)).setHeader(eq(RouteHeaders.HANDELSE), eq(HandelseType.INTYGSUTKAST_SKAPAT.value()));
        verify(message, times(1)).setHeader(eq(RouteHeaders.VERSION), eq(NotificationVersion.VERSION_1.name()));

        verify(moduleRegistry, times(1)).getModuleApi(eq(FK7263));
        verify(moduleApi, times(1)).createNotification(any());
    }

    @Test
    public void testSendBackwardsCompatibility() throws Exception {
        // Given
        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, "FK7263", new LocalDateTime(),
                HandelseType.INTYGSUTKAST_SKAPAT, LOGISK_ADRESS, "{ }", FragorOchSvar.getEmpty(), null);
        Message message = spy(new DefaultMessage());
        message.setBody(notificationMessage);

        IntygModuleRegistry moduleRegistry = mock(IntygModuleRegistry.class);
        ModuleApi moduleApi = mock(ModuleApi.class);
        when(moduleRegistry.getModuleApi(Mockito.anyString())).thenReturn(moduleApi);
        when(moduleApi.createNotification(Mockito.any(NotificationMessage.class))).thenReturn(EXPECTED_BODY);

        NotificationTransformer processor = new NotificationTransformer();
        processor.setModuleRegistry(moduleRegistry);

        // When
        processor.process(message);

        // Then
        assertEquals(EXPECTED_BODY, message.getBody());
        assertEquals(HandelseType.INTYGSUTKAST_SKAPAT.value(), message.getHeader(RouteHeaders.HANDELSE));
        assertEquals(INTYGS_ID, message.getHeader(RouteHeaders.INTYGS_ID));
        assertEquals(LOGISK_ADRESS, message.getHeader(RouteHeaders.LOGISK_ADRESS));
        assertNull(message.getHeader(RouteHeaders.VERSION));

        verify(message, times(1)).setHeader(eq(RouteHeaders.LOGISK_ADRESS), eq(LOGISK_ADRESS));
        verify(message, times(1)).setHeader(eq(RouteHeaders.INTYGS_ID), eq(INTYGS_ID));
        verify(message, times(1)).setHeader(eq(RouteHeaders.HANDELSE), eq(HandelseType.INTYGSUTKAST_SKAPAT.value()));
        verify(message, never()).setHeader(eq(RouteHeaders.VERSION), any());

        verify(moduleRegistry, times(1)).getModuleApi(eq(FK7263));
        verify(moduleApi, times(1)).createNotification(any());
    }

}
