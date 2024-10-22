/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.notifications.route;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.ANDRAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.HANFRFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.KFSIGN;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.MAKULE;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYFRFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.NYSVFM;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.RADERA;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SIGNAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKAPAT;
import static se.inera.intyg.common.support.common.enumerations.HandelsekodEnum.SKICKA;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import se.inera.intyg.common.fk7263.support.Fk7263EntryPoint;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.ArendeCount;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.support.modules.support.api.notification.SchemaVersion;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.notification_sender.notifications.helper.NotificationTestHelper;
import se.inera.intyg.webcert.notification_sender.notifications.routes.NotificationRouteHeaders;
import se.inera.intyg.webcert.notification_sender.notifications.services.NotificationTypeConverter;
import se.inera.intyg.webcert.notification_sender.notifications.testconfig.NotificationCamelTestConfig;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.riv.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v3.CertificateStatusUpdateForCareType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Handelsekod;
import se.riv.clinicalprocess.healthcond.certificate.v3.Handelse;

@CamelSpringTest
@ContextConfiguration(classes = NotificationCamelTestConfig.class)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@MockEndpoints("bean:notificationAggregator|direct:signatWireTap")
@MockEndpointsAndSkip("bean:notificationTransformer|bean:notificationWSSender|bean:notificationPostProcessor|direct:permanentErrorHandlerEndpoint|direct:temporaryErrorHandlerEndpoint")
public class NotificationRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Produce("direct:receiveNotificationForAggregationRequestEndpoint")
    protected ProducerTemplate producerTemplateAggregation;

    @EndpointInject("mock:direct:signatWireTap")
    protected MockEndpoint signatWireTap;

    @EndpointInject("mock:bean:notificationAggregator")
    protected MockEndpoint notificationAggregator;

    @EndpointInject("mock:bean:notificationTransformer")
    protected MockEndpoint notificationTransformer;

    @EndpointInject("mock:bean:notificationWSSender")
    protected MockEndpoint notificationWSSender;

    @Produce("direct:notificationPostProcessing")
    protected ProducerTemplate producerTemplatePostProcessing;

    @EndpointInject("mock:bean:notificationPostProcessor")
    protected MockEndpoint notificationPostProcessor;

    @EndpointInject("mock:direct:permanentErrorHandlerEndpoint")
    protected MockEndpoint permanentErrorHandlerEndpoint;

    @EndpointInject("mock:direct:temporaryErrorHandlerEndpoint")
    protected MockEndpoint temporaryErrorHandlerEndpoint;

    private final ObjectMapper objectMapper = new CustomObjectMapper();
    private final SortedMap<Integer, Exchange> messagesToSend = new TreeMap<>();
    private static final SchemaVersion SCHEMA_VERSION = SchemaVersion.VERSION_3;
    private static final String CERTIFICATE_ID_1 = UUID.randomUUID().toString();
    private static final String CERTIFICATE_ID_2 = UUID.randomUUID().toString();
    private static final String VERSION_1_2 = "1.2";
    private static final String LOGICAL_ADDRESS = "SE12345678-1234";
    private static final String ROUTE_START_ENDPOINT = "direct:receiveNotificationForAggregationRequestEndpoint";
    private static final Long ENDPOINT_REASSERT_PERIOD = 100L;

    @BeforeEach
    public void setup() {
        setupMockedEndpoints();
    }

    @AfterEach
    public void finish() {
        messagesToSend.clear();
    }

    @Test
    public void testRoutingForCreateEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 1, 1, 1, 0, 0);
        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, SKAPAT);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingForChangeEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(1, 1, 1, 1, 1, 0, 0);
        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingForSignEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(1, 1, 1, 1, 1, 0, 0);
        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, SIGNAT);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingForSendEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 1, 1, 1, 0, 0);
        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, SKICKA);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingForDeleteEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 1, 1, 1, 0, 0);
        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, RADERA);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingForRevokeEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 1, 1, 1, 0, 0);
        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, MAKULE);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingForReadyForSignEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 1, 1, 1, 0, 0);
        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, KFSIGN);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingForQuestionEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 1, 1, 1, 0, 0);
        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, NYFRFM);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingForAnswerEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 1, 1, 1, 0, 0);
        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, NYSVFM);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingForHandledEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 1, 1, 1, 0, 0);
        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, HANFRFM);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingWhenAggregatorException() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(1, 1, 0, 0, 0, 0, 1);

        notificationAggregator.whenAnyExchangeReceived((exchange) -> {
            throw new RuntimeException("Test exception in NotificationAggregator");
        });

        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingWhenTransformerException() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 1, 0, 0, 1, 0);

        notificationTransformer.whenAnyExchangeReceived((exchange) -> {
            throw new RuntimeException("Test exception in NotificationTransformer");
        });

        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, SKAPAT);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testRoutingWhenSenderException() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 1, 1, 0, 1, 0);

        notificationWSSender.whenAnyExchangeReceived((exchange) -> {
            throw new RuntimeException("Test exception in NotificationSender");
        });

        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, SKAPAT);
        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testAggregationOfChangedEvents() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(3, 1, 2, 2, 2, 0, 0);
        setEndpointReassertPeriod();

        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, SKAPAT);
        addMessageToSend(2, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);
        addMessageToSend(3, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);
        addMessageToSend(4, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);

        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testAggregatorPassesOnLatestChangedEvent() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(3, 1, 1, 1, 1, 0, 0);
        setEndpointReassertPeriod();

        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);
        assertDifferentTimestamps();
        addMessageToSend(2, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);
        assertDifferentTimestamps();
        addMessageToSend(3, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);

        final var lastChangedEventCorrelationId = (String) messagesToSend.get(messagesToSend.lastKey()).getMessage()
            .getHeader(NotificationRouteHeaders.CORRELATION_ID);

        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();

        final var changedEventCorrelationIdFromTransformer = (String) notificationTransformer.getExchanges()
            .getLast().getMessage().getHeader(NotificationRouteHeaders.CORRELATION_ID);

        assertEquals(lastChangedEventCorrelationId, changedEventCorrelationIdFromTransformer);
    }

    @Test
    public void testRoutingForFk7263() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(0, 0, 5, 5, 5, 0, 0);
        setEndpointReassertPeriod();

        addMessageToSend(1, CERTIFICATE_ID_1, Fk7263EntryPoint.MODULE_ID, SKAPAT);
        addMessageToSend(2, CERTIFICATE_ID_1, Fk7263EntryPoint.MODULE_ID, ANDRAT);
        addMessageToSend(3, CERTIFICATE_ID_1, Fk7263EntryPoint.MODULE_ID, ANDRAT);
        addMessageToSend(4, CERTIFICATE_ID_1, Fk7263EntryPoint.MODULE_ID, ANDRAT);
        addMessageToSend(5, CERTIFICATE_ID_1, Fk7263EntryPoint.MODULE_ID, SIGNAT);

        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testSignEventFromDifferentCertificatIdShouldNotCancelAggregatedChangedEvents()
        throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(4, 1, 3, 3, 3, 0, 0);
        setEndpointReassertPeriod();

        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, SKAPAT);
        addMessageToSend(2, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);
        addMessageToSend(3, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);
        addMessageToSend(4, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);

        addMessageToSend(5, CERTIFICATE_ID_2, LisjpEntryPoint.MODULE_ID, SIGNAT);

        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    @Test
    public void testSignEventCancelsAggregatedChangedEvents() throws JsonProcessingException, InterruptedException {
        setExpectedMessageCount(3, 1, 2, 2, 2, 0, 0);
        setEndpointReassertPeriod();

        addMessageToSend(1, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, SKAPAT);
        addMessageToSend(2, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);
        addMessageToSend(3, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, ANDRAT);
        addMessageToSend(4, CERTIFICATE_ID_1, LisjpEntryPoint.MODULE_ID, SIGNAT);

        sendMessagesToNotificationRoute(messagesToSend);
        verifyMessageCount();
    }

    private void sendMessagesToNotificationRoute(SortedMap<Integer, Exchange> messagesToSend) {
        for (var exchange : messagesToSend.values()) {
            producerTemplateAggregation.asyncSend(ROUTE_START_ENDPOINT, exchange);
        }
    }

    private void addMessageToSend(int order, String certificateId, String certificateType, HandelsekodEnum event)
        throws JsonProcessingException {
        messagesToSend.put(order, createExchange(certificateId, certificateType, event));
    }

    private void assertDifferentTimestamps() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(10);

    }

    private Exchange createExchange(String certificateId, String certificateType, HandelsekodEnum event)
        throws JsonProcessingException {
        final var message = new DefaultMessage(camelContext);
        message.setHeaders(getMessageHeaders(certificateType, event));
        message.setBody(createNotificationMessage(certificateId, certificateType, event));

        final var exchange = new DefaultExchange(camelContext);
        exchange.setMessage(message);
        return exchange;
    }

    private Map<String, Object> getMessageHeaders(String certificateType, HandelsekodEnum eventCode) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(NotificationRouteHeaders.INTYGS_TYP, certificateType);
        headers.put(NotificationRouteHeaders.INTYG_TYPE_VERSION, VERSION_1_2);
        headers.put(NotificationRouteHeaders.HANDELSE, eventCode.name());
        headers.put(NotificationRouteHeaders.CORRELATION_ID, UUID.randomUUID().toString());
        return headers;
    }

    private void setExpectedMessageCount(int wiretap, int aggregator, int transformer, int sender, int postprocessor, int permanent,
        int temporary) {
        signatWireTap.expectedMessageCount(wiretap);
        notificationAggregator.expectedMessageCount(aggregator);
        notificationTransformer.expectedMessageCount(transformer);
        notificationWSSender.expectedMessageCount(sender);
        notificationPostProcessor.expectedMessageCount(postprocessor);
        permanentErrorHandlerEndpoint.expectedMessageCount(permanent);
        temporaryErrorHandlerEndpoint.expectedMessageCount(temporary);
    }

    private void setEndpointReassertPeriod() {
        signatWireTap.setAssertPeriod(ENDPOINT_REASSERT_PERIOD);
        notificationAggregator.setAssertPeriod(ENDPOINT_REASSERT_PERIOD);
        notificationTransformer.setAssertPeriod(ENDPOINT_REASSERT_PERIOD);
        notificationWSSender.setAssertPeriod(ENDPOINT_REASSERT_PERIOD);
        notificationPostProcessor.setAssertPeriod(ENDPOINT_REASSERT_PERIOD);
        permanentErrorHandlerEndpoint.setAssertPeriod(ENDPOINT_REASSERT_PERIOD);
        temporaryErrorHandlerEndpoint.setAssertPeriod(ENDPOINT_REASSERT_PERIOD);
    }

    private void verifyMessageCount() throws InterruptedException {
        assertIsSatisfied(signatWireTap, notificationAggregator, notificationTransformer, notificationWSSender,
            notificationPostProcessor, permanentErrorHandlerEndpoint, temporaryErrorHandlerEndpoint);
    }

    private void setupMockedEndpoints() {
        notificationWSSender.whenAnyExchangeReceived(exchange -> producerTemplatePostProcessing.send(exchange));
        notificationTransformer.whenAnyExchangeReceived(exchange -> exchange.getMessage().setBody(createStatusUpdate()));
    }

    private String createNotificationMessage(String certificateId, String certificateType, HandelsekodEnum eventEnum)
        throws JsonProcessingException {
        final var notificationMessage = new NotificationMessage();
        notificationMessage.setIntygsId(certificateId);
        notificationMessage.setIntygsTyp(certificateType);
        notificationMessage.setHandelse(eventEnum);
        notificationMessage.setHandelseTid(LocalDateTime.now());
        notificationMessage.setLogiskAdress(LOGICAL_ADDRESS);
        notificationMessage.setMottagnaFragor(ArendeCount.getEmpty());
        notificationMessage.setSkickadeFragor(ArendeCount.getEmpty());
        notificationMessage.setVersion(SCHEMA_VERSION);
        notificationMessage.setUtkast(objectMapper.convertValue(createDraft(certificateId, certificateType), JsonNode.class));
        return objectMapper.writeValueAsString(notificationMessage);
    }

    private Utkast createDraft(String certificateId, String certificateType) {
        final var draft = new Utkast();
        draft.setIntygsId(certificateId);
        draft.setIntygsTyp(certificateType);
        draft.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").orElse(null));
        draft.setModel("draftModel");
        return draft;
    }

    private CertificateStatusUpdateForCareType createStatusUpdate() {
        final var statusUpdate = new CertificateStatusUpdateForCareType();
        statusUpdate.setIntyg(NotificationTestHelper.createIntyg(LisjpEntryPoint.MODULE_ID, VERSION_1_2, CERTIFICATE_ID_1));
        statusUpdate.setHandelse(createEvent());
        statusUpdate.setSkickadeFragor(NotificationTypeConverter.toArenden(ArendeCount.getEmpty()));
        statusUpdate.setMottagnaFragor(NotificationTypeConverter.toArenden(ArendeCount.getEmpty()));
        statusUpdate.setRef(null);
        statusUpdate.setHanteratAv(null);
        return statusUpdate;
    }

    private Handelse createEvent() {
        final var event = new Handelse();
        event.setHandelsekod(createEventCode());
        event.setTidpunkt(LocalDateTime.now());
        return event;
    }

    private Handelsekod createEventCode() {
        final var eventCode = new Handelsekod();
        eventCode.setCode(SKAPAT.value());
        eventCode.setCodeSystem("dfd7bbad-dbe5-4a2f-ba25-f7b9b2cc6b14");
        eventCode.setDisplayName(SKAPAT.description());
        return eventCode;
    }
}
