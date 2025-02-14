/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.sendnotification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {

    private static final Integer COUNT = 10;
    private static final String ID = "ID";

    @Mock
    SendNotificationCountValidator sendNotificationCountValidator;

    @Mock
    NotificationRedeliveryRepository notificationRedeliveryRepositoryCustom;

    @Mock
    SendNotificationRequestValidator sendNotificationRequestValidator;

    @InjectMocks
    SendNotificationService sendNotificationService;

    @Test
    void shouldThrowIfCountExceedLimit() {
        doThrow(IllegalArgumentException.class).when(sendNotificationCountValidator)
            .notification(ID);

        assertThrows(IllegalArgumentException.class, () -> sendNotificationService.send(ID));
    }

    @Test
    void shouldReturnResponseFromRepository() {
        when(notificationRedeliveryRepositoryCustom.sendNotification(ID))
            .thenReturn(COUNT);

        final var response = sendNotificationService.send(ID);

        assertEquals(COUNT, response.getCount());
    }

    @Test
    void shouldValidateId() {
        final var captor = ArgumentCaptor.forClass(String.class);
        sendNotificationService.send(ID);

        verify(sendNotificationRequestValidator).validateId(captor.capture());

        assertEquals(ID, captor.getValue());
    }
}