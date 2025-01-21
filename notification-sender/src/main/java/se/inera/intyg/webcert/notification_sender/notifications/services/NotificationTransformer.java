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
package se.inera.intyg.webcert.notification_sender.notifications.services;

import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYG_TYPE_VERSION;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.common.support.xml.XmlMarshallerHelper;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageCreator;
import se.inera.intyg.webcert.notification_sender.notifications.services.postprocessing.NotificationResultMessageSender;
import se.inera.intyg.webcert.notification_sender.notifications.services.v3.CertificateStatusUpdateForCareCreator;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

public class NotificationTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationTransformer.class);

    @Autowired
    private IntygModuleRegistry moduleRegistry;
    @Autowired
    private CertificateStatusUpdateForCareCreator certificateStatusUpdateForCareCreator;
    @Autowired
    private NotificationResultMessageCreator notificationResultMessageCreator;
    @Autowired
    private NotificationResultMessageSender notificationResultMessageSender;
    @Autowired
    private MdcHelper mdcHelper;

    /**
     * Process message by adding headers and creating a CertificateStatusUpdateForCareType based on the {@link NotificationMessage} and
     * set it as the message body.
     *
     * Failure to do so will result in an error and if a TemporaryException is thrown, the message will be processed again. If
     * it resulted in a different exception a NotificationResultMessage will be created and added to the queue for post processing.
     */
    public void process(Message message) throws ModuleException, IOException, ModuleNotFoundException, TemporaryException {
        LOG.debug("Receiving message: {}", message.getMessageId());

        final var notificationMessage = message.getBody(NotificationMessage.class);

        try {
            final var certificateId = notificationMessage.getIntygsId();
            final var logicaAddress = notificationMessage.getLogiskAdress();
            final var eventId = notificationMessage.getHandelse().value();

            MDC.put(MdcLogConstants.TRACE_ID_KEY, mdcHelper.traceId());
            MDC.put(MdcLogConstants.SPAN_ID_KEY, mdcHelper.spanId());
            MDC.put(MdcLogConstants.EVENT_CERTIFICATE_ID, certificateId);
            MDC.put(MdcLogConstants.EVENT_LOGICAL_ADDRESS, logicaAddress);
            MDC.put(MdcLogConstants.EVENT_STATUS_UPDATE_EVENT_ID, eventId);

            message.setHeader(NotificationRouteHeaders.VERSION, getSchemaVersion(notificationMessage));
            message.setHeader(NotificationRouteHeaders.LOGISK_ADRESS, logicaAddress);
            message.setHeader(NotificationRouteHeaders.INTYGS_ID, certificateId);
            message.setHeader(NotificationRouteHeaders.HANDELSE, eventId);

            final var statusUpdateForCare = getStatusUpdateForCare(notificationMessage, message);
            message.setBody(statusUpdateForCare);
        } catch (Exception e) {
            handleExceptions(message, notificationMessage, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    private CertificateStatusUpdateForCareType getStatusUpdateForCare(NotificationMessage notificationMessage, Message message)
        throws ModuleNotFoundException, IOException, ModuleException {
        if (notificationMessage.getStatusUpdateXml() != null) {
            final var element = XmlMarshallerHelper.unmarshal(
                new String(notificationMessage.getStatusUpdateXml(), StandardCharsets.UTF_8)
            );
            return (CertificateStatusUpdateForCareType) element.getValue();
        }

        final var certificateTypeVersion = resolveIntygTypeVersion(notificationMessage.getIntygsTyp(),
            message, notificationMessage.getUtkast());
        return certificateStatusUpdateForCareCreator.create(notificationMessage, certificateTypeVersion);
    }

    private void handleExceptions(Message message, NotificationMessage notificationMessage, Exception e)
        throws ModuleNotFoundException, IOException, ModuleException {
        LOG.error("Failure transforming notification [certificateId: " + notificationMessage.getIntygsId() + ", eventType: "
            + notificationMessage.getHandelse().value() + ", timestamp: " + notificationMessage.getHandelseTid() + "]", e);
        final var certificateTypeVersion = resolveIntygTypeVersion(notificationMessage.getIntygsTyp(),
            message, notificationMessage.getUtkast());
        final var correlationId = message.getHeader(CORRELATION_ID, String.class);
        final var userId = message.getHeader(USER_ID, String.class);
        final var resultMessage = notificationResultMessageCreator
            .createFailureMessage(notificationMessage, correlationId, userId, certificateTypeVersion, e);
        notificationResultMessageSender.sendResultMessage(resultMessage);
    }

    private String getSchemaVersion(NotificationMessage notificationMessage) {
        final var schemaVersion = notificationMessage.getVersion();
        if (schemaVersion == null) {
            LOG.warn("Recieved notificationMessage with unknown VERSION header, forcing V3");
            return SchemaVersion.VERSION_3.name();
        }

        if (!SchemaVersion.VERSION_3.equals(schemaVersion)) {
            throw new IllegalArgumentException("Unsupported combination of version '" + schemaVersion + "' and type '"
                + notificationMessage.getIntygsTyp() + "'");
        }

        return schemaVersion.name();
    }

    /**
     * Prefer using header for INTYG_TYPE_VERSION before trying to parse from body.
     */
    private String resolveIntygTypeVersion(String intygsTyp, Message message, String json) throws ModuleNotFoundException {
        if (message.getHeader(INTYG_TYPE_VERSION) != null) {
            return (String) message.getHeader(INTYG_TYPE_VERSION);
        }
        String certificateVersion = moduleRegistry.resolveVersionFromUtlatandeJson(intygsTyp, json);
        message.setHeader(INTYG_TYPE_VERSION, certificateVersion);
        return certificateVersion;
    }
}
