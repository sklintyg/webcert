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
package se.inera.intyg.webcert.notification_sender.notifications.strategy;


import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = NotificationRedeliveryStrategyFactory.class)
@TestPropertySource(properties = {"notification.redelivery.strategy.template.standard=1#1:s"})
public class NotificationRedeliveryStrategyFactoryTest {

    @Autowired
    NotificationRedeliveryStrategyFactory factory;

    @Test
    public void shouldReturnStandardStrategyWhenRequested() {
        final var strategy = factory.getResendStrategy(NotificationRedeliveryStrategyEnum.STANDARD);
        assertTrue(strategy instanceof NotificationRedeliveryStrategyStandard);
    }

    @Test
    public void shouldReturnStrategySingleWhenRequested() {
        final var strategy = factory.getResendStrategy(NotificationRedeliveryStrategyEnum.SINGLE);
        assertTrue(strategy instanceof NotificationRedeliveryStrategySingle);
    }
}
