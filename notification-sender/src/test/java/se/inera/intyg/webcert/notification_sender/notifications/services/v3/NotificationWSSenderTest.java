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
package se.inera.intyg.webcert.notification_sender.notifications.services.v3;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageCreator;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageSender;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponseType;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@RunWith(MockitoJUnitRunner.class)
public class NotificationWSSenderTest {

    @Mock
    private CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;
    @Mock
    private NotificationResultMessageCreator notificationResultMessageCreator;
    @Mock
    private NotificationResultMessageSender notificationResultMessageSender;
    @Spy
    private MdcHelper mdcHelper;

    @InjectMocks
    private NotificationWSSender notificationWSSender;

    private static final String CERTIFICATE_ID = "testCertificateId";
    private static final String LOGICAL_ADDRESS = "testLogicalAddress";
    private static final String USER_ID = "testUser";
    private static final String CORRELATION_ID = "testCorrelationId";

    @Test
    public void shallUpdateHandleByIfUserIdHeaderIsSet() {
        final var statusUpdateMock = mock(CertificateStatusUpdateForCareType.class);
        final var argumentCaptor = ArgumentCaptor.forClass(HsaId.class);

        doReturn(mock(CertificateStatusUpdateForCareResponseType.class)).when(statusUpdateForCareClient)
            .certificateStatusUpdateForCare(LOGICAL_ADDRESS, statusUpdateMock);

        notificationWSSender.sendStatusUpdate(statusUpdateMock, CERTIFICATE_ID, LOGICAL_ADDRESS, USER_ID, CORRELATION_ID);

        verify(statusUpdateMock).setHanteratAv(argumentCaptor.capture());
        final var actualUserId = argumentCaptor.getValue() != null ? argumentCaptor.getValue().getExtension() : null;
        assertEquals(USER_ID, actualUserId);
    }

    @Test
    public void shallLeaveHandleByUnchangedIfUserIdHeaderIsMissing() {
        final var statusUpdateMock = mock(CertificateStatusUpdateForCareType.class);

        doReturn(mock(CertificateStatusUpdateForCareResponseType.class)).when(statusUpdateForCareClient)
            .certificateStatusUpdateForCare(LOGICAL_ADDRESS, statusUpdateMock);

        notificationWSSender.sendStatusUpdate(statusUpdateMock, CERTIFICATE_ID, LOGICAL_ADDRESS, null, CORRELATION_ID);

        verify(statusUpdateMock, never()).setHanteratAv(any());
    }

    @Test
    public void shallAddResultTypeToNotificationResultMessageWhenCertificateStatusUpdateForCareReturns() {
        final var statusUpdateMock = mock(CertificateStatusUpdateForCareType.class);
        final var certificateStatusUpdateForCareResponseType = mock(CertificateStatusUpdateForCareResponseType.class);
        final var result = mock(ResultType.class);

        final var argumentCaptor = ArgumentCaptor.forClass(ResultType.class);
        doReturn(result).when(certificateStatusUpdateForCareResponseType).getResult();
        doReturn(certificateStatusUpdateForCareResponseType).when(statusUpdateForCareClient)
            .certificateStatusUpdateForCare(LOGICAL_ADDRESS, statusUpdateMock);

        notificationWSSender.sendStatusUpdate(statusUpdateMock, CERTIFICATE_ID, LOGICAL_ADDRESS, USER_ID, CORRELATION_ID);

        verify(notificationResultMessageCreator).addToResultMessage(any(), any(), argumentCaptor.capture());
        assertEquals("Result type should be added to the result message", result, argumentCaptor.getValue());
    }

    @Test
    public void shallAddExceptionToNotificationResultMessageWhenCertificateStatusUpdateForCareThrowsException() {
        final var statusUpdateMock = mock(CertificateStatusUpdateForCareType.class);
        final var exception = new RuntimeException();

        final var argumentCaptor = ArgumentCaptor.forClass(Exception.class);
        doThrow(exception).when(statusUpdateForCareClient).certificateStatusUpdateForCare(LOGICAL_ADDRESS, statusUpdateMock);

        notificationWSSender.sendStatusUpdate(statusUpdateMock, CERTIFICATE_ID, LOGICAL_ADDRESS, USER_ID, CORRELATION_ID);

        verify(notificationResultMessageCreator).addToResultMessage(any(), any(), argumentCaptor.capture());
        assertEquals("Exception should be added to the result message", exception, argumentCaptor.getValue());
    }

    @Test
    public void shallSendNotificationResultMessageWhenCertificateStatusUpdateForCareReturns() {
        final var statusUpdateMock = mock(CertificateStatusUpdateForCareType.class);
        final var expectedResultMessage = mock(NotificationResultMessage.class);
        final var argumentCaptor = ArgumentCaptor.forClass(NotificationResultMessage.class);

        doReturn(mock(CertificateStatusUpdateForCareResponseType.class)).when(statusUpdateForCareClient)
            .certificateStatusUpdateForCare(LOGICAL_ADDRESS, statusUpdateMock);
        doReturn(expectedResultMessage).when(notificationResultMessageCreator).createResultMessage(statusUpdateMock, CORRELATION_ID);

        notificationWSSender.sendStatusUpdate(statusUpdateMock, CERTIFICATE_ID, LOGICAL_ADDRESS, USER_ID, CORRELATION_ID);

        verify(notificationResultMessageSender).sendResultMessage(argumentCaptor.capture());
        assertEquals(expectedResultMessage, argumentCaptor.getValue());
    }

    @Test
    public void shallSendNotificationResultMessageWhenCertificateStatusUpdateForCareThrowsException() {
        final var statusUpdateMock = mock(CertificateStatusUpdateForCareType.class);
        final var expectedResultMessage = mock(NotificationResultMessage.class);
        final var argumentCaptor = ArgumentCaptor.forClass(NotificationResultMessage.class);

        doThrow(new RuntimeException()).when(statusUpdateForCareClient).certificateStatusUpdateForCare(LOGICAL_ADDRESS, statusUpdateMock);
        doReturn(expectedResultMessage).when(notificationResultMessageCreator).createResultMessage(statusUpdateMock, CORRELATION_ID);

        notificationWSSender.sendStatusUpdate(statusUpdateMock, CERTIFICATE_ID, LOGICAL_ADDRESS, USER_ID, CORRELATION_ID);

        verify(notificationResultMessageSender).sendResultMessage(argumentCaptor.capture());
        assertEquals(expectedResultMessage, argumentCaptor.getValue());
    }
}
