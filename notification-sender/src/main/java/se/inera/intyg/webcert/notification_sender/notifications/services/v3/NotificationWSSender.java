/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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

import static se.inera.intyg.common.support.Constants.HSA_ID_OID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import javax.jms.TextMessage;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.dto.ExceptionInfoTransporter;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationWSResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

public class NotificationWSSender <T extends Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationWSSender.class);

    @Autowired
    @Qualifier("jmsTemplateNotificationPostProcessing")
    private JmsTemplate jmsTemplateNotificationPostProcessing;

    @Autowired
    private CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendStatusUpdate(CertificateStatusUpdateForCareType statusUpdate,
        @Header(NotificationRouteHeaders.INTYGS_ID) String certificateId,
        @Header(NotificationRouteHeaders.LOGISK_ADRESS) String logicalAddress,
        @Header(NotificationRouteHeaders.USER_ID) String userId,
        @Header(NotificationRouteHeaders.CORRELATION_ID) String correlationId,
        @Header(Constants.JMS_TIMESTAMP) long messageTimestamp) {

        statusUpdate.setHanteratAv(userHsaId(userId));

        final NotificationWSResultMessage resultMessage = createResultMessage(statusUpdate, certificateId, logicalAddress, userId,
            correlationId, messageTimestamp);
        final String handledBy = (statusUpdate.getHanteratAv() == null) ? null : statusUpdate.getHanteratAv().getExtension();
        try {
            LOG.debug("Sending status update to care: {} with request: {}", resultMessage, statusUpdate);
            ResultType resultType = statusUpdateForCareClient.certificateStatusUpdateForCare(logicalAddress, statusUpdate).getResult();
            resultMessage.setResultType(resultType);
        } catch (Exception e) {
            LOG.warn("Runtime exception occurred during status update for care {} with error message: {}", resultMessage, e);
            resultMessage.setExceptionInfoTransporter(new ExceptionInfoTransporter(e));
        } finally {
            postProcessSendResult(resultMessage);
        }
    }

    private void postProcessSendResult(NotificationWSResultMessage resultMessage) {

        final String notificationMessageAsJson = messageToJson(resultMessage);

        try {
            LOG.debug("Sending status update for postprocessing with result message: {}", resultMessage);

            jmsTemplateNotificationPostProcessing.send(session -> {
                TextMessage textMessage = session.createTextMessage(notificationMessageAsJson);
                textMessage.setStringProperty(NotificationRouteHeaders.INTYGS_ID, resultMessage.getCertificateId());
                textMessage.setStringProperty(NotificationRouteHeaders.CORRELATION_ID, resultMessage.getCorrelationId());
                textMessage.setStringProperty(NotificationRouteHeaders.LOGISK_ADRESS, resultMessage.getLogicalAddress());
                textMessage.setStringProperty(NotificationRouteHeaders.HANDELSE, resultMessage.getStatusUpdate().getHandelse()
                    .getHandelsekod().getCode());
                return textMessage;
            });
        } catch (Exception e) {
            LOG.error("Runtime exception occurred when sending {} to postprocessing with error message: {}", resultMessage, e);
        }
    }

    private String messageToJson(NotificationWSResultMessage notificationMessage) {
        try {
            return objectMapper.writeValueAsString(notificationMessage);
        } catch (JsonProcessingException e) {
            LOG.error("Problem occured when trying to create and marshall NotificationWSResultMessage.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    private NotificationWSResultMessage createResultMessage(CertificateStatusUpdateForCareType statusUpdate, String certificateId,
        String logicalAddress, String userId, String correlationId, long messageTimestamp) {

        final NotificationWSResultMessage message = new NotificationWSResultMessage();
        message.setStatusUpdate(statusUpdate);
        message.setCertificateId(certificateId);
        message.setCorrelationId(correlationId);
        message.setLogicalAddress(logicalAddress);
        message.setUserId(userId);
        message.setMessageTimestamp(messageTimestamp);
        return message;
    }

    private HsaId userHsaId(String userId) {
        if (Objects.nonNull(userId)) {
            LOG.debug("Set hanteratAv to '{}'", userId);
            final HsaId hsaId = new HsaId();
            hsaId.setExtension(userId);
            hsaId.setRoot(HSA_ID_OID);
            return hsaId;
        }
        return null;
    }
}
