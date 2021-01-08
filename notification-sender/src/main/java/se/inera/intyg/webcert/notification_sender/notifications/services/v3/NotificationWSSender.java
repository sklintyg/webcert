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
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.dto.CertificateMessages;
import se.inera.intyg.webcert.notification_sender.notifications.dto.ExceptionInfoMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationRedeliveryMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

public class NotificationWSSender {

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
        //@Header(NotificationRouteHeaders.USER_ID) String userId,
        @Header(NotificationRouteHeaders.CORRELATION_ID) String correlationId,
        @Header(NotificationRouteHeaders.IS_FAILED_MESSAGE) Boolean isFailedMessage,
        @Header(NotificationRouteHeaders.IS_MANUAL_REDELIVERY) Boolean isManualRedelivery,
        @Header(Constants.JMS_TIMESTAMP) Long messageTimestamp) {

        //if (Objects.nonNull(userId)) {
        //    statusUpdate.setHanteratAv(NotificationRedeliveryUtil.getIIType(new HsaId(), userId, HSA_ID_OID));
        //}

        final NotificationResultMessage resultMessage = new NotificationResultMessage();
        resultMessage.setCertificateId(certificateId);
        resultMessage.setCorrelationId(correlationId);
        resultMessage.setLogicalAddress(logicalAddress);
        resultMessage.setIsFailedMessage(isFailedMessage);
        resultMessage.setIsManualRedelivery(isManualRedelivery);
        resultMessage.setEvent(extractEventFromStatusUpdate(statusUpdate));
        resultMessage.setNotificationRedeliveryMessage(getRedeliveryMessage(statusUpdate));

        try {
            LOG.debug("Sending status update to care: {} with request: {}", resultMessage, statusUpdate);
            if (!isFailedMessage) {
                ResultType resultType = statusUpdateForCareClient.certificateStatusUpdateForCare(logicalAddress, statusUpdate).getResult();
                resultMessage.setResultType(new NotificationResultType(resultType));
            } else {
                resultMessage.setResultType(new NotificationResultType(NotificationResultTypeEnum.ERROR, "Exception occured in"
                    + " NotificationTransformer", NotificationErrorTypeEnum.WEBCERT_FAILURE));
            }
        } catch (Exception e) {
            LOG.warn("Runtime exception occurred during status update for care {} with error message: {}", resultMessage, e);
            resultMessage.setExceptionInfoMessage(new ExceptionInfoMessage(e));
        } finally {
            postProcessSendResult(resultMessage);
        }
    }

    private void postProcessSendResult(NotificationResultMessage resultMessage) {

        final String notificationMessageAsJson = messageToJson(resultMessage);

        try {
            LOG.debug("Sending notification result message to postprocessor: {}", resultMessage);

            jmsTemplateNotificationPostProcessing.send(session -> {
                TextMessage textMessage = session.createTextMessage(notificationMessageAsJson);
                textMessage.setStringProperty(NotificationRouteHeaders.INTYGS_ID, resultMessage.getCertificateId());
                textMessage.setStringProperty(NotificationRouteHeaders.CORRELATION_ID, resultMessage.getCorrelationId());
                textMessage.setStringProperty(NotificationRouteHeaders.LOGISK_ADRESS, resultMessage.getLogicalAddress());
                textMessage.setStringProperty(NotificationRouteHeaders.HANDELSE, resultMessage.getEvent().getCode().value());
                return textMessage;
            });
        } catch (Exception e) {
            LOG.error("Runtime exception occurred when sending {} to postprocessing with error message: {}", resultMessage, e);
            // TODO monitorlog this exception
        }
    }

    private NotificationRedeliveryMessage getRedeliveryMessage(CertificateStatusUpdateForCareType statusUpdate) {
        final NotificationRedeliveryMessage redeliveryMessage = new NotificationRedeliveryMessage();
        redeliveryMessage.setCert(statusUpdate.getIntyg());
        redeliveryMessage.setSent(statusUpdate.getSkickadeFragor() != null
            ? new CertificateMessages(statusUpdate.getSkickadeFragor()) : null);
        redeliveryMessage.setReceived(statusUpdate.getMottagnaFragor() != null
            ? new CertificateMessages(statusUpdate.getMottagnaFragor()) : null);
        redeliveryMessage.setReference(statusUpdate.getRef());
        return redeliveryMessage;
    }

    private String messageToJson(NotificationResultMessage notificationMessage) {
        try {
            return objectMapper.writeValueAsString(notificationMessage);
        } catch (JsonProcessingException e) {
            LOG.error("Exception occured when trying to create and marshall NotificationWSResultMessage.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    private Handelse extractEventFromStatusUpdate(CertificateStatusUpdateForCareType statusUpdate) {
        Amneskod topicCode = statusUpdate.getHandelse().getAmne();
        HsaId userId = statusUpdate.getHanteratAv();

        Handelse event = new Handelse();
        event.setCode(HandelsekodEnum.fromValue(statusUpdate.getHandelse().getHandelsekod().getCode()));
        event.setEnhetsId(statusUpdate.getIntyg().getSkapadAv().getEnhet().getEnhetsId().getExtension());
        event.setIntygsId(statusUpdate.getIntyg().getIntygsId().getExtension());
        event.setCertificateType(statusUpdate.getIntyg().getTyp().getCode());
        event.setCertificateVersion(statusUpdate.getIntyg().getVersion());
        event.setCertificateIssuer(statusUpdate.getIntyg().getSkapadAv().getPersonalId().getExtension());
        event.setPersonnummer(statusUpdate.getIntyg().getPatient().getPersonId().getExtension());
        event.setTimestamp(statusUpdate.getHandelse().getTidpunkt());
        event.setVardgivarId(statusUpdate.getIntyg().getSkapadAv().getEnhet().getVardgivare().getVardgivareId().getExtension());
        event.setAmne(topicCode != null ? ArendeAmne.valueOf(topicCode.getCode()) : null);
        event.setSistaDatumForSvar(statusUpdate.getHandelse().getSistaDatumForSvar());
        event.setHanteratAv(userId != null ? userId.getExtension() : null);
        return event;
    }
}
