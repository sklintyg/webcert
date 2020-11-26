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
import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import javax.jms.JMSException;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import se.inera.intyg.webcert.common.Constants;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;

public class NotificationWSSender {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationWSSender.class);

    @Autowired
    private CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("jmsTemplateNotificationPostProcessing")
    private JmsTemplate jmsTemplateNotificationPostProcessing;

    public void sendStatusUpdate(CertificateStatusUpdateForCareType statusMessage,
        @Header(NotificationRouteHeaders.LOGISK_ADRESS) String logicalAddress,
        @Header(NotificationRouteHeaders.USER_ID) String userId,
        @Header(NotificationRouteHeaders.CORRELATION_ID) String correlationId,
        @Header(NotificationRouteHeaders.INTYGS_ID) String certificateId,
        @Header(Constants.JMS_TIMESTAMP) long messageTimestamp) throws JMSException {

        statusMessage.setHanteratAv(getUserHsaId(userId));

        NotificationWSResultMessage notificationWSMessage = new NotificationWSResultMessage();
        notificationWSMessage.setCorrelationId(correlationId);
        notificationWSMessage.setLogicalAddress(logicalAddress);
        notificationWSMessage.setUserId(userId);
        notificationWSMessage.setStatusMessage(statusMessage);
        notificationWSMessage.setCertificateId(certificateId);

        try {
            LOG.debug("Sending status update to care: {}", notificationWSMessage);
            ResultType resultType = statusUpdateForCareClient.certificateStatusUpdateForCare(logicalAddress, statusMessage).getResult();
            notificationWSMessage.setResultType(resultType);
        } catch (Exception e) {
            notificationWSMessage.setException(e);
        }

        JsonProcessingException jsonProcessingException = null;
        String notificationWSMessageJson = "";
        try {
            notificationWSMessageJson = objectMapper.writeValueAsString(notificationWSMessage);
        } catch (JsonProcessingException e) {
            jsonProcessingException = e;
        } finally {
            LOG.debug("Sending notification message for postprocessing: {}", notificationWSMessage);
            final JsonProcessingException jsonProcessingExceptionFinal = jsonProcessingException;
            jmsTemplateNotificationPostProcessing.convertAndSend(notificationWSMessageJson, jmsMessage -> {
                if (jsonProcessingExceptionFinal != null) {
                    jmsMessage.setObjectProperty(NotificationRouteHeaders.JSON_EXCEPTION, ImmutableMap.of(
                        NotificationRouteHeaders.JSON_EXCEPTION, jsonProcessingExceptionFinal));
                }
                return jmsMessage;
            });
        }
    }

    HsaId getUserHsaId(String userId) {
        if (Objects.nonNull(userId)) {
            final HsaId hsaId = new HsaId();
            hsaId.setExtension(userId);
            hsaId.setRoot(HSA_ID_OID);
            return hsaId;
        } else {
            return null;
        }
    }
}
