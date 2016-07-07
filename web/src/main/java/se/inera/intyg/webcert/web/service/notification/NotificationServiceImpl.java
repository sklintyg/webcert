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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.modules.support.api.notification.HandelseType;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Optional;

import static se.inera.intyg.common.support.modules.support.api.notification.HandelseType.*;
import static se.inera.intyg.common.support.modules.support.api.notification.HandelseType.INTYGSUTKAST_ANDRAT;

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
    public void sendNotificationForDraftCreated(Utkast utkast) {
        createAndSendNotification(utkast, INTYGSUTKAST_SKAPAT);
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
        createAndSendNotification(utkast, INTYGSUTKAST_SIGNERAT);
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
        createAndSendNotification(utkast, INTYGSUTKAST_ANDRAT);
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
        createAndSendNotification(utkast, INTYGSUTKAST_RADERAT);
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
            createAndSendNotification(utkast.get(), INTYG_SKICKAT_FK);
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
            createAndSendNotification(utkast.get(), INTYG_MAKULERAT);
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
            createAndSendNotification(utkast.get(), FRAGA_FRAN_FK);
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
            createAndSendNotification(utkast.get(), FRAGA_FRAN_FK_HANTERAD);
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
            createAndSendNotification(utkast.get(), FRAGA_TILL_FK);
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
            createAndSendNotification(utkast.get(), SVAR_FRAN_FK);
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
            createAndSendNotification(utkast.get(), SVAR_FRAN_FK_HANTERAD);
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
    public void sendNotificationForQuestionReceived(Arende arende) {
        Optional<Utkast> utkast = getUtkast(arende.getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), FRAGA_FRAN_FK);
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
    public void sendNotificationForQuestionHandled(Arende arende) {
        Optional<Utkast> utkast = getUtkast(arende.getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), FRAGA_FRAN_FK_HANTERAD);
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
    public void sendNotificationForQuestionSent(Arende arende) {
        Optional<Utkast> utkast = getUtkast(arende.getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), FRAGA_TILL_FK);
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
    public void sendNotificationForAnswerRecieved(Arende arende) {
        Optional<Utkast> utkast = getUtkast(arende.getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), SVAR_FRAN_FK);
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
    public void sendNotificationForAnswerHandled(Arende arende) {
        Optional<Utkast> utkast = getUtkast(arende.getIntygsId());
        if (utkast.isPresent()) {
            createAndSendNotification(utkast.get(), SVAR_FRAN_FK_HANTERAD);
        }
    }

    protected void createAndSendNotification(Utkast utkast, HandelseType handelse) {
        Optional<SchemaVersion> version = sendNotificationStrategy.decideNotificationForIntyg(utkast);

        if (!version.isPresent()) {
            LOGGER.debug("Will not send notification message for event {}", handelse);
            return;
        }

        NotificationMessage notificationMessage = notificationMessageFactory.createNotificationMessage(utkast, handelse, version.get());
        send(notificationMessage, utkast.getEnhetsId());
    }

    private void send(NotificationMessage notificationMessage, String enhetsId) {
        if (jmsTemplateForAggregation == null) {
            LOGGER.warn("Can not notify listeners! The JMS transport is not initialized.");
            return;
        }

        String notificationMessageAsJson = notificationMessageToJson(notificationMessage);

        jmsTemplateForAggregation.send(
                new NotificationMessageCreator(
                        notificationMessageAsJson, notificationMessage.getIntygsId(), notificationMessage.getIntygsTyp(), notificationMessage.getHandelse())
        );

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
        private final HandelseType handelseTyp;

        private NotificationMessageCreator(String notificationMessage, String intygsId, String intygsTyp, HandelseType handelseTyp) {
            this.value = notificationMessage;
            this.intygsId = intygsId;
            this.intygsTyp = intygsTyp;
            this.handelseTyp = handelseTyp;
        }

        /**
         * Note that we add intygsTyp and handelseTyp as JMS headers to simplify subsequent routing.
         *
         * We also add a JMSX_GROUP_ID as a number of types has to be processed by the same consumer. If
         * not of those types, we additionally send a {@link Constants#JMSX_GROUP_SEQ} instead that tells
         * ActiveMQ to remove any existing grouping for the given JMSX_GROUP_ID.
         *
         * Essentially, we do this to mitigate long-term performance and stability problems in ActiveMQ where excessive
         * amounts of stale Message Groups can cause stability problems. By sending the -1 as JMSXGroupSeq we tell
         * ActiveMQ its OK to discard that message group.
         *
         * Furthermore - we actually remove those two headers in the aggregationRoute as they only are applicable there.
         */
        @Override
        public Message createMessage(Session session) throws JMSException {
            TextMessage textMessage = session.createTextMessage(this.value);
            textMessage.setStringProperty(NotificationRouteHeaders.INTYGS_TYP, this.intygsTyp);
            textMessage.setStringProperty(NotificationRouteHeaders.HANDELSE, this.handelseTyp.value());
            textMessage.setStringProperty(Constants.JMSX_GROUP_ID, this.intygsId);
            switch (this.handelseTyp) {
                case INTYGSUTKAST_SKAPAT:
                case INTYGSUTKAST_ANDRAT:
                case INTYGSUTKAST_SIGNERAT:
                    break;
                default:
                    textMessage.setIntProperty(Constants.JMSX_GROUP_SEQ, -1);
            }
            return textMessage;
        }
    }

}
