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

import static se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationResultTypeEnum.FAILURE;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.CORRELATION_ID;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.INTYG_TYPE_VERSION;
import static se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders.USER_ID;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.jms.TextMessage;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
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
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultMessage;
import se.inera.intyg.webcert.notification_sender.notifications.dto.NotificationResultType;
import se.inera.intyg.webcert.notification_sender.notifications.enumerations.NotificationErrorTypeEnum;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.riv.clinicalprocess.healthcond.certificate.v3.Intyg;

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
    @Qualifier("jmsTemplateNotificationPostProcessing")
    private JmsTemplate jmsTemplateNotificationPostProcessing;

    @Autowired
    private ObjectMapper objectMapper;

    public void process(Message message) throws ModuleException, IOException, ModuleNotFoundException, TemporaryException {
        LOG.debug("Receiving message: {}", message.getMessageId());

        final var notificationMessage = message.getBody(NotificationMessage.class);

        final var schemaVersion = getSchemaVersion(notificationMessage);

        message.setHeader(NotificationRouteHeaders.VERSION, schemaVersion);
        message.setHeader(NotificationRouteHeaders.LOGISK_ADRESS, notificationMessage.getLogiskAdress());
        message.setHeader(NotificationRouteHeaders.INTYGS_ID, notificationMessage.getIntygsId());
        message.setHeader(NotificationRouteHeaders.HANDELSE, notificationMessage.getHandelse().value());

        Utlatande utlatande = null;
        String intygTypeVersion;
        try {
            intygTypeVersion = resolveIntygTypeVersion(notificationMessage.getIntygsTyp(), message,
                notificationMessage.getUtkast());
            ModuleApi moduleApi = moduleRegistry.getModuleApi(notificationMessage.getIntygsTyp(), intygTypeVersion);
            utlatande = moduleApi.getUtlatandeFromJson(notificationMessage.getUtkast());
            Intyg intyg = moduleApi.getIntygFromUtlatande(utlatande);
            notificationPatientEnricher.enrichWithPatient(intyg);
            message.setBody(NotificationTypeConverter.convert(notificationMessage, intyg));

        } catch (Exception e) {
            LOG.error("Failure transforming notification [certificateId: " + notificationMessage.getIntygsId() + ", eventType: "
                + notificationMessage.getHandelse().value() + ", timestamp: " + notificationMessage.getHandelseTid() + "]", e);

            if (usingWebcertMessaging()) {
                final var resultMessage = createResultMessage(message, notificationMessage, utlatande, e);
                sendResultMessage(resultMessage, notificationMessage);
            }
            throw e;
        }
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

    private boolean usingWebcertMessaging() {
        return featuresHelper.isFeatureActive(AuthoritiesConstants.FEATURE_USE_WEBCERT_MESSAGING);
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

    private NotificationResultMessage createResultMessage(Message message, NotificationMessage notificationMessage, Utlatande utlatande,
        Exception exception) {

        final var correlationId = message.getHeader(CORRELATION_ID, String.class);
        final var userId = message.getHeader(USER_ID, String.class);

        final var event = createEvent(notificationMessage, utlatande, userId);

        final var notificationResultType = createResultType(exception);

        final var resultMessage = new NotificationResultMessage();
        resultMessage.setCorrelationId(correlationId);
        resultMessage.setEvent(event);
        resultMessage.setResultType(notificationResultType);

        return resultMessage;
    }

    private NotificationResultType createResultType(Exception exception) {
        final var notificationResultType = new NotificationResultType();
        // TODO: Could ERROR be used or is it necessary with a specific type?
        notificationResultType.setNotificationResult(FAILURE);
        notificationResultType.setNotificationResultText(exception.getMessage());
        notificationResultType.setNotificationErrorType(NotificationErrorTypeEnum.WEBCERT_EXCEPTION);
        notificationResultType.setException(exception.getClass().getName());
        return notificationResultType;
    }

    public static Handelse createEvent(NotificationMessage notificationMessage, Utlatande utlatande, String user) {
        final var event = new Handelse();
        event.setIntygsId(utlatande.getId());
        event.setCertificateType(utlatande.getTyp());
        event.setCertificateVersion(utlatande.getTextVersion());
        event.setVardgivarId(utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid());
        event.setCertificateIssuer(utlatande.getGrundData().getSkapadAv().getPersonId());
        event.setPersonnummer(utlatande.getGrundData().getPatient().getPersonId().getPersonnummer());

        event.setCode(notificationMessage.getHandelse());
        event.setTimestamp(notificationMessage.getHandelseTid());
        event.setAmne(notificationMessage.getAmne() != null ? ArendeAmne.valueOf(notificationMessage.getAmne().getCode()) : null);
        event.setEnhetsId(notificationMessage.getLogiskAdress());
        event.setSistaDatumForSvar(notificationMessage.getSistaSvarsDatum());

        event.setHanteratAv(user);

        return event;
    }

    private void sendResultMessage(NotificationResultMessage resultMessage, NotificationMessage notificationMessage) {

        try {
            String notificationMessageJson = objectMapper.writeValueAsString(resultMessage);

            jmsTemplateNotificationPostProcessing.send(session -> {
                TextMessage textMessage = session.createTextMessage(notificationMessageJson);
                textMessage.setStringProperty(NotificationRouteHeaders.INTYGS_ID, notificationMessage.getIntygsId());
                textMessage.setStringProperty(NotificationRouteHeaders.CORRELATION_ID, resultMessage.getCorrelationId());
                textMessage.setStringProperty(NotificationRouteHeaders.LOGISK_ADRESS, notificationMessage.getLogiskAdress());
                textMessage.setStringProperty(NotificationRouteHeaders.HANDELSE, resultMessage.getEvent().getCode().value());
                return textMessage;
            });
        } catch (Exception e) {
            // TODO: Log the exception.
            LOG.error("Exception occured sending NotificationResultMessage after exception in NotificationTransformer {}", resultMessage);
        }
    }
}
