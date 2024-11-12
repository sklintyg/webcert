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
package se.inera.intyg.webcert.notification_sender.certificatesender.route;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.apache.camel.test.spring.junit5.MockEndpointsAndSkip;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.notification_sender.certificatesender.testconfig.CertificateCamelTestConfig;

@CamelSpringTest
@ContextConfiguration(classes = CertificateCamelTestConfig.class)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@MockEndpointsAndSkip("bean:certificateStoreProcessor|bean:certificateSendProcessor|bean:certificateRevokeProcessor|bean:sendMessageToRecipientProcessor|direct:certPermanentErrorHandlerEndpoint|direct:certTemporaryErrorHandlerEndpoint")
class RouteTest {

    private static final String MESSAGE_BODY = "message";

    @Autowired
    CamelContext camelContext;

    @Produce("direct://receiveCertificateTransferEndpoint")
    protected ProducerTemplate producerTemplate;

    @EndpointInject("mock:bean:certificateStoreProcessor")
    protected MockEndpoint storeProcessor;

    @EndpointInject("mock:bean:certificateSendProcessor")
    protected MockEndpoint sendProcessor;

    @EndpointInject("mock:bean:certificateRevokeProcessor")
    protected MockEndpoint revokeProcessor;

    @EndpointInject("mock:bean:sendMessageToRecipientProcessor")
    protected MockEndpoint sendMessageProcessor;

    @EndpointInject("mock:direct:certPermanentErrorHandlerEndpoint")
    protected MockEndpoint permanentErrorHandlerEndpoint;

    @EndpointInject("mock:direct:certTemporaryErrorHandlerEndpoint")
    protected MockEndpoint temporaryErrorHandlerEndpoint;

    @Test
    void testNormalStoreRoute() throws InterruptedException {
        storeProcessor.expectedMessageCount(1);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.STORE_MESSAGE));

        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    void testNormalSendRoute() throws InterruptedException {
        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(1);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE));

        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    void testNormalRevokeRoute() throws InterruptedException {
        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(1);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.REVOKE_MESSAGE));

        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Disabled("This test is unstable, INTYGFV-12301")
    @Test
    void testNormalSendMessageRoute() throws InterruptedException {
        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE_TO_RECIPIENT));

        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    void testUnknownMessageType() throws InterruptedException {
        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, "non-existant"));

        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    void testPermanentException() throws InterruptedException {
        sendProcessor.whenAnyExchangeReceived(exchange -> {
            throw new PermanentException("");
        });

        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(1);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);

        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE));

        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    void testTemporaryException() throws InterruptedException {
        revokeProcessor.whenAnyExchangeReceived(exchange -> {
            throw new TemporaryException("");
        });

        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(1);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        assertThrows(CamelExecutionException.class, () ->
            producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.REVOKE_MESSAGE)));

        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    void shallHandleUnexpectedExceptionAsTemporaryError() throws InterruptedException {
        storeProcessor.whenAnyExchangeReceived(exchange -> {
            throw new IllegalArgumentException();
        });

        storeProcessor.expectedMessageCount(1);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        assertThrows(CamelExecutionException.class, () ->
            producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.STORE_MESSAGE))
        );

        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    void shallHandlePermanentExceptionAsPermanentError() throws InterruptedException {
        storeProcessor.whenAnyExchangeReceived(exchange -> {
            throw new PermanentException("Permanent exception!");
        });

        storeProcessor.expectedMessageCount(1);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);

        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.STORE_MESSAGE));

        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }
}
