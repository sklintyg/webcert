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

import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYG_TYPE_VERSION;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import java.io.IOException;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.infra.integration.hsa.services.HsaOrganizationsService;
import se.inera.intyg.infra.security.authorities.FeaturesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;

public class NotificationTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationTransformer.class);

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private NotificationPatientEnricher notificationPatientEnricher;

    @Autowired
    HsaOrganizationsService organizationsService;

    @Autowired
    FeaturesHelper featuresHelper;

    @Autowired
    NotificationResultMessageCreator notificationResultMessageCreator;

    @Autowired
    NotificationResultMessageSender notificationResultMessageSender;

    public void process(Message message) throws ModuleException, IOException, ModuleNotFoundException, TemporaryException {
        LOG.debug("Receiving message: {}", message.getMessageId());

        final var notificationMessage = message.getBody(NotificationMessage.class);

        final var schemaVersion = getSchemaVersion(notificationMessage);
        final var correlationId = message.getHeader(CORRELATION_ID, String.class);
        final var userId = message.getHeader(USER_ID, String.class);

        message.setHeader(NotificationRouteHeaders.VERSION, schemaVersion);
        message.setHeader(NotificationRouteHeaders.LOGISK_ADRESS, notificationMessage.getLogiskAdress());
        message.setHeader(NotificationRouteHeaders.INTYGS_ID, notificationMessage.getIntygsId());
        message.setHeader(NotificationRouteHeaders.HANDELSE, notificationMessage.getHandelse().value());

        Utlatande utlatande = null;
        try {
            final var moduleApi = getModuleApi(notificationMessage, message);
            utlatande = moduleApi.getUtlatandeFromJson(notificationMessage.getUtkast());

            final var statusUpdateForCare = getStatusUpdateForCare(notificationMessage, moduleApi, utlatande);
            message.setBody(statusUpdateForCare);
        } catch (TemporaryException e) {
            LOG.error("Failure transforming notification [certificateId: " + notificationMessage.getIntygsId() + ", eventType: "
                + notificationMessage.getHandelse().value() + ", timestamp: " + notificationMessage.getHandelseTid() + "]", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Failure transforming notification [certificateId: " + notificationMessage.getIntygsId() + ", eventType: "
                + notificationMessage.getHandelse().value() + ", timestamp: " + notificationMessage.getHandelseTid() + "]", e);

            if (usingWebcertMessaging()) {
                final var resultMessage = notificationResultMessageCreator
                    .createFailureMessage(correlationId, userId, notificationMessage, utlatande, e);
                final var success = notificationResultMessageSender.sendResultMessage(resultMessage);
                if (!success) {
                    throw e;
                }
            } else {
                throw e;
            }
        }
    }

    private CertificateStatusUpdateForCareType getStatusUpdateForCare(NotificationMessage notificationMessage, ModuleApi moduleApi,
        Utlatande utlatande) throws ModuleException, TemporaryException {
        final var intyg = moduleApi.getIntygFromUtlatande(utlatande);
        notificationPatientEnricher.enrichWithPatient(intyg);
        return NotificationTypeConverter.convert(notificationMessage, intyg);
    }

    private ModuleApi getModuleApi(NotificationMessage notificationMessage, Message message) throws ModuleNotFoundException {
        final var intygTypeVersion = resolveIntygTypeVersion(notificationMessage.getIntygsTyp(), message,
            notificationMessage.getUtkast());
        return moduleRegistry.getModuleApi(notificationMessage.getIntygsTyp(), intygTypeVersion);
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

    private boolean usingWebcertMessaging() {
        return featuresHelper.isFeatureActive(AuthoritiesConstants.FEATURE_USE_WEBCERT_MESSAGING);
    }
}
