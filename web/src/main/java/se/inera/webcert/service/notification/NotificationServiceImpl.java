package se.inera.webcert.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Service that notifies a unit care of incoming changes.
 *
 * @author Magnus Ekstrand
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired(required = false)
    private JmsTemplate jmsTemplate;

    @PostConstruct
    public void checkJmsTemplate() {
        if (jmsTemplate == null) {
            LOGGER.error("Notification is disabled!");
        }
    }

    @Override
    public void notify(String message) {
        send(message);
    }

    void send(String message) {

        if (jmsTemplate == null) {
            LOGGER.warn("Can not notify listeners! The JMS transport is not initialized.");
            return;
        }

        jmsTemplate.send(new MC(message));
    }

    static final class MC implements MessageCreator {
        private String message;

        public MC(String message) {
            this.message = message;
        }

        public Message createMessage(Session session) throws JMSException {
            return session.createObjectMessage(message);
        }
    }

}
