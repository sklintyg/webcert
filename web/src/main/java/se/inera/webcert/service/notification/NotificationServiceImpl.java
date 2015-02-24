package se.inera.webcert.service.notification;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import se.inera.certificate.modules.support.api.notification.HandelseType;
import se.inera.certificate.modules.support.api.notification.NotificationMessage;
import se.inera.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.webcert.persistence.utkast.model.Utkast;
import se.inera.webcert.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.webcert.service.exception.WebCertServiceException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service that notifies a unit care of incoming changes.
 *
 * @author Magnus Ekstrand
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired(required = false)
    @Qualifier("jmsNotificationTemplate")
    private JmsTemplate jmsTemplate;

    @PostConstruct
    public void checkJmsTemplate() {
        if (jmsTemplate == null) {
            LOGGER.error("Notification is disabled!");
        }
    }

    @Autowired
    private SendNotificationStrategy sendNotificationStrategy;

    @Autowired
    private NotificationMessageFactory notificationMessageFactory;

    @Autowired
    private ObjectMapper objectMapper;

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.NewNotificationService#sendNotificationForDraftCreated(se.inera.webcert
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftCreated(Utkast utkast) {
        createAndSendNotification(utkast, HandelseType.INTYGSUTKAST_SKAPAT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.NewNotificationService#sendNotificationForDraftSigned(se.inera.webcert.
     * persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftSigned(Utkast utkast) {
        createAndSendNotification(utkast, HandelseType.INTYGSUTKAST_SIGNERAT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.NewNotificationService#sendNotificationForDraftChanged(se.inera.webcert
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftChanged(Utkast utkast) {
        createAndSendNotification(utkast, HandelseType.INTYGSUTKAST_ANDRAT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.NewNotificationService#sendNotificationForDraftDeleted(se.inera.webcert
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForDraftDeleted(Utkast utkast) {
        createAndSendNotification(utkast, HandelseType.INTYGSUTKAST_RADERAT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.service.notification.NewNotificationService#sendNotificationForIntygSent(se.inera.webcert.
     * persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForIntygSent(String intygsId) {
        createAndSendNotification(intygsId, HandelseType.INTYG_SKICKAT_FK);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.NewNotificationService#sendNotificationForIntygRevoked(se.inera.webcert
     * .persistence.utkast.model.Utkast)
     */
    @Override
    public void sendNotificationForIntygRevoked(String intygsId) {
        createAndSendNotification(intygsId, HandelseType.INTYG_MAKULERAT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.NewNotificationService#sendNotificationForQuestionReceived(se.inera.webcert
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public void sendNotificationForQuestionReceived(FragaSvar fragaSvar) {
        createAndSendNotification(fragaSvar, HandelseType.FRAGA_FRAN_FK);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.NewNotificationService#sendNotificationForQuestionHandled(se.inera.webcert
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public void sendNotificationForQuestionHandled(FragaSvar fragaSvar) {
        createAndSendNotification(fragaSvar, HandelseType.FRAGA_FRAN_FK_HANTERAD);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.NewNotificationService#sendNotificationForQuestionSent(se.inera.webcert
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public void sendNotificationForQuestionSent(FragaSvar fragaSvar) {
        createAndSendNotification(fragaSvar, HandelseType.FRAGA_TILL_FK);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.NewNotificationService#sendNotificationForAnswerRecieved(se.inera.webcert
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public void sendNotificationForAnswerRecieved(FragaSvar fragaSvar) {
        createAndSendNotification(fragaSvar, HandelseType.SVAR_FRAN_FK);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * se.inera.webcert.service.notification.NewNotificationService#sendNotificationForAnswerHandled(se.inera.webcert
     * .persistence.fragasvar.model.FragaSvar)
     */
    @Override
    public void sendNotificationForAnswerHandled(FragaSvar fragaSvar) {
        createAndSendNotification(fragaSvar, HandelseType.SVAR_FRAN_FK_HANTERAD);
    }

    public void createAndSendNotification(String intygsId, HandelseType handelse) {

        Utkast utkast = sendNotificationStrategy.decideNotificationForIntyg(intygsId);

        if (utkast == null) {
            LOGGER.debug("Will not send notification message for certificate '{}' and event '{}'", intygsId, handelse);
        }
        
        NotificationMessage notificationMessage = notificationMessageFactory.createNotificationMessage(utkast, handelse);
        send(notificationMessage);
    }

    public void createAndSendNotification(Utkast utkast, HandelseType handelse) {

        utkast = sendNotificationStrategy.decideNotificationForIntyg(utkast);

        if (utkast == null) {
            LOGGER.debug("Will not send notification message for event {}", handelse);
        }
        
        NotificationMessage notificationMessage = notificationMessageFactory.createNotificationMessage(utkast, handelse);
        send(notificationMessage);
    }

    public void createAndSendNotification(FragaSvar fragaSvar, HandelseType handelse) {

        Utkast utkast = sendNotificationStrategy.decideNotificationForFragaSvar(fragaSvar);

        if (utkast == null) {
            LOGGER.debug("Will not send notification message for event {}", handelse);
        }
        
        NotificationMessage notificationMessage = notificationMessageFactory.createNotificationMessage(utkast, handelse);
        send(notificationMessage);
    }

    /* -- Package visibility -- */
    void send(NotificationMessage notificationMessage) {

        if (jmsTemplate == null) {
            LOGGER.warn("Can not notify listeners! The JMS transport is not initialized.");
            return;
        }

        LOGGER.debug("Sending notification {}", notificationMessage);

        String notificationMessageAsJson = notificationMessageToJson(notificationMessage);

        jmsTemplate.send(new NotificationMessageCreator(notificationMessageAsJson));
    }

    String notificationMessageToJson(NotificationMessage notificationMessage) {
        try {
            return objectMapper.writeValueAsString(notificationMessage);
        } catch (JsonProcessingException e) {
            LOGGER.error("Problem occured when trying to create and marshall NotificationMessage.", e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM, e);
        }
    }

    static final class NotificationMessageCreator implements MessageCreator {

        private String value;

        public NotificationMessageCreator(String notificationMessage) {
            this.value = notificationMessage;
        }

        public Message createMessage(Session session) throws JMSException {
            return session.createTextMessage(this.value);
        }
    }

}
