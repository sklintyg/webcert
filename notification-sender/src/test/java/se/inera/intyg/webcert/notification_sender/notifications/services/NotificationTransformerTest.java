/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mockito;

import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.notification.FragorOchSvar;
import se.inera.intyg.common.support.modules.support.api.notification.HandelseType;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.webcert.notification_sender.notifications.routes.RouteHeaders;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTransformer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NotificationTransformerTest {

    public static final String EXPECTED_BODY = "Body";
    public static final String INTYGS_ID = "intyg1";
    public static final String LOGISK_ADRESS = "address1";

    @Test
    public void testSend() throws Exception {
        // Given
        NotificationMessage notificationMessage = new NotificationMessage(INTYGS_ID, "FK7263", new LocalDateTime(),
                HandelseType.INTYGSUTKAST_SKAPAT, LOGISK_ADRESS, "{ }", new FragorOchSvar(0, 0, 0, 0));
        Message message = new DefaultMessage();
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
        assertEquals(message.getHeader(RouteHeaders.HANDELSE), HandelseType.INTYGSUTKAST_SKAPAT.value());
        assertEquals(message.getHeader(RouteHeaders.INTYGS_ID), INTYGS_ID);
        assertEquals(message.getHeader(RouteHeaders.LOGISK_ADRESS), LOGISK_ADRESS);
    }

}
