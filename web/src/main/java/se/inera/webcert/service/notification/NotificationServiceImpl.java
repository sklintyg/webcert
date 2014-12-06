package se.inera.webcert.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.notifications.message.v1.ObjectFactory;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

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

    @Override
    public void notify(NotificationRequestType notificationRequestType) {
        send(notificationRequestType);
    }


    /* -- Package visibility -- */

    void send(NotificationRequestType notificationRequestType) {

        if (jmsTemplate == null) {
            LOGGER.warn("Can not notify listeners! The JMS transport is not initialized.");
            return;
        }

        jmsTemplate.send(new NotificationMessageCreator(notificationRequestType));
    }

    static final class NotificationMessageCreator implements MessageCreator {
        private NotificationRequestType value;

        public NotificationMessageCreator(NotificationRequestType notificationRequestType) {
            this.value = notificationRequestType;
        }

        public Message createMessage(Session session) throws JMSException {
            String message = null;
            try {
                message = objToString();
            } catch (JAXBException e) {
                throw new JMSException("Could not create notification message!", e.getMessage());
            }

            return session.createObjectMessage(message);
        }

        String objToString() throws JAXBException {
            ObjectFactory objectFactory = new ObjectFactory();
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance("se.inera.webcert.notifications.message.v1");

            Marshaller m = context.createMarshaller();
            m.marshal(objectFactory.createNotificationRequest(value), writer);

            return writer.toString();
        }
    }

}
