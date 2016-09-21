/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.notification;

import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.*;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.jms.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

/**
 * Service that notifies a unit care of incoming changes.
 *
 * @author Magnus Ekstrand
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired(required = false)
    @Qualifier("jmsNotificationTemplateForAggregation")
    private JmsTemplate jmsTemplateForAggregation;

    @PostConstruct
    public void checkJmsTemplate() {
        if (jmsTemplateForAggregation == null) {
            LOGGER.error("Notification is disabled!");
        }
    }

    @Autowired
    private SendNotificationStrategy sendNotificationStrategy;

    @Autowired
    private NotificationMessageFactory notificationMessageFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private UtkastRepository utkastRepo;

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForDraftCreated(se.inera.
     * intyg.webcert.web
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftCreated(Utkast utkast, String reference) {
        createAndSendNotification(utkast, SKAPAT, reference);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForDraftSigned(se.inera.
     * intyg.webcert.web.
     * persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftSigned(Utkast utkast) {
        createAndSendNotification(utkast, SIGNAT);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForDraftChanged(se.inera.
     * intyg.webcert.web
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftChanged(Utkast utkast) {
        createAndSendNotification(utkast, ANDRAT);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForDraftDeleted(se.inera.
     * intyg.webcert.web
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftDeleted(Utkast utkast) {
        createAndSendNotification(utkast, RADERA);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForIntygSent(se.inera.
     * intyg.webcert.web.
     * persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForIntygSent(String intygsId) {
        Optional<Utkast> utkast = getUtkast(intygsId);
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), SKICKA);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForIntygRevoked(se.inera.
     * intyg.webcert.web
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForIntygRevoked(String intygsId) {
        Optional<Utkast> utkast = getUtkast(intygsId);
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), MAKULE);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForQuestionReceived(se.
     * inera.intyg.webcert.web
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public void sendNotificationForQuestionReceived(FragaSvar fragaSvar) {
        Optional<Utkast> utkast = getUtkast(fragaSvar.getIntygsReferens().getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), NYFRFM);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForQuestionHandled(se.
     * inera.intyg.webcert.web
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public void sendNotificationForQuestionHandled(FragaSvar fragaSvar) {
        Optional<Utkast> utkast = getUtkast(fragaSvar.getIntygsReferens().getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), HANFRFM);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForQuestionSent(se.inera.
     * intyg.webcert.web
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public void sendNotificationForQuestionSent(FragaSvar fragaSvar) {
        Optional<Utkast> utkast = getUtkast(fragaSvar.getIntygsReferens().getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), NYFRFV);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForAnswerRecieved(se.inera
     * .intyg.webcert.web
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public void sendNotificationForAnswerRecieved(FragaSvar fragaSvar) {
        Optional<Utkast> utkast = getUtkast(fragaSvar.getIntygsReferens().getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), NYSVFM);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForAnswerHandled(se.inera.
     * intyg.webcert.web
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public void sendNotificationForAnswerHandled(FragaSvar fragaSvar) {
        Optional<Utkast> utkast = getUtkast(fragaSvar.getIntygsReferens().getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), HANFRFV);
        }
    }

    @Override
    public void sendNotificationForQuestionReceived(Arende arende) {
        Optional<Utkast> utkast = getUtkast(arende.getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), NYFRFM);
        }
    }

    @Override
    public void sendNotificationForQuestionHandled(Arende arende) {
        Optional<Utkast> utkast = getUtkast(arende.getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), HANFRFM);
        }
    }

    @Override
    public void sendNotificationForQuestionSent(Arende arende) {
        Optional<Utkast> utkast = getUtkast(arende.getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), NYFRFV);
        }
    }

    @Override
    public void sendNotificationForAnswerRecieved(Arende arende) {
        Optional<Utkast> utkast = getUtkast(arende.getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), NYSVFM);
        }
    }

    @Override
    public void sendNotificationForQuestionToRecipientHandled(Arende arende) {
        Optional<Utkast> utkast = getUtkast(arende.getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), HANFRFV);
        }
    }

    protected void createAndSendNotification(Utkast utkast, HandelsekodEnum handelse) {
        createAndSendNotification(utkast, handelse, null);
    }

    protected void createAndSendNotification(Utkast utkast, HandelsekodEnum handelse, String reference) {
        Optional<SchemaVersion> version = sendNotificationStrategy.decideNotificationForIntyg(utkast);

        if (!version.isPresent()) {
            LOGGER.debug("Will not send notification message for event {}", handelse);
            return;
        }

        NotificationMessage notificationMessage = notificationMessageFactory.createNotificationMessage(utkast, handelse, version.get(), reference);
        send(notificationMessage, utkast.getEnhetsId());
    }

    private void send(NotificationMessage notificationMessage, String enhetsId) {
        if (jmsTemplateForAggregation == null) {
            LOGGER.warn("Can not notify listeners! The JMS transport is not initialized.");
            return;
        }

        String notificationMessageAsJson = notificationMessageToJson(notificationMessage);

        try {
            jmsTemplateForAggregation.send(
                    new NotificationMessageCreator(
                            notificationMessageAsJson, notificationMessage.getIntygsId(), notificationMessage.getIntygsTyp(), notificationMessage.getHandelse())
            );
        } catch (JmsException e) {
            LOGGER.error("Could not send message", e);
            throw e;
        }

        LOGGER.debug("Notification sent: {}", notificationMessage);
        monitoringLog.logNotificationSent(notificationMessage.getHandelse().name(), enhetsId);
    }

    private Optional<Utkast> getUtkast(String intygsId) {
        return Optional.ofNullable(utkastRepo.findOne(intygsId));
    }

    private String notificationMessageToJson(NotificationMessage notificationMessage) {
        try {
            return objectMapper.writeValueAsString(notificationMessage);
        } catch (JsonProcessingException e) {
            LOGGER.error("Problem occured when trying to create and marshall NotificationMessage.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    private static final class NotificationMessageCreator implements MessageCreator {

        private final String value;
        private final String intygsId;
        private final String intygsTyp;
        private final HandelsekodEnum handelseTyp;

        private NotificationMessageCreator(String notificationMessage, String intygsId, String intygsTyp, HandelsekodEnum handelseTyp) {
            this.value = notificationMessage;
            this.intygsId = intygsId;
            this.intygsTyp = intygsTyp;
            this.handelseTyp = handelseTyp;
        }

        /**
         * Note that we add intygsTyp and handelseTyp as JMS headers to simplify subsequent routing.
         */
        @Override
        public Message createMessage(Session session) throws JMSException {
            TextMessage textMessage = session.createTextMessage(this.value);
            textMessage.setStringProperty(NotificationRouteHeaders.INTYGS_ID, this.intygsId);
            textMessage.setStringProperty(NotificationRouteHeaders.INTYGS_TYP, this.intygsTyp);
            textMessage.setStringProperty(NotificationRouteHeaders.HANDELSE, this.handelseTyp.value());
            return textMessage;
        }
    }

}
