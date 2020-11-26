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
import javax.xml.ws.soap.SOAPFaultException;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
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

    private static final String NOTIFICATION_RESULT_RESEND = "RESEND";
    private static final String NOTIFICATION_RESULT_FAILURE = "FAILURE";
    private static final String NOTIFICATION_RESULT_INFO = "INFO";
    private static final String NOTIFICATION_RESULT_SUCCESS = "SUCCESS";
    private static final String MARSHALLING_ERROR = "Marshalling Error";
    private static final String UNMARSHALLING_ERROR = "Unmarshalling Error";

    public void sendStatusUpdate(CertificateStatusUpdateForCareType request,
        @Header(NotificationRouteHeaders.LOGISK_ADRESS) String logicalAddress,
        @Header(NotificationRouteHeaders.USER_ID) String userId,
        @Header(NotificationRouteHeaders.CORRELATION_ID) String correlationId,
        @Header(Constants.JMS_TIMESTAMP) long messageTimestamp) {

        final String messageInfo = String.format("[logicalAddress: %s, intygId: %s, correlationId: %s]", logicalAddress,
            request.getIntyg().getIntygsId().getExtension(), correlationId);

        String statusUpdateResult;
        String requestAsJson = "";

        if (Objects.nonNull(userId)) {
            LOG.debug("Set hanteratAv to '{}'", userId);
            request.setHanteratAv(userHsaId(userId));
        }

        try {
            LOG.debug("Sending status update: {} with request: {}", messageInfo, request);
            ResultType resultType = sendStatusUpdateToCare(logicalAddress, request);
            statusUpdateResult = handleResult(resultType);
            if (statusUpdateResult.equals(NOTIFICATION_RESULT_INFO)) {
                LOG.info("{} message: {}", messageInfo, resultType.getResultText());
                statusUpdateResult = NOTIFICATION_RESULT_SUCCESS;
            }
            requestAsJson = requestToJson(request); // SKA ALLTID FÅS MED
        } catch (TemporaryException tex) {
            LOG.warn("Temporary exception occurred during status update for care {} with error message: {}", messageInfo, tex);
            statusUpdateResult = NOTIFICATION_RESULT_RESEND;
        } catch (PermanentException pex) {
            LOG.error("Permanent exception occurred during status update for care {} with error message: {}", messageInfo, pex);
            statusUpdateResult = NOTIFICATION_RESULT_FAILURE;
        }

        final String finalStatusUpdateResult = statusUpdateResult;
        jmsTemplateNotificationPostProcessing.convertAndSend(requestAsJson, message -> {
                message.setStringProperty(NotificationRouteHeaders.LOGISK_ADRESS, logicalAddress);
                message.setStringProperty(NotificationRouteHeaders.USER_ID, userId);
                message.setStringProperty(NotificationRouteHeaders.CORRELATION_ID, correlationId);
                message.setStringProperty(NotificationRouteHeaders.NOTIFICATION_RESULT, finalStatusUpdateResult);
                message.setLongProperty(Constants.JMS_TIMESTAMP, messageTimestamp);

                return message;
            }
        ); // FINALLY BLOCK som typ ej får misslyckas FÖR ANNAT ÄN JMS EXCEPTION
    }

    private ResultType sendStatusUpdateToCare(String logicalAddress, CertificateStatusUpdateForCareType request)
        throws PermanentException, TemporaryException {
        try {
            return statusUpdateForCareClient.certificateStatusUpdateForCare(logicalAddress, request).getResult();
        } catch (Exception e) {
            if (isMarshallingError(e)) {
                throw new PermanentException(e);
            }
            throw new TemporaryException(e);
        }
    }

    private String handleResult(ResultType result) throws TemporaryException, PermanentException {
        switch (result.getResultCode()) {
            case ERROR:
                return handleError(result);
            case INFO:
                return NOTIFICATION_RESULT_INFO;
            default:
                return NOTIFICATION_RESULT_SUCCESS;
        }
    }

    private String requestToJson(CertificateStatusUpdateForCareType request) throws PermanentException {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new PermanentException(e);
        }
    }

    private String handleError(ResultType result) throws TemporaryException, PermanentException {
        String errorMessage = String.format("Error code: %s and error message: \"%s\"", result.getErrorId(), result.getResultText());
        if (ErrorIdType.TECHNICAL_ERROR.equals(result.getErrorId())) {
            throw new TemporaryException(errorMessage);
        }
        throw new PermanentException(errorMessage);
    }

    private boolean isMarshallingError(Exception e) {
        if (e instanceof SOAPFaultException) {
            final String msg = e.getMessage();
            return Objects.nonNull(msg) && (msg.contains(MARSHALLING_ERROR) || msg.contains(UNMARSHALLING_ERROR));
        }
        return false;
    }

    HsaId userHsaId(String id) {
        final HsaId hsaId = new HsaId();
        hsaId.setExtension(id);
        hsaId.setRoot(HSA_ID_OID);
        return hsaId;
    }
}
