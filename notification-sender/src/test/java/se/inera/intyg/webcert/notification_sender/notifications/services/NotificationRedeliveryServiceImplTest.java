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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationDeliveryStatusEnum;
import se.inera.intyg.webcert.common.enumerations.NotificationRedeliveryStrategyEnum;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategy;
import se.inera.intyg.webcert.notification_sender.notifications.strategy.NotificationRedeliveryStrategyFactory;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.notification.model.NotificationRedelivery;
import se.inera.intyg.webcert.persistence.notification.repository.NotificationRedeliveryRepository;

@RunWith(MockitoJUnitRunner.class)
public class NotificationRedeliveryServiceImplTest {

    @Mock
    private MonitoringLogService logService;

    @Mock
    private HandelseRepository handelseRepository;

    @Mock
    private NotificationRedeliveryRepository notificationRedeliveryRepository;

    @Mock
    private NotificationRedeliveryStrategyFactory notificationRedeliveryStrategyFactory;

    @InjectMocks
    private NotificationRedeliveryServiceImpl notificationRedeliveryService;

    @Test
    public void testHandleNotificationSuccess() {
        final Handelse event = createEvent(NotificationDeliveryStatusEnum.SUCCESS);

        final NotificationResultType notificationResultType = createNotificationResultType(NotificationResultTypeEnum.OK,
            null,
            null);

        final NotificationResultMessage resultMessage = createNotificationResultMessage(event, notificationResultType);

        doReturn(event).when(handelseRepository).save(event);

        notificationRedeliveryService.handleNotificationSuccess(resultMessage);

        verifyEventCreated(event);
        verifyMonitorLoggingSuccess(event, resultMessage);
        verifyNoMoreInteractions(handelseRepository);
        verifyNoMoreInteractions(logService);
    }

    @Test
    public void testHandleNotificationSuccessForManualRedelivery() {
        final Handelse event = createEvent(NotificationDeliveryStatusEnum.SUCCESS);

        final NotificationResultType notificationResultType = createNotificationResultType(NotificationResultTypeEnum.OK,
            null,
            null);

        final NotificationResultMessage resultMessage = createNotificationResultMessage(event, notificationResultType);

        final NotificationRedeliveryStrategy notificationRedeliveryStrategy = createNotificationRedeliveryStrategy(
            NotificationRedeliveryStrategyEnum.MANUAL);

        final NotificationRedelivery notificationRedelivery = createNotificationRedelivery(event,
            resultMessage, LocalDateTime.now(), 1, notificationRedeliveryStrategy);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(resultMessage.getCorrelationId());

        doReturn(Optional.of(event)).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doReturn(event).when(handelseRepository).save(event);

        notificationRedeliveryService.handleNotificationSuccess(resultMessage);

        verifyEventUpdated(event);
        verifyMonitorLoggingSuccess(event, resultMessage);
        verifyNoMoreInteractions(handelseRepository);
        verifyNoMoreInteractions(logService);
    }

    @Test
    public void testHandleNotificationFailed() {
        final Handelse event = createEvent(NotificationDeliveryStatusEnum.FAILURE);

        final NotificationResultType notificationResultType = createNotificationResultType(NotificationResultTypeEnum.ERROR,
            "RESULT_TEXT", NotificationErrorTypeEnum.APPLICATION_ERROR);

        final NotificationResultMessage resultMessage = createNotificationResultMessage(event, notificationResultType);

        final NotificationRedelivery notificationRedelivery = createNotificationRedelivery(event, resultMessage, LocalDateTime.now(), 1,
            createNotificationRedeliveryStrategy(NotificationRedeliveryStrategyEnum.STANDARD));

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(resultMessage.getCorrelationId());

        doReturn(Optional.of(event)).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doReturn(event).when(handelseRepository).save(event);

        notificationRedeliveryService.handleNotificationFailure(resultMessage);

        verifyEventUpdated(event);
        verifyMonitorLoggingFailure(event, notificationResultType, resultMessage, 1);
        verifyNoMoreInteractions(handelseRepository);
        verifyNoMoreInteractions(logService);
    }

    @Test
    public void testHandleNotificationFailedForManualRedelivery() {
        final Handelse event = createEvent(NotificationDeliveryStatusEnum.FAILURE);

        final NotificationResultType notificationResultType = createNotificationResultType(NotificationResultTypeEnum.ERROR,
            "RESULT_TEXT", NotificationErrorTypeEnum.APPLICATION_ERROR);

        final NotificationResultMessage resultMessage = createNotificationResultMessage(event, notificationResultType);

        final NotificationRedelivery notificationRedelivery = createNotificationRedelivery(event, resultMessage, LocalDateTime.now(), 1,
            createNotificationRedeliveryStrategy(NotificationRedeliveryStrategyEnum.MANUAL));

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(resultMessage.getCorrelationId());

        doReturn(Optional.of(event)).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doReturn(event).when(handelseRepository).save(event);

        notificationRedeliveryService.handleNotificationFailure(resultMessage);

        verifyEventUpdated(event);
        verifyMonitorLoggingFailure(event, notificationResultType, resultMessage, 1);
        verifyNoMoreInteractions(handelseRepository);
        verifyNoMoreInteractions(logService);
    }

    @Test
    public void testHandleNotificationResend() {
        final Handelse event = createEvent(NotificationDeliveryStatusEnum.RESEND);

        final NotificationResultType notificationResultType = createNotificationResultType(NotificationResultTypeEnum.ERROR,
            "RESULT_TEXT", NotificationErrorTypeEnum.APPLICATION_ERROR);

        final NotificationResultMessage resultMessage = createNotificationResultMessage(event, notificationResultType);

        final NotificationRedeliveryStrategy notificationRedeliveryStrategy = createNotificationRedeliveryStrategy(
            NotificationRedeliveryStrategyEnum.STANDARD);

        final NotificationRedelivery notificationRedelivery = createNotificationRedelivery(event,
            resultMessage, LocalDateTime.now(), 1, notificationRedeliveryStrategy);

        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(NotificationRedeliveryStrategyEnum.STANDARD);

        doReturn(event).when(handelseRepository).save(event);
        doReturn(notificationRedelivery).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationRedeliveryService.handleNotificationResend(resultMessage);

        verifyNotificationRedeliveryCreated(notificationRedelivery);
        verifyEventCreated(event);
        verifyMonitorLoggingResend(event, notificationResultType, resultMessage, 1, notificationRedelivery.getRedeliveryTime());
        verifyNoMoreInteractions(handelseRepository);
        verifyNoMoreInteractions(logService);
    }

    @Test
    public void testHandleNotificationResendForManualRedelivery() {
        final Handelse event = createEvent(NotificationDeliveryStatusEnum.RESEND);

        final NotificationResultType notificationResultType = createNotificationResultType(NotificationResultTypeEnum.ERROR,
            "RESULT_TEXT", NotificationErrorTypeEnum.APPLICATION_ERROR);

        final NotificationResultMessage resultMessage = createNotificationResultMessage(event, notificationResultType);

        final NotificationRedeliveryStrategy notificationRedeliveryStrategy = createNotificationRedeliveryStrategy(
            NotificationRedeliveryStrategyEnum.MANUAL);

        final NotificationRedelivery notificationRedelivery = createNotificationRedelivery(event,
            resultMessage, LocalDateTime.now(), 1, notificationRedeliveryStrategy);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(resultMessage.getCorrelationId());

        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(NotificationRedeliveryStrategyEnum.MANUAL);

        doReturn(Optional.of(event)).when(handelseRepository).findById(notificationRedelivery.getEventId());
        doReturn(event).when(handelseRepository).save(event);

        doReturn(notificationRedelivery).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationRedeliveryService.handleNotificationResend(resultMessage);

        verifyNotificationRedeliveryCreated(notificationRedelivery);
        verifyEventUpdated(event);
        verifyMonitorLoggingResend(event, notificationResultType, resultMessage, 2, notificationRedelivery.getRedeliveryTime());
        verifyNoMoreInteractions(handelseRepository);
        verifyNoMoreInteractions(logService);
    }

    @Test
    public void testHandleNotificationResendAttemptTwo() {
        final Handelse event = createEvent(NotificationDeliveryStatusEnum.RESEND);

        final NotificationResultType notificationResultType = createNotificationResultType(NotificationResultTypeEnum.ERROR,
            "RESULT_TEXT", NotificationErrorTypeEnum.APPLICATION_ERROR);

        final NotificationResultMessage resultMessage = createNotificationResultMessage(event, notificationResultType);

        final NotificationRedeliveryStrategy notificationRedeliveryStrategy = createNotificationRedeliveryStrategy(
            NotificationRedeliveryStrategyEnum.STANDARD);

        final NotificationRedelivery notificationRedelivery = createNotificationRedelivery(event,
            resultMessage, LocalDateTime.now(), 1, notificationRedeliveryStrategy);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(resultMessage.getCorrelationId());

        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(NotificationRedeliveryStrategyEnum.STANDARD);

        doReturn(Optional.of(event)).when(handelseRepository).findById(event.getId());
        doReturn(event).when(handelseRepository).save(event);

        doReturn(notificationRedelivery).when(notificationRedeliveryRepository).save(any(NotificationRedelivery.class));

        notificationRedeliveryService.handleNotificationResend(resultMessage);

        verifyNotificationRedeliveryCreated(notificationRedelivery);
        verifyEventUpdated(event);
        verifyMonitorLoggingResend(event, notificationResultType, resultMessage, 2, notificationRedelivery.getRedeliveryTime());
        verifyNoMoreInteractions(handelseRepository);
        verifyNoMoreInteractions(logService);
    }

    @Test
    public void testHandleNotificationResendLastAttemptFailed() {
        final Handelse event = createEvent(NotificationDeliveryStatusEnum.FAILURE);

        final NotificationResultType notificationResultType = createNotificationResultType(NotificationResultTypeEnum.ERROR,
            "RESULT_TEXT", NotificationErrorTypeEnum.APPLICATION_ERROR);

        final NotificationResultMessage resultMessage = createNotificationResultMessage(event, notificationResultType);

        final NotificationRedeliveryStrategy notificationRedeliveryStrategy = createNotificationRedeliveryStrategy(
            NotificationRedeliveryStrategyEnum.STANDARD);

        final NotificationRedelivery notificationRedelivery = createNotificationRedelivery(event,
            resultMessage, LocalDateTime.now(), 30, notificationRedeliveryStrategy);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(resultMessage.getCorrelationId());

        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(NotificationRedeliveryStrategyEnum.STANDARD);

        doReturn(Optional.of(event)).when(handelseRepository).findById(event.getId());
        doReturn(event).when(handelseRepository).save(event);

        notificationRedeliveryService.handleNotificationResend(resultMessage);

        verifyNotificationRedeliveryDeleted(notificationRedelivery);
        verifyEventUpdated(event);
        verifyMonitorLoggingFailure(event, notificationResultType, resultMessage, 31);
        verifyNoMoreInteractions(handelseRepository);
        verifyNoMoreInteractions(logService);
    }

    @Test
    public void testHandleNotificationResendForManualRedeliveryLastAttemptFailed() {
        final Handelse event = createEvent(NotificationDeliveryStatusEnum.FAILURE);

        final NotificationResultType notificationResultType = createNotificationResultType(NotificationResultTypeEnum.ERROR,
            "RESULT_TEXT", NotificationErrorTypeEnum.APPLICATION_ERROR);

        final NotificationResultMessage resultMessage = createNotificationResultMessage(event, notificationResultType);

        final NotificationRedeliveryStrategy notificationRedeliveryStrategy = createNotificationRedeliveryStrategy(
            NotificationRedeliveryStrategyEnum.MANUAL);

        final NotificationRedelivery notificationRedelivery = createNotificationRedelivery(event,
            resultMessage, LocalDateTime.now(), 30, notificationRedeliveryStrategy);

        doReturn(Optional.of(notificationRedelivery)).when(notificationRedeliveryRepository)
            .findByCorrelationId(resultMessage.getCorrelationId());

        doReturn(notificationRedeliveryStrategy).when(notificationRedeliveryStrategyFactory)
            .getResendStrategy(NotificationRedeliveryStrategyEnum.MANUAL);

        doReturn(Optional.of(event)).when(handelseRepository).findById(event.getId());
        doReturn(event).when(handelseRepository).save(event);

        notificationRedeliveryService.handleNotificationResend(resultMessage);

        verifyNotificationRedeliveryDeleted(notificationRedelivery);
        verifyEventUpdated(event);
        verifyMonitorLoggingFailure(event, notificationResultType, resultMessage, 31);
        verifyNoMoreInteractions(handelseRepository);
        verifyNoMoreInteractions(logService);
    }

    private void verifyNotificationRedeliveryCreated(NotificationRedelivery notificationRedelivery) {
        verify(notificationRedeliveryRepository).save(argThat(new ArgumentMatcher<NotificationRedelivery>() {
            @Override
            public boolean matches(NotificationRedelivery argument) {
                if (argument == null) {
                    return false;
                }

                if (!notificationRedelivery.getCorrelationId().equalsIgnoreCase(argument.getCorrelationId())) {
                    return false;
                }

                if (notificationRedelivery.getEventId() != argument.getEventId()) {
                    return false;
                }

                if (notificationRedelivery.getAttemptedDeliveries() != argument.getAttemptedDeliveries()) {
                    return false;
                }

                return true;
            }
        }));
    }

    private void verifyNotificationRedeliveryDeleted(NotificationRedelivery notificationRedelivery) {
        verify(notificationRedeliveryRepository).delete(notificationRedelivery);
    }

    private void verifyEventCreated(Handelse event) {
        verify(handelseRepository).save(event);
    }

    private void verifyEventUpdated(Handelse event) {
        verify(handelseRepository).findById(event.getId());
        verify(handelseRepository).save(event);
    }

    private void verifyMonitorLoggingFailure(Handelse event, NotificationResultType notificationResultType,
        NotificationResultMessage resultMessage, int sendAttempt) {
        verify(logService).logStatusUpdateForCareStatusFailure(
            event.getId(),
            event.getCode().name(),
            event.getEnhetsId(),
            event.getIntygsId(),
            resultMessage.getCorrelationId(),
            notificationResultType.getNotificationErrorType().value(),
            notificationResultType.getNotificationResultText(),
            sendAttempt);
    }

    private void verifyMonitorLoggingResend(Handelse event, NotificationResultType notificationResultType,
        NotificationResultMessage resultMessage, int sendAttempt, LocalDateTime resendAttemptTimestamp) {
        verify(logService).logStatusUpdateForCareStatusResend(
            event.getId(),
            event.getCode().name(),
            event.getEnhetsId(),
            event.getIntygsId(),
            resultMessage.getCorrelationId(),
            notificationResultType.getNotificationErrorType().value(),
            notificationResultType.getNotificationResultText(),
            sendAttempt,
            resendAttemptTimestamp);
    }

    private void verifyMonitorLoggingSuccess(Handelse event, NotificationResultMessage resultMessage) {
        verify(logService).logStatusUpdateForCareStatusSuccess(
            event.getId(),
            event.getCode().name(),
            event.getIntygsId(),
            resultMessage.getCorrelationId(),
            event.getEnhetsId());
    }

    private NotificationResultMessage createNotificationResultMessage(Handelse event, NotificationResultType notificationResultType) {
        final NotificationResultMessage resultMessage = new NotificationResultMessage();
        resultMessage.setEvent(event);
        resultMessage.setResultType(notificationResultType);
        resultMessage.setRedeliveryMessageBytes(null);
        resultMessage.setCorrelationId("CORRELATION_ID");
        return resultMessage;
    }

    private NotificationResultType createNotificationResultType(NotificationResultTypeEnum type, String resultText,
        NotificationErrorTypeEnum errorType) {
        final NotificationResultType notificationResultType = new NotificationResultType();
        notificationResultType.setNotificationResult(type);
        notificationResultType.setNotificationResultText(resultText);
        notificationResultType.setNotificationErrorType(errorType);
        return notificationResultType;
    }

    private Handelse createEvent(NotificationDeliveryStatusEnum notificationDeliveryStatusEnum) {
        final Handelse event = new Handelse();
        event.setDeliveryStatus(notificationDeliveryStatusEnum);
        event.setCertificateIssuer("CERTIFICATE_ISSUER");
        event.setCertificateType("CERTIFICATE_TYPE");
        event.setCode(HandelsekodEnum.SKAPAT);
        event.setCertificateVersion("CERTIFICATE_VERSION");
        event.setEnhetsId("UNIT_ID");
        event.setHanteratAv("HANDLE_BY");
        event.setId(1000L);
        event.setIntygsId("CERTIFICATE_ID");
        event.setPersonnummer("191212121212");
        event.setTimestamp(LocalDateTime.now());
        event.setVardgivarId("CAREPROVIDER_ID");
        return event;
    }

    private NotificationRedeliveryStrategy createNotificationRedeliveryStrategy(
        NotificationRedeliveryStrategyEnum notificationRedeliveryStrategyEnum) {
        final var notificationRedeliveryStrategy = mock(NotificationRedeliveryStrategy.class);
        doReturn(notificationRedeliveryStrategyEnum).when(notificationRedeliveryStrategy).getName();
        doReturn(1).when(notificationRedeliveryStrategy).getNextTimeValue(anyInt());
        doReturn(ChronoUnit.MINUTES).when(notificationRedeliveryStrategy).getNextTimeUnit(anyInt());
        doReturn(30).when(notificationRedeliveryStrategy).getMaxRedeliveries();
        return notificationRedeliveryStrategy;
    }

    private NotificationRedelivery createNotificationRedelivery(Handelse event, NotificationResultMessage resultMessage,
        LocalDateTime redeliveryTime, int attemptedDeliveries, NotificationRedeliveryStrategy notificationRedeliveryStrategy) {
        final NotificationRedelivery notificationRedelivery = new NotificationRedelivery();
        notificationRedelivery.setEventId(event.getId());
        notificationRedelivery.setCorrelationId(resultMessage.getCorrelationId());
        notificationRedelivery.setAttemptedDeliveries(attemptedDeliveries);
        if (notificationRedeliveryStrategy != null) {
            notificationRedelivery.setRedeliveryStrategy(notificationRedeliveryStrategy.getName());
        }
        notificationRedelivery.setRedeliveryTime(redeliveryTime);
        return notificationRedelivery;
    }
}
