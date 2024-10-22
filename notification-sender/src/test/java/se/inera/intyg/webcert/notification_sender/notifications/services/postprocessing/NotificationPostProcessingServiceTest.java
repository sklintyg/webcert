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
package se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.FAILURE;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.RESEND;
import static se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum.SUCCESS;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.APPLICATION_ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.REVOKED;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.TECHNICAL_ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.VALIDATION_ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum.WEBCERT_EXCEPTION;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.ERROR;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.INFO;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.OK;
import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.UNRECOVERABLE_ERROR;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;

@RunWith(MockitoJUnitRunner.class)
public class NotificationPostProcessingServiceTest {

    @Mock
    private NotificationResultSuccessService notificationResultSuccessService;

    @Mock
    private NotificationResultFailedService notificationResultFailedService;

    @Mock
    private NotificationResultResendService notificationResultResendService;

    @Mock
    private Appender<ILoggingEvent> appender;

    @InjectMocks
    private NotificationPostProcessingService notificationPostProcessingService;

    private static final Long EVENT_ID = 1000L;
    private static final String UNIT_ID = "UNIT_ID";
    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";
    private static final String CORRELATION_ID = "CORRELATION_ID";
    private static final HandelsekodEnum TEST_EVENT_ENUM = HandelsekodEnum.SKAPAT;
    private static final byte[] STATUS_UPDATE_XML = "STATUS_UPDATE_XML".getBytes();

    private static final String SOAPFAULTEXCEPTION = "jakarta.xml.ws.soap.SOAPFaultException";
    private static final String MARSHALLING_ERROR = "Marshalling Error";
    private static final String UNMARSHALLING_ERROR = "Unmarshalling Error";

    @Test
    public void shouldProduceFailureOnUnrecoverableWebcertException() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(UNRECOVERABLE_ERROR, WEBCERT_EXCEPTION,
            "UNRECOVERABLE_WEBCERT_EXCEPTION", "TEST_EXCEPTION"));

        final var captureResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(notificationResultFailedService).process(captureResultMessage.capture());
        assertEquals(CORRELATION_ID, captureResultMessage.getValue().getCorrelationId());
        assertEquals(EVENT_ID, captureResultMessage.getValue().getEvent().getId());
        assertEquals(UNIT_ID, captureResultMessage.getValue().getEvent().getEnhetsId());
        assertEquals(CERTIFICATE_ID, captureResultMessage.getValue().getEvent().getIntygsId());
        assertEquals(STATUS_UPDATE_XML, captureResultMessage.getValue().getStatusUpdateXml());
        assertEquals(FAILURE, captureResultMessage.getValue().getEvent().getDeliveryStatus());
    }

    @Test
    public void shouldProduceFailureOnSoapFaultExceptionWithMarshallingError() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(ERROR, WEBCERT_EXCEPTION, MARSHALLING_ERROR,
            SOAPFAULTEXCEPTION));

        final var captureResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(notificationResultFailedService).process(captureResultMessage.capture());
        assertEquals(CORRELATION_ID, captureResultMessage.getValue().getCorrelationId());
        assertEquals(EVENT_ID, captureResultMessage.getValue().getEvent().getId());
        assertEquals(UNIT_ID, captureResultMessage.getValue().getEvent().getEnhetsId());
        assertEquals(CERTIFICATE_ID, captureResultMessage.getValue().getEvent().getIntygsId());
        assertEquals(STATUS_UPDATE_XML, captureResultMessage.getValue().getStatusUpdateXml());
        assertEquals(FAILURE, captureResultMessage.getValue().getEvent().getDeliveryStatus());
    }

    @Test
    public void shouldProduceFailureOnSoapFaultExceptionWithUnMarshallingError() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(ERROR, WEBCERT_EXCEPTION, UNMARSHALLING_ERROR,
            SOAPFAULTEXCEPTION));

        final var captureResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(notificationResultFailedService).process(captureResultMessage.capture());
        assertEquals(CORRELATION_ID, captureResultMessage.getValue().getCorrelationId());
        assertEquals(EVENT_ID, captureResultMessage.getValue().getEvent().getId());
        assertEquals(UNIT_ID, captureResultMessage.getValue().getEvent().getEnhetsId());
        assertEquals(CERTIFICATE_ID, captureResultMessage.getValue().getEvent().getIntygsId());
        assertEquals(STATUS_UPDATE_XML, captureResultMessage.getValue().getStatusUpdateXml());
        assertEquals(FAILURE, captureResultMessage.getValue().getEvent().getDeliveryStatus());
    }

    @Test
    public void shouldProduceFailureOnValidationError() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(ERROR, VALIDATION_ERROR, "VALIDATION_ERROR", null));

        final var captureResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(notificationResultFailedService).process(captureResultMessage.capture());
        assertEquals(CORRELATION_ID, captureResultMessage.getValue().getCorrelationId());
        assertEquals(EVENT_ID, captureResultMessage.getValue().getEvent().getId());
        assertEquals(UNIT_ID, captureResultMessage.getValue().getEvent().getEnhetsId());
        assertEquals(CERTIFICATE_ID, captureResultMessage.getValue().getEvent().getIntygsId());
        assertEquals(STATUS_UPDATE_XML, captureResultMessage.getValue().getStatusUpdateXml());
        assertEquals(FAILURE, captureResultMessage.getValue().getEvent().getDeliveryStatus());
    }

    @Test
    public void shouldProduceFailureOnApplicationError() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(ERROR, APPLICATION_ERROR, "APPLICATION_ERROR", null));

        final var captureResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(notificationResultFailedService).process(captureResultMessage.capture());
        assertEquals(CORRELATION_ID, captureResultMessage.getValue().getCorrelationId());
        assertEquals(EVENT_ID, captureResultMessage.getValue().getEvent().getId());
        assertEquals(UNIT_ID, captureResultMessage.getValue().getEvent().getEnhetsId());
        assertEquals(CERTIFICATE_ID, captureResultMessage.getValue().getEvent().getIntygsId());
        assertEquals(STATUS_UPDATE_XML, captureResultMessage.getValue().getStatusUpdateXml());
        assertEquals(FAILURE, captureResultMessage.getValue().getEvent().getDeliveryStatus());
    }

    @Test
    public void shouldProduceFailureOnRevoked() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(ERROR, REVOKED, "REVOKED", null));

        final var captureResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(notificationResultFailedService).process(captureResultMessage.capture());
        assertEquals(CORRELATION_ID, captureResultMessage.getValue().getCorrelationId());
        assertEquals(EVENT_ID, captureResultMessage.getValue().getEvent().getId());
        assertEquals(UNIT_ID, captureResultMessage.getValue().getEvent().getEnhetsId());
        assertEquals(CERTIFICATE_ID, captureResultMessage.getValue().getEvent().getIntygsId());
        assertEquals(STATUS_UPDATE_XML, captureResultMessage.getValue().getStatusUpdateXml());
        assertEquals(FAILURE, captureResultMessage.getValue().getEvent().getDeliveryStatus());
    }

    @Test
    public void shouldProduceResendOnTechnicalError() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(ERROR, TECHNICAL_ERROR, "TECHNICAL_ERROR", null));

        final var captureResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(notificationResultResendService).process(captureResultMessage.capture());
        assertEquals(CORRELATION_ID, captureResultMessage.getValue().getCorrelationId());
        assertEquals(EVENT_ID, captureResultMessage.getValue().getEvent().getId());
        assertEquals(UNIT_ID, captureResultMessage.getValue().getEvent().getEnhetsId());
        assertEquals(CERTIFICATE_ID, captureResultMessage.getValue().getEvent().getIntygsId());
        assertEquals(STATUS_UPDATE_XML, captureResultMessage.getValue().getStatusUpdateXml());
        assertEquals(RESEND, captureResultMessage.getValue().getEvent().getDeliveryStatus());
    }

    @Test
    public void shouldProduceResendOnRecoverableWebcertException() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(ERROR, WEBCERT_EXCEPTION, "RECOVERABLE_WEBCERT_EXCEPTION",
            "TEST_EXCEPTION"));

        final var captureResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(notificationResultResendService).process(captureResultMessage.capture());
        assertEquals(CORRELATION_ID, captureResultMessage.getValue().getCorrelationId());
        assertEquals(EVENT_ID, captureResultMessage.getValue().getEvent().getId());
        assertEquals(UNIT_ID, captureResultMessage.getValue().getEvent().getEnhetsId());
        assertEquals(CERTIFICATE_ID, captureResultMessage.getValue().getEvent().getIntygsId());
        assertEquals(STATUS_UPDATE_XML, captureResultMessage.getValue().getStatusUpdateXml());
        assertEquals(RESEND, captureResultMessage.getValue().getEvent().getDeliveryStatus());
    }

    @Test
    public void shouldProduceSuccessOnReturnTypeOk() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(OK, null, null, null));

        final var captureResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(notificationResultSuccessService).process(captureResultMessage.capture());
        assertEquals(CORRELATION_ID, captureResultMessage.getValue().getCorrelationId());
        assertEquals(EVENT_ID, captureResultMessage.getValue().getEvent().getId());
        assertEquals(UNIT_ID, captureResultMessage.getValue().getEvent().getEnhetsId());
        assertEquals(CERTIFICATE_ID, captureResultMessage.getValue().getEvent().getIntygsId());
        assertEquals(STATUS_UPDATE_XML, captureResultMessage.getValue().getStatusUpdateXml());
        assertEquals(SUCCESS, captureResultMessage.getValue().getEvent().getDeliveryStatus());
    }

    @Test
    public void shouldProduceSuccessOnReturnTypeInfo() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(INFO, null, "INFORMATION_TEXT", null));

        final var captureResultMessage = ArgumentCaptor.forClass(NotificationResultMessage.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(notificationResultSuccessService).process(captureResultMessage.capture());
        assertEquals(CORRELATION_ID, captureResultMessage.getValue().getCorrelationId());
        assertEquals(EVENT_ID, captureResultMessage.getValue().getEvent().getId());
        assertEquals(UNIT_ID, captureResultMessage.getValue().getEvent().getEnhetsId());
        assertEquals(CERTIFICATE_ID, captureResultMessage.getValue().getEvent().getIntygsId());
        assertEquals(STATUS_UPDATE_XML, captureResultMessage.getValue().getStatusUpdateXml());
        assertEquals(SUCCESS, captureResultMessage.getValue().getEvent().getDeliveryStatus());
    }

    @Test
    public void shouldInfoLogResultTextOnReturnTypeInfo() {
        final var notificationResultMessage = createNotificationResultMessage();
        notificationResultMessage.setResultType(createNotificationResultType(INFO, null, "INFORMATION_TEXT", null));

        // TODO Is use of the Appender, which not visible in the tested code, accepted practice?
        final var root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(appender);

        final var captureLogMessage = ArgumentCaptor.forClass(ILoggingEvent.class);

        notificationPostProcessingService.processNotificationResult(notificationResultMessage);

        verify(appender).doAppend(captureLogMessage.capture());
        assertTrue(captureLogMessage.getValue().getFormattedMessage().contains("INFORMATION_TEXT"));
        assertEquals("INFO", captureLogMessage.getValue().getLevel().levelStr);
    }

    private NotificationResultMessage createNotificationResultMessage() {
        final var notificationResultMessage = new NotificationResultMessage();
        notificationResultMessage.setEvent(createEvent());
        notificationResultMessage.setCorrelationId(CORRELATION_ID);
        notificationResultMessage.setStatusUpdateXml(STATUS_UPDATE_XML);
        return notificationResultMessage;
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setId(EVENT_ID);
        event.setCode(TEST_EVENT_ENUM);
        event.setIntygsId(CERTIFICATE_ID);
        event.setEnhetsId(UNIT_ID);
        return event;
    }

    private NotificationResultType createNotificationResultType(NotificationResultTypeEnum resultType, NotificationErrorTypeEnum errorType,
        String resultText, String exception) {
        final var notificationResultType = new NotificationResultType();
        notificationResultType.setNotificationResult(resultType);
        notificationResultType.setNotificationErrorType(errorType);
        notificationResultType.setNotificationResultText(resultText);
        notificationResultType.setException(exception);
        return notificationResultType;
    }
}
