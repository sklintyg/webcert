/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.common.service.notification.AmneskodCreator;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.persistence.arende.model.Arende;
import se.inera.intyg.webcert.persistence.arende.model.ArendeAmne;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.handelse.model.Handelse;
import se.inera.intyg.webcert.persistence.handelse.repository.HandelseRepository;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.service.mail.MailNotification;
import se.inera.intyg.webcert.web.service.mail.MailNotificationService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.referens.ReferensService;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Amneskod;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.ANDRAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.HANFRFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.HANFRFV;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.KFSIGN;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.MAKULE;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYFRFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYFRFV;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYSVFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.RADERA;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SIGNAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKAPAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKICKA;

/**
 * Service that notifies a unit care of incoming changes.
 *
 * @author Magnus Ekstrand
 */
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Autowired
    private MailNotificationService mailNotificationService;

    @Autowired(required = false)
    @Qualifier("jmsNotificationTemplateForAggregation")
    private JmsTemplate jmsTemplateForAggregation;

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

    @Autowired
    private HandelseRepository handelseRepo;

    @Autowired
    private ReferensService referensService;

    @PostConstruct
    public void checkJmsTemplate() {
        if (jmsTemplateForAggregation == null) {
            LOGGER.error("Notification is disabled!");
        }
    }

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
        createAndSendNotification(utkast, SKAPAT);
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
     * se.inera.intyg.webcert.web.service.notification.NewNotificationService#sendNotificationForDraftRevoked(se.inera.
     * intyg.webcert.web
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftRevoked(Utkast utkast) {
        createAndSendNotification(utkast, MAKULE);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * se.inera.intyg.webcert.web.service.notification.NotificationService#sendNotificationForDraftReadyToSign(se.inera.
     * intyg.webcert.web.persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftReadyToSign(Utkast utkast) {
        createAndSendNotification(utkast, KFSIGN);
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
        Utkast utkast = getUtkast(intygsId);
        if (utkast != null) {
            createAndSendNotification(utkast, SKICKA);
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
        Utkast utkast = getUtkast(intygsId);
        if (utkast != null) {
            createAndSendNotification(utkast, MAKULE);
        }
    }

    @Override
    public void sendNotificationForQuestionReceived(FragaSvar fragaSvar) {
        if (integreradeEnheterRegistry.isEnhetIntegrerad(fragaSvar.getVardperson().getEnhetsId(), Fk7263EntryPoint.MODULE_ID)) {
            sendNotificationForQAs(fragaSvar.getIntygsReferens().getIntygsId(), NotificationEvent.NEW_QUESTION_FROM_RECIPIENT,
                    fragaSvar.getSistaDatumForSvar(), ArendeAmne.fromAmne(fragaSvar.getAmne()).orElse(null));
        } else {
            sendNotificationForIncomingQuestionByMail(new MailNotification(fragaSvar.getInternReferens().toString(),
                    fragaSvar.getIntygsReferens().getIntygsId(), Fk7263EntryPoint.MODULE_ID,
                    fragaSvar.getVardperson().getEnhetsId(), fragaSvar.getVardperson().getEnhetsnamn(),
                    fragaSvar.getVardperson().getHsaId()));
        }
    }

    @Override
    public void sendNotificationForAnswerRecieved(FragaSvar fragaSvar) {
        if (integreradeEnheterRegistry.isEnhetIntegrerad(fragaSvar.getVardperson().getEnhetsId(), Fk7263EntryPoint.MODULE_ID)) {
            sendNotificationForQAs(fragaSvar.getIntygsReferens().getIntygsId(), NotificationEvent.NEW_ANSWER_FROM_RECIPIENT);
        } else {
            sendNotificationForIncomingAnswerByMail(new MailNotification(fragaSvar.getInternReferens().toString(),
                    fragaSvar.getIntygsReferens().getIntygsId(), Fk7263EntryPoint.MODULE_ID,
                    fragaSvar.getVardperson().getEnhetsId(), fragaSvar.getVardperson().getEnhetsnamn(),
                    fragaSvar.getVardperson().getHsaId()));
        }
    }

    @Override
    public void sendNotificationForQuestionReceived(Arende arende) {
        if (integreradeEnheterRegistry.isEnhetIntegrerad(arende.getEnhetId(), arende.getIntygTyp())) {
            sendNotificationForQAs(arende.getIntygsId(), NotificationEvent.NEW_QUESTION_FROM_RECIPIENT, arende.getSistaDatumForSvar(),
                    arende.getAmne());
        } else {
            sendNotificationForIncomingQuestionByMail(new MailNotification(arende.getMeddelandeId(), arende.getIntygsId(),
                    arende.getIntygTyp(), arende.getEnhetId(), arende.getEnhetName(), arende.getSigneratAv()));
        }
    }

    @Override
    public void sendNotificationForAnswerRecieved(Arende arende) {
        if (integreradeEnheterRegistry.isEnhetIntegrerad(arende.getEnhetId(), arende.getIntygTyp())) {
            sendNotificationForQAs(arende.getIntygsId(), NotificationEvent.NEW_ANSWER_FROM_RECIPIENT);
        } else {
            sendNotificationForIncomingAnswerByMail(new MailNotification(arende.getMeddelandeId(), arende.getIntygsId(),
                    arende.getIntygTyp(), arende.getEnhetId(), arende.getEnhetName(), arende.getSigneratAv()));
        }
    }

    @Override
    public List<Handelse> getNotifications(String intygsId) {
        return handelseRepo.findByIntygsId(intygsId);
    }

    @Override
    public void sendNotificationForQAs(String intygsId, NotificationEvent event) {
        sendNotificationForQAs(intygsId, event, null, null);
    }

    @Override
    public void forwardInternalNotification(final String intygsId, final String intygstyp, final Utlatande utlatande,
            final HandelsekodEnum handelse) {
        final String careUnitId = utlatande.getGrundData().getSkapadAv().getVardenhet().getEnhetsid();
        final String careGiverId = utlatande.getGrundData().getSkapadAv().getVardenhet().getVardgivare().getVardgivarid();
        final String reference = referensService.getReferensForIntygsId(intygsId);

        try {
            String json = objectMapper.writeValueAsString(utlatande);
            NotificationMessage notificationMessage = notificationMessageFactory.createNotificationMessage(intygsId, intygstyp, careUnitId,
                    json, handelse,
                    SchemaVersion.VERSION_3, reference, null, null);

            save(notificationMessage, careUnitId, careGiverId,
                    utlatande.getGrundData().getPatient().getPersonId().getPersonnummerWithDash(), null, null);

            send(notificationMessage, careUnitId, utlatande.getTextVersion());
        } catch (JsonProcessingException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e.getMessage());
        }

    }

    private void sendNotificationForQAs(String intygsId, NotificationEvent event, LocalDate date, ArendeAmne amne) {
        Utkast utkast = getUtkast(intygsId);
        if (utkast != null) {
            createAndSendNotificationForQAs(utkast, event, amne, date);
        }
    }

    void createAndSendNotification(Utkast utkast, HandelsekodEnum handelse) {
        createAndSendNotification(utkast, handelse, null, null);
    }

    private void createAndSendNotification(Utkast utkast, HandelsekodEnum handelse,
            ArendeAmne amne, LocalDate sistaDatumForSvar) {

        Optional<SchemaVersion> version = sendNotificationStrategy.decideNotificationForIntyg(utkast);
        if (!version.isPresent()) {
            LOGGER.debug("Schema version is not present. Notification message not sent for event {}", handelse);
            return;
        }

        createAndSendNotification(utkast, handelse, amne, sistaDatumForSvar, version.get());
    }

    private void createAndSendNotification(Utkast utkast, HandelsekodEnum handelse,
            ArendeAmne amne, LocalDate sistaDatumForSvar, SchemaVersion version) {
        Amneskod amneskod = null;
        if (amne != null) {
            amneskod = AmneskodCreator.create(amne.name(), amne.getDescription());
        }

        String reference = referensService.getReferensForIntygsId(utkast.getIntygsId());

        NotificationMessage notificationMessage = notificationMessageFactory.createNotificationMessage(utkast, handelse,
                version, reference, amneskod, sistaDatumForSvar);

        save(notificationMessage, utkast.getEnhetsId(), utkast.getVardgivarId(),
                utkast.getPatientPersonnummer().getPersonnummer(), amne, sistaDatumForSvar);

        send(notificationMessage, utkast.getEnhetsId(), utkast.getIntygTypeVersion());
    }

    private void createAndSendNotificationForQAs(Utkast utkast, NotificationEvent event, ArendeAmne amne, LocalDate sistaDatumForSvar) {

        Optional<SchemaVersion> version = sendNotificationStrategy.decideNotificationForIntyg(utkast);
        if (!version.isPresent()) {
            LOGGER.debug("Schema version is not present. Notification message not sent");
            return;
        }

        HandelsekodEnum handelse = null;
        if (SchemaVersion.VERSION_3 == version.get()) {
            handelse = getHandelseV3(event);
        } else {
            handelse = getHandelseV1(event);
        }

        if (handelse == null) {
            LOGGER.debug("Notification message not sent for event {} in version {}", event.name(), version.get().name());
            return;
        }

        createAndSendNotification(utkast, handelse, amne, sistaDatumForSvar, version.get());
    }

    private HandelsekodEnum getHandelseV1(NotificationEvent event) {
        switch (event) {
        case QUESTION_FROM_CARE_WITH_ANSWER_HANDLED:
            return HANFRFV;
        case QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED:
        case NEW_ANSWER_FROM_RECIPIENT:
            return NYSVFM;
        case NEW_ANSWER_FROM_CARE:
        case QUESTION_FROM_RECIPIENT_HANDLED:
            return HANFRFM;
        case NEW_QUESTION_FROM_RECIPIENT:
        case QUESTION_FROM_RECIPIENT_UNHANDLED:
            return NYFRFM;
        case NEW_QUESTION_FROM_CARE:
            return NYFRFV;
        case QUESTION_FROM_CARE_HANDLED:
        case QUESTION_FROM_CARE_UNHANDLED:
            return null;
        }
        return null;
    }

    private HandelsekodEnum getHandelseV3(NotificationEvent event) {
        switch (event) {
        case QUESTION_FROM_CARE_WITH_ANSWER_HANDLED:
        case QUESTION_FROM_CARE_WITH_ANSWER_UNHANDLED:
        case QUESTION_FROM_CARE_HANDLED:
        case QUESTION_FROM_CARE_UNHANDLED:
            return HANFRFV;
        case NEW_ANSWER_FROM_CARE:
        case QUESTION_FROM_RECIPIENT_HANDLED:
        case QUESTION_FROM_RECIPIENT_UNHANDLED:
            return HANFRFM;
        case NEW_QUESTION_FROM_CARE:
            return NYFRFV;
        case NEW_QUESTION_FROM_RECIPIENT:
            return NYFRFM;
        case NEW_ANSWER_FROM_RECIPIENT:
            return NYSVFM;
        }
        return null;
    }

    private void save(NotificationMessage notificationMessage, String enhetsId, String vardgivarId, String personnummer,
            ArendeAmne amne, LocalDate sistaDatumForSvar) {

        Handelse handelse = new Handelse();
        handelse.setCode(notificationMessage.getHandelse());
        handelse.setEnhetsId(enhetsId);
        handelse.setIntygsId(notificationMessage.getIntygsId());
        handelse.setPersonnummer(personnummer);
        handelse.setTimestamp(notificationMessage.getHandelseTid());
        handelse.setVardgivarId(vardgivarId);
        handelse.setAmne(amne);
        handelse.setSistaDatumForSvar(sistaDatumForSvar);

        handelseRepo.save(handelse);
    }

    private void send(NotificationMessage notificationMessage, String enhetsId, String intygTypeVersion) {
        if (jmsTemplateForAggregation == null) {
            LOGGER.warn("Can not notify listeners! The JMS transport is not initialized.");
            return;
        }

        String notificationMessageAsJson = notificationMessageToJson(notificationMessage);

        try {
            jmsTemplateForAggregation.send(
                    new NotificationMessageCreator(
                            notificationMessageAsJson, notificationMessage.getIntygsId(), notificationMessage.getIntygsTyp(),
                            intygTypeVersion, notificationMessage.getHandelse()));
        } catch (JmsException e) {
            LOGGER.error("Could not send message", e);
            throw e;
        }

        LOGGER.debug("Notification sent: {}", notificationMessage);
        monitoringLog.logNotificationSent(notificationMessage.getHandelse().name(), enhetsId, notificationMessage.getIntygsId());
    }

    private Utkast getUtkast(String intygsId) {
        return utkastRepo.findOne(intygsId);
    }

    private String notificationMessageToJson(NotificationMessage notificationMessage) {
        try {
            return objectMapper.writeValueAsString(notificationMessage);
        } catch (JsonProcessingException e) {
            LOGGER.error("Problem occured when trying to create and marshall NotificationMessage.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    private void sendNotificationForIncomingQuestionByMail(MailNotification mailNotification) {
        // send mail to enhet to inform about new question
        try {
            mailNotificationService.sendMailForIncomingQuestion(mailNotification);
        } catch (MailSendException e) {
            LOGGER.error("Notification mail for question '" + mailNotification.getQaId()
                    + "' concerning certificate '" + mailNotification.getCertificateId()
                    + "' couldn't be sent to " + mailNotification.getCareUnitId()
                    + " (" + mailNotification.getCareUnitName() + "): " + e.getMessage());
        }
    }

    private void sendNotificationForIncomingAnswerByMail(MailNotification mailNotification) {
        // send mail to enhet to inform about new question
        try {
            mailNotificationService.sendMailForIncomingAnswer(mailNotification);
        } catch (MailSendException e) {
            LOGGER.error("Notification mail for answer '" + mailNotification.getQaId()
                    + "' concerning certificate '" + mailNotification.getCertificateId()
                    + "' couldn't be sent to " + mailNotification.getCareUnitId()
                    + " (" + mailNotification.getCareUnitName() + "): " + e.getMessage());
        }
    }

    private static final class NotificationMessageCreator implements MessageCreator {

        private final String value;
        private final String intygsId;
        private final String intygsTyp;
        private final String intygTypeVersion;
        private final HandelsekodEnum handelseTyp;

        private NotificationMessageCreator(String notificationMessage, String intygsId, String intygsTyp, String intygTypeVersion,
                                           HandelsekodEnum handelseTyp) {
            this.value = notificationMessage;
            this.intygsId = intygsId;
            this.intygsTyp = intygsTyp;
            this.intygTypeVersion = intygTypeVersion;
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
            textMessage.setStringProperty(NotificationRouteHeaders.INTYG_TYPE_VERSION, this.intygTypeVersion);
            textMessage.setStringProperty(NotificationRouteHeaders.HANDELSE, this.handelseTyp.value());
            return textMessage;
        }
    }

}
