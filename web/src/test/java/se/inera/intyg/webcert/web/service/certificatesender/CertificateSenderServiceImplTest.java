/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.destination.DestinationResolutionException;
import org.springframework.test.util.ReflectionTestUtils;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.Constants;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class CertificateSenderServiceImplTest {

  private static final String LOGICAL_ADDRESS = "logical address";

  @Mock private Session session;

  @Mock private JmsTemplate template;

  @InjectMocks private CertificateSenderServiceImpl service;

  @BeforeEach
  public void setup() throws Exception {
    ReflectionTestUtils.setField(service, "logicalAddress", LOGICAL_ADDRESS);
    when(session.createTextMessage(anyString()))
        .thenAnswer(invocation -> createTextMessage((String) invocation.getArguments()[0]));
  }

  @Test
  public void storeCertificateTest() throws Exception {
    final String intygsId = "intygsId";
    final String jsonBody = "jsonBody";
    final String intygsTyp = "intygsTyp";

    service.storeCertificate(intygsId, intygsTyp, jsonBody);
    ArgumentCaptor<MessageCreator> messageCaptor = ArgumentCaptor.forClass(MessageCreator.class);
    verify(template).send(messageCaptor.capture());

    Message res = messageCaptor.getValue().createMessage(session);
    assertEquals(Constants.STORE_MESSAGE, res.getStringProperty(Constants.MESSAGE_TYPE));
    assertEquals(intygsId, res.getStringProperty(Constants.INTYGS_ID));
    assertEquals(intygsTyp, res.getStringProperty(Constants.INTYGS_TYP));
    assertEquals(LOGICAL_ADDRESS, res.getStringProperty(Constants.LOGICAL_ADDRESS));
    assertEquals(jsonBody, ((TextMessage) res).getText());
  }

  @Test
  public void storeCertificateJmsException() {
    assertThrows(JmsException.class, () -> {
    doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));

    try {
      service.storeCertificate("intygsId", "intygsTyp", "jsonBody");
    } finally {
      verify(template, times(1)).send(any(MessageCreator.class));
    }
      });
  }

  @Test
  public void sendCertificateTest() throws Exception {
    final String intygsId = "intygsId";
    final Personnummer personId = createPnr("19121212-1212");
    final String jsonBody = "jsonBody";
    final String recipientId = "recipientId";

    service.sendCertificate(intygsId, personId, jsonBody, recipientId);
    ArgumentCaptor<MessageCreator> messageCaptor = ArgumentCaptor.forClass(MessageCreator.class);
    verify(template).send(messageCaptor.capture());

    Message res = messageCaptor.getValue().createMessage(session);
    assertEquals(Constants.SEND_MESSAGE, res.getStringProperty(Constants.MESSAGE_TYPE));
    assertEquals(intygsId, res.getStringProperty(Constants.INTYGS_ID));
    assertEquals(personId.getPersonnummer(), res.getStringProperty(Constants.PERSON_ID));
    assertEquals(recipientId, res.getStringProperty(Constants.RECIPIENT));
    assertEquals(LOGICAL_ADDRESS, res.getStringProperty(Constants.LOGICAL_ADDRESS));
    assertNull(res.getStringProperty(Constants.DELAY_MESSAGE));
    assertEquals(jsonBody, ((TextMessage) res).getText());
  }

  @Test
  public void sendCertificateWithDelayTest() throws Exception {
    final String intygsId = "intygsId";
    final Personnummer personId = createPnr("19121212-1212");
    final String jsonBody = "jsonBody";
    final String recipientId = "recipientId";

    service.sendCertificate(intygsId, personId, jsonBody, recipientId, true);
    ArgumentCaptor<MessageCreator> messageCaptor = ArgumentCaptor.forClass(MessageCreator.class);
    verify(template).send(messageCaptor.capture());

    Message res = messageCaptor.getValue().createMessage(session);
    assertEquals(Constants.SEND_MESSAGE, res.getStringProperty(Constants.MESSAGE_TYPE));
    assertEquals(intygsId, res.getStringProperty(Constants.INTYGS_ID));
    assertEquals(personId.getPersonnummer(), res.getStringProperty(Constants.PERSON_ID));
    assertEquals(recipientId, res.getStringProperty(Constants.RECIPIENT));
    assertEquals(LOGICAL_ADDRESS, res.getStringProperty(Constants.LOGICAL_ADDRESS));
    assertEquals( res.getStringProperty(Constants.DELAY_MESSAGE),"true");
    assertEquals(jsonBody, ((TextMessage) res).getText());
  }

  @Test
  public void sendCertificateJmsException() {
    assertThrows(JmsException.class, () -> {
    doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));

    try {
      service.sendCertificate("intygsId", createPnr("19121212-1212"), "jsonBody", "recipientId");
    } finally {
      verify(template, times(1)).send(any(MessageCreator.class));
    }
      });
  }

  @Test
  public void revokeCertificateTest() throws Exception {
    final String intygsId = "intygsId";
    final String xmlBody = "xmlBody";
    final String intygsTyp = "intygsTyp";
    final String intygsTypVersion = "intygsTypVersion";

    service.revokeCertificate(intygsId, xmlBody, intygsTyp, intygsTypVersion);
    ArgumentCaptor<MessageCreator> messageCaptor = ArgumentCaptor.forClass(MessageCreator.class);
    verify(template).send(messageCaptor.capture());

    Message res = messageCaptor.getValue().createMessage(session);
    assertEquals(Constants.REVOKE_MESSAGE, res.getStringProperty(Constants.MESSAGE_TYPE));
    assertEquals(intygsId, res.getStringProperty(Constants.INTYGS_ID));
    assertEquals(intygsTyp, res.getStringProperty(Constants.INTYGS_TYP));
    assertEquals(intygsTypVersion, res.getStringProperty(Constants.INTYGS_TYP_VERSION));
    assertEquals(LOGICAL_ADDRESS, res.getStringProperty(Constants.LOGICAL_ADDRESS));
    assertEquals(xmlBody, ((TextMessage) res).getText());
  }

  @Test
  public void revokeCertificateJmsException() {
    assertThrows(JmsException.class, () -> {
    doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));

    try {
      service.revokeCertificate("intygsId", "xmlBody", "intygsTyp", "1.0");
    } finally {
      verify(template, times(1)).send(any(MessageCreator.class));
    }
      });
  }

  @Test
  public void sendMessageToRecipientTest() throws Exception {
    final String intygsId = "intygsId";
    final String xmlBody = "xmlBody";

    service.sendMessageToRecipient(intygsId, xmlBody);
    ArgumentCaptor<MessageCreator> messageCaptor = ArgumentCaptor.forClass(MessageCreator.class);
    verify(template).send(messageCaptor.capture());

    Message res = messageCaptor.getValue().createMessage(session);
    assertEquals(
        Constants.SEND_MESSAGE_TO_RECIPIENT, res.getStringProperty(Constants.MESSAGE_TYPE));
    assertEquals(intygsId, res.getStringProperty(Constants.INTYGS_ID));
    assertEquals(LOGICAL_ADDRESS, res.getStringProperty(Constants.LOGICAL_ADDRESS));
    assertEquals(xmlBody, ((TextMessage) res).getText());
  }

  @Test
  public void sendMessageToRecipientJmsException() {
    assertThrows(JmsException.class, () -> {
    doThrow(new DestinationResolutionException("")).when(template).send(any(MessageCreator.class));

    try {
      service.sendMessageToRecipient("intygsId", "xmlBody");
    } finally {
      verify(template, times(1)).send(any(MessageCreator.class));
    }
      });
  }

  @Test
  public void testCheckJmsTemplateNoTemplateAvailable() {
    ReflectionTestUtils.setField(service, "jmsTemplate", null);
    service.checkJmsTemplate();
    // no exception is thrown
  }

  private TextMessage createTextMessage(String s) throws JMSException {
    ActiveMQTextMessage message = new ActiveMQTextMessage();
    message.setText(s);
    return message;
  }

  private Personnummer createPnr(String personId) {
    return Personnummer.createPersonnummer(personId)
        .orElseThrow(
            () -> new IllegalArgumentException("Could not parse passed personnummer: " + personId));
  }
}
