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

import static se.inera.intyg.common.support.Constants.HSA_ID_OID;

import java.util.Objects;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.logging.PerformanceLogging;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageCreator;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageSender;
import se.inera.intyg.webcert.notification_sender.notifications.util.NotificationRedeliveryUtil;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;

@Component
public class NotificationWSSender {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationWSSender.class);

    private final CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;

    private final NotificationResultMessageCreator notificationResultMessageCreator;

    private final NotificationResultMessageSender notificationResultMessageSender;

    public NotificationWSSender(
        CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient,
        NotificationResultMessageCreator notificationResultMessageCreator,
        NotificationResultMessageSender notificationResultMessageSender) {
        this.statusUpdateForCareClient = statusUpdateForCareClient;
        this.notificationResultMessageCreator = notificationResultMessageCreator;
        this.notificationResultMessageSender = notificationResultMessageSender;
    }


    @PerformanceLogging(eventAction = "send-status-update-for-care", eventType = MdcLogConstants.EVENT_TYPE_INFO)
    public void sendStatusUpdate(CertificateStatusUpdateForCareType statusUpdate,
        @Header(NotificationRouteHeaders.INTYGS_ID) String certificateId,
        @Header(NotificationRouteHeaders.LOGISK_ADRESS) String logicalAddress,
        @Header(NotificationRouteHeaders.USER_ID) String userId,
        @Header(NotificationRouteHeaders.CORRELATION_ID) String correlationId) {

        if (Objects.nonNull(userId)) {
            statusUpdate.setHanteratAv(NotificationRedeliveryUtil.getIIType(new HsaId(), userId, HSA_ID_OID));
        }

        final var resultMessage = notificationResultMessageCreator.createResultMessage(statusUpdate, correlationId);

        try {
            LOG.debug("Sending status update to care: {} with request: {}", resultMessage, statusUpdate);
            final var resultType = statusUpdateForCareClient.certificateStatusUpdateForCare(logicalAddress, statusUpdate).getResult();
            notificationResultMessageCreator.addToResultMessage(resultMessage, statusUpdate, resultType);
        } catch (Exception e) {
            LOG.warn("Runtime exception occurred during status update for care {} with error message: {}", resultMessage, e);
            notificationResultMessageCreator.addToResultMessage(resultMessage, statusUpdate, e);
        } finally {
            notificationResultMessageSender.sendResultMessage(resultMessage);
        }
    }
}
