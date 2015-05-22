package se.inera.webcert.service.certificatesender;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import se.inera.webcert.common.Constants;

/**
 * Created by eriklupander on 2015-05-20.
 */
@Component
public class CertificateSenderServiceImpl implements CertificateSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateSenderServiceImpl.class);

    private static final String JMSX_GROUP_ID = "JMSXGroupID";

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;

    @Autowired(required = false)
    @Qualifier("jmsCertificateSenderTemplate")
    private JmsTemplate jmsTemplate;

    @PostConstruct
    public void checkJmsTemplate() {
        if (jmsTemplate == null) {
            LOGGER.error("CertificateSender JMS template is unavailable!");
        }
    }

    @Override
    public void storeCertificate(String intygsId, String intygsTyp, String jsonBody) {
        jmsTemplate.send(new StoreCertificateMessageCreator(intygsId, jsonBody, intygsTyp, logicalAddress));
    }

    @Override
    public void sendCertificate(String intygsId, String personId, String recipientId) {
        jmsTemplate.send(new SendCertificateMessageCreator(intygsId, personId, recipientId, logicalAddress));
    }

    @Override
    public void revokeCertificate(String intygsId, String xmlBody) {
        jmsTemplate.send(new RevokeCertificateMessageCreator(intygsId, xmlBody, logicalAddress));
    }


    static final class StoreCertificateMessageCreator implements MessageCreator {

        private String intygsId;
        private String body;
        private String intygsTyp;
        private String logicalAddress;

        public StoreCertificateMessageCreator(String intygsId, String body, String intygsTyp, String logicalAddress) {
            this.intygsId = intygsId;
            this.body = body;
            this.intygsTyp = intygsTyp;
            this.logicalAddress = logicalAddress;
        }

        public Message createMessage(Session session) throws JMSException {
            Message message = session.createTextMessage(this.body);
            message.setStringProperty(JMSX_GROUP_ID, intygsId);
            message.setStringProperty(Constants.MESSAGE_TYPE, Constants.STORE_MESSAGE);
            message.setStringProperty(Constants.INTYGS_TYP, intygsTyp);
            message.setStringProperty(Constants.LOGICAL_ADDRESS, logicalAddress);
            return message;
        }
    }

    static final class SendCertificateMessageCreator implements MessageCreator {

        private String intygsId;
        private String personId;
        private String recipientId;
        private String logicalAddress;

        public SendCertificateMessageCreator(String intygsId, String personId, String recipientId, String logicalAddress) {
            this.intygsId = intygsId;
            this.personId = personId;
            this.recipientId = recipientId;
            this.logicalAddress = logicalAddress;
        }

        public Message createMessage(Session session) throws JMSException {
            Message message = session.createTextMessage();
            message.setStringProperty(JMSX_GROUP_ID, intygsId);
            message.setStringProperty(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE);

            message.setStringProperty(Constants.INTYGS_ID, intygsId);
            message.setStringProperty(Constants.PERSON_ID, personId);
            message.setStringProperty(Constants.RECIPIENT, recipientId);
            message.setStringProperty(Constants.LOGICAL_ADDRESS, logicalAddress);
            return message;
        }
    }

    static final class RevokeCertificateMessageCreator implements MessageCreator {
        private final String intygsId;
        private final String xmlBody;
        private final String logicalAddress;

        public RevokeCertificateMessageCreator(String intygsId, String xmlBody, String logicalAddress) {
            this.intygsId = intygsId;
            this.xmlBody = xmlBody;
            this.logicalAddress = logicalAddress;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            Message message = session.createTextMessage(xmlBody);
            message.setStringProperty(JMSX_GROUP_ID, intygsId);
            message.setStringProperty(Constants.MESSAGE_TYPE, Constants.REVOKE_MESSAGE);

            message.setStringProperty(Constants.INTYGS_ID, intygsId);
            message.setStringProperty(Constants.LOGICAL_ADDRESS, logicalAddress);
            return message;
        }
    }
}
