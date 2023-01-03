/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.notifications.monitoring;



import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import java.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class MonitoringLogServiceImplTest {

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    final private MonitoringLogService logService = new MonitoringLogServiceImpl();

    private final static long EVENT_ID = 1234L;
    private final static String EVENT_TYPE = "EVENT_TYPE";
    private final static String CORRELATION_ID = "CORRELATION_ID";
    private final static String LOGICAL_ADDRESS = "LOGICAL_ADDRESS";
    private final static String CERTIFICATE_ID = "CERTIFICATE_ID";
    private final static String ERROR_CODE = "ERROR_CODE";
    private final static String ERROR_MESSAGE = "ERROR_MESSAGE";
    private final static int DELIVERY_ATTEMPT = 14;
    private final static LocalDateTime NEXT_REDELIEVERY_TIME = LocalDateTime.of(
        2021, 2, 21, 16, 1, 23,123456789
    );

    @Before
    public void setup() {
        final var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
    }

    @After
    public void teardown() {
        final var logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAppender(mockAppender);
    }

    @Test
    public void shouldLogNotificationDeliverySuccess() {
        logService.logStatusUpdateForCareStatusSuccess(EVENT_ID, EVENT_TYPE, CERTIFICATE_ID, CORRELATION_ID,
            LOGICAL_ADDRESS, DELIVERY_ATTEMPT);
        verifyLog("STATUS_UPDATE_RESULT_SUCCESS Status update for care success on "
            + "delivery attempt '" + DELIVERY_ATTEMPT + "' for event ["
            + "certificateId: '" + CERTIFICATE_ID + "', "
            + "correlationId: '" + CORRELATION_ID + "', "
            + "logicalAddress: '" + LOGICAL_ADDRESS + "', "
            + "eventId: '" + EVENT_ID + "', "
            + "eventType: '" + EVENT_TYPE + "']."
        );
    }

    @Test
    public void shouldLogNotificationDeliveryResend() {
        logService.logStatusUpdateForCareStatusResend(EVENT_ID, EVENT_TYPE, LOGICAL_ADDRESS, CERTIFICATE_ID, CORRELATION_ID, ERROR_CODE,
            ERROR_MESSAGE, DELIVERY_ATTEMPT, NEXT_REDELIEVERY_TIME);
        verifyLog("STATUS_UPDATE_RESULT_RESEND Status update for care failure on "
            + "delivery attempt '" + DELIVERY_ATTEMPT + "' for event ["
            + "certificateId: '" + CERTIFICATE_ID + "', "
            + "correlationId: '" + CORRELATION_ID + "', "
            + "logicalAddress: '" + LOGICAL_ADDRESS + "', "
            + "eventId: '" + EVENT_ID + "', "
            + "eventType: '" + EVENT_TYPE + "', "
            + "errorCode: '" + ERROR_CODE + "', "
            + "errorMessage: '" + ERROR_MESSAGE + "']. "
            + "Redelivery has been scheduled for '"+ NEXT_REDELIEVERY_TIME + "'."
        );
    }

    @Test
    public void shouldLogNotificationDeliveryFailure() {
        logService.logStatusUpdateForCareStatusFailure(EVENT_ID, EVENT_TYPE, LOGICAL_ADDRESS, CERTIFICATE_ID, CORRELATION_ID, ERROR_CODE,
            ERROR_MESSAGE, DELIVERY_ATTEMPT);
        verifyLog("STATUS_UPDATE_RESULT_FAILURE Status update for care failure on "
            + "delivery attempt '" + DELIVERY_ATTEMPT + "' for event ["
            + "certificateId: '" + CERTIFICATE_ID + "', "
            + "correlationId: '" + CORRELATION_ID + "', "
            + "logicalAddress: '"+ LOGICAL_ADDRESS + "', "
            + "eventId: '" + EVENT_ID + "', "
            + "eventType: '" + EVENT_TYPE + "', "
            + "errorCode: '" + ERROR_CODE + "', "
            + "errorMessage: '" + ERROR_MESSAGE + "']. No further delivery attempts will be performed."
        );
    }

    private void verifyLog(String logMessage) {
        verify(mockAppender).doAppend(captorLoggingEvent.capture());
        final var loggingEvent = captorLoggingEvent.getValue();

        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertEquals(logMessage, loggingEvent.getFormattedMessage());
    }
}
