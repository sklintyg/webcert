/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.certificatesender;

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

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.common.common.Constants;

/**
 * Created by eriklupander on 2015-05-20.
 */
@Component
public class CertificateSenderServiceImpl implements CertificateSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateSenderServiceImpl.class);

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
    public void sendCertificate(String intygsId, Personnummer personId, String recipientId) {
        jmsTemplate.send(new SendCertificateMessageCreator(intygsId, personId, recipientId, logicalAddress));
    }

    @Override
    public void revokeCertificate(String intygsId, String xmlBody) {
        jmsTemplate.send(new RevokeCertificateMessageCreator(intygsId, xmlBody, logicalAddress));
    }


    static final class StoreCertificateMessageCreator implements MessageCreator {

        private final String intygsId;
        private final String body;
        private final String intygsTyp;
        private final String logicalAddress;

        private StoreCertificateMessageCreator(String intygsId, String body, String intygsTyp, String logicalAddress) {
            this.intygsId = intygsId;
            this.body = body;
            this.intygsTyp = intygsTyp;
            this.logicalAddress = logicalAddress;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            Message message = session.createTextMessage(this.body);
            message.setStringProperty(Constants.JMSX_GROUP_ID, intygsId);
            message.setStringProperty(Constants.MESSAGE_TYPE, Constants.STORE_MESSAGE);
            message.setStringProperty(Constants.INTYGS_TYP, intygsTyp);
            message.setStringProperty(Constants.LOGICAL_ADDRESS, logicalAddress);
            return message;
        }
    }

    static final class SendCertificateMessageCreator implements MessageCreator {

        private final String intygsId;
        private final Personnummer personId;
        private final String recipientId;
        private final String logicalAddress;

        private SendCertificateMessageCreator(String intygsId, Personnummer personId, String recipientId, String logicalAddress) {
            this.intygsId = intygsId;
            this.personId = personId;
            this.recipientId = recipientId;
            this.logicalAddress = logicalAddress;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            Message message = session.createTextMessage();
            message.setStringProperty(Constants.JMSX_GROUP_ID, intygsId);
            message.setStringProperty(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE);

            message.setStringProperty(Constants.INTYGS_ID, intygsId);
            message.setStringProperty(Constants.PERSON_ID, personId.getPersonnummer());
            message.setStringProperty(Constants.RECIPIENT, recipientId);
            message.setStringProperty(Constants.LOGICAL_ADDRESS, logicalAddress);
            return message;
        }
    }

    static final class RevokeCertificateMessageCreator implements MessageCreator {
        private final String intygsId;
        private final String xmlBody;
        private final String logicalAddress;

        private RevokeCertificateMessageCreator(String intygsId, String xmlBody, String logicalAddress) {
            this.intygsId = intygsId;
            this.xmlBody = xmlBody;
            this.logicalAddress = logicalAddress;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            Message message = session.createTextMessage(xmlBody);
            message.setStringProperty(Constants.JMSX_GROUP_ID, intygsId);
            message.setStringProperty(Constants.MESSAGE_TYPE, Constants.REVOKE_MESSAGE);

            message.setStringProperty(Constants.INTYGS_ID, intygsId);
            message.setStringProperty(Constants.LOGICAL_ADDRESS, logicalAddress);
            return message;
        }
    }
}
