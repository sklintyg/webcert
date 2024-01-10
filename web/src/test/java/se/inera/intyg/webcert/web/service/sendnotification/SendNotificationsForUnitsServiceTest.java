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
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepositoryCustom;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.SendNotificationsForUnitsRequestDTO;

@ExtendWith(MockitoExtension.class)
class SendNotificationsForUnitsServiceTest {

    private static final Integer COUNT = 10;
    private static final List<String> IDS = List.of("ID");
    private static final List<NotificationDeliveryStatusEnum> STATUSES = List.of(NotificationDeliveryStatusEnum.FAILURE);
    private static final LocalDateTime START = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime END = LocalDateTime.now();
    private static final LocalDateTime ACTIVATION_TIME = LocalDateTime.now();
    private static final SendNotificationsForUnitsRequestDTO REQUEST = SendNotificationsForUnitsRequestDTO.create(IDS,
        STATUSES, ACTIVATION_TIME, START, END);

    @Mock
    NotificationRedeliveryRepositoryCustom notificationRedeliveryRepositoryCustom;

    @InjectMocks
    SendNotificationsForUnitsService sendNotificationsForUnitsService;

    @Test
    void shouldReturnResponseFromRepository() {
        when(notificationRedeliveryRepositoryCustom.sendNotificationsForUnits(IDS, STATUSES, START, END))
            .thenReturn(COUNT);

        final var response = sendNotificationsForUnitsService.send(REQUEST);

        assertEquals(COUNT, response.getCount());
    }
}