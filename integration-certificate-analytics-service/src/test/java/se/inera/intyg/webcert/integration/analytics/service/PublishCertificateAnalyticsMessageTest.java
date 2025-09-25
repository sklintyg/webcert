/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.integration.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.jms.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import se.inera.intyg.webcert.integration.analytics.model.AnalyticsCertificate;
import se.inera.intyg.webcert.integration.analytics.model.AnalyticsEvent;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessage;
import se.inera.intyg.webcert.integration.analytics.model.CertificateAnalyticsMessageType;
import se.inera.intyg.webcert.logging.MdcLogConstants;

@ExtendWith(MockitoExtension.class)
class PublishCertificateAnalyticsMessageTest {

    @Mock
    private CertificateAnalyticsServiceProfile certificateAnalyticsServiceProfile;

    @Mock
    private JmsTemplate jmsTemplateForCertificateAnalyticsMessages;

    @InjectMocks
    private PublishCertificateAnalyticsMessage publishCertificateAnalyticsMessage;

    @Test
    void shallPublishMessageToJmsQueue() {
        final var expected = CertificateAnalyticsMessage.builder()
            .certificate(
                AnalyticsCertificate.builder()
                    .id("test")
                    .build()
            )
            .build();

        when(certificateAnalyticsServiceProfile.isEnabled()).thenReturn(true);

        final var captor = ArgumentCaptor.forClass(CertificateAnalyticsMessage.class);

        doNothing().when(jmsTemplateForCertificateAnalyticsMessages).convertAndSend(captor.capture(), any());

        publishCertificateAnalyticsMessage.publishEvent(expected);

        assertEquals(expected, captor.getValue());
    }

    @Test
    void shallNotPublishMessagesIfCertificateAnalyticsNotActive() {
        when(certificateAnalyticsServiceProfile.isEnabled()).thenReturn(false);

        final var message = CertificateAnalyticsMessage.builder()
            .certificate(
                AnalyticsCertificate.builder()
                    .id("test")
                    .build()
            )
            .build();

        publishCertificateAnalyticsMessage.publishEvent(message);

        verifyNoInteractions(jmsTemplateForCertificateAnalyticsMessages);
    }

    @Nested
    class MessagePropertyTests {

        @Captor
        private ArgumentCaptor<MessagePostProcessor> mppCaptor;

        private final String sessionId = "sess-456";
        private final String traceId = "trace-789";
        private final CertificateAnalyticsMessage message = CertificateAnalyticsMessage.builder()
            .certificate(
                AnalyticsCertificate.builder()
                    .id("test")
                    .build()
            )
            .event(
                AnalyticsEvent.builder()
                    .messageType(CertificateAnalyticsMessageType.DRAFT_CREATED)
                    .build()
            )
            .build();

        @BeforeEach
        void setUp() {
            MDC.put(MdcLogConstants.SESSION_ID_KEY, sessionId);
            MDC.put(MdcLogConstants.TRACE_ID_KEY, traceId);

            when(certificateAnalyticsServiceProfile.isEnabled()).thenReturn(true);
        }

        @AfterEach
        void tearDown() {
            MDC.clear();
        }

        @Test
        void shallSetMessageIdPropertyWhenPublishing() throws Exception {
            publishCertificateAnalyticsMessage.publishEvent(message);

            verify(jmsTemplateForCertificateAnalyticsMessages).convertAndSend(eq(message), mppCaptor.capture());

            final var mpp = mppCaptor.getValue();
            final var jmsMsg = mock(Message.class);
            mpp.postProcessMessage(jmsMsg);

            verify(jmsMsg).setStringProperty("messageId", message.getMessageId());
        }

        @Test
        void shallSetSessionIdPropertyWhenPublishing() throws Exception {
            publishCertificateAnalyticsMessage.publishEvent(message);

            verify(jmsTemplateForCertificateAnalyticsMessages).convertAndSend(eq(message), mppCaptor.capture());

            final var mpp = mppCaptor.getValue();
            final var jmsMsg = mock(Message.class);
            mpp.postProcessMessage(jmsMsg);

            verify(jmsMsg).setStringProperty("sessionId", sessionId);
        }

        @Test
        void shallSetTraceIdPropertyWhenPublishing() throws Exception {
            publishCertificateAnalyticsMessage.publishEvent(message);

            verify(jmsTemplateForCertificateAnalyticsMessages).convertAndSend(eq(message), mppCaptor.capture());

            final var mpp = mppCaptor.getValue();
            final var jmsMsg = mock(Message.class);
            mpp.postProcessMessage(jmsMsg);

            verify(jmsMsg).setStringProperty("traceId", traceId);
        }

        @Test
        void shallSetTypePropertyWhenPublishing() throws Exception {
            publishCertificateAnalyticsMessage.publishEvent(message);

            verify(jmsTemplateForCertificateAnalyticsMessages).convertAndSend(eq(message), mppCaptor.capture());

            final var mpp = mppCaptor.getValue();
            final var jmsMsg = mock(Message.class);
            mpp.postProcessMessage(jmsMsg);

            verify(jmsMsg).setStringProperty("_type", message.getType());
        }

        @Test
        void shallSetSchemaVersionPropertyWhenPublishing() throws Exception {
            publishCertificateAnalyticsMessage.publishEvent(message);

            verify(jmsTemplateForCertificateAnalyticsMessages).convertAndSend(eq(message), mppCaptor.capture());

            final var mpp = mppCaptor.getValue();
            final var jmsMsg = mock(Message.class);
            mpp.postProcessMessage(jmsMsg);

            verify(jmsMsg).setStringProperty("schemaVersion", message.getSchemaVersion());
        }

        @Test
        void shallSetContentTypePropertyWhenPublishing() throws Exception {
            publishCertificateAnalyticsMessage.publishEvent(message);

            verify(jmsTemplateForCertificateAnalyticsMessages).convertAndSend(eq(message), mppCaptor.capture());

            final var mpp = mppCaptor.getValue();
            final var jmsMsg = mock(Message.class);
            mpp.postProcessMessage(jmsMsg);

            verify(jmsMsg).setStringProperty("contentType", "application/json");
        }

        @Test
        void shallSetMessageTypePropertyWhenPublishing() throws Exception {
            publishCertificateAnalyticsMessage.publishEvent(message);

            verify(jmsTemplateForCertificateAnalyticsMessages).convertAndSend(eq(message), mppCaptor.capture());

            final var mpp = mppCaptor.getValue();
            final var jmsMsg = mock(Message.class);
            mpp.postProcessMessage(jmsMsg);

            verify(jmsMsg).setStringProperty("messageType", message.getEvent().getMessageType().toString());
        }
    }
}