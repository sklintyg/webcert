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
package se.inera.intyg.webcert.notification_sender.certificatesender.route;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.CamelTestContextBootstrapper;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/certificates/unit-test-certificate-sender-config.xml")
@BootstrapWith(CamelTestContextBootstrapper.class)
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class}) // Suppresses warning
@MockEndpointsAndSkip("bean:certificateStoreProcessor|bean:certificateSendProcessor|bean:certificateRevokeProcessor|bean:sendMessageToRecipientProcessor|direct:certPermanentErrorHandlerEndpoint|direct:certTemporaryErrorHandlerEndpoint")
public class RouteTest {

    private static final String MESSAGE_BODY = "message";

    @Autowired
    CamelContext camelContext;

    @Produce(uri = "direct://receiveCertificateTransferEndpoint")
    private ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:bean:certificateStoreProcessor")
    private MockEndpoint storeProcessor;

    @EndpointInject(uri = "mock:bean:certificateSendProcessor")
    private MockEndpoint sendProcessor;

    @EndpointInject(uri = "mock:bean:certificateRevokeProcessor")
    private MockEndpoint revokeProcessor;

    @EndpointInject(uri = "mock:bean:sendMessageToRecipientProcessor")
    private MockEndpoint sendMessageProcessor;

    @EndpointInject(uri = "mock:direct:certPermanentErrorHandlerEndpoint")
    private MockEndpoint permanentErrorHandlerEndpoint;

    @EndpointInject(uri = "mock:direct:certTemporaryErrorHandlerEndpoint")
    private MockEndpoint temporaryErrorHandlerEndpoint;

    @Before
    public void setup() {
        MockEndpoint.resetMocks(camelContext);
    }

    @Test
    @DirtiesContext
    public void testNormalStoreRoute() throws InterruptedException {
        // Given
        storeProcessor.expectedMessageCount(1);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.STORE_MESSAGE));

        // Then
        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    public void testNormalSendRoute() throws InterruptedException {
        // Given
        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(1);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE));

        // Then
        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    public void testNormalRevokeRoute() throws InterruptedException {
        // Given
        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(1);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.REVOKE_MESSAGE));

        // Then
        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    public void testNormalSendMessageRoute() throws InterruptedException {
        // Given
        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(1);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE_TO_RECIPIENT));

        // Then
        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    public void testUnknownMessageType() throws InterruptedException {
        // Given
        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, "non-existant"));

        // Then
        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    public void testPermanentException() throws InterruptedException {
        // Given
        sendProcessor.whenAnyExchangeReceived(exchange -> {
            throw new PermanentException("");
        });

        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(1);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);

        // When
        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE));

        // Then
        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test(expected = CamelExecutionException.class)
    public void testTemporaryException() throws InterruptedException {
        // Given
        revokeProcessor.whenAnyExchangeReceived(exchange -> {
            throw new TemporaryException("");
        });

        storeProcessor.expectedMessageCount(0);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(1);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(1);
        permanentErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.REVOKE_MESSAGE));

        // Then
        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }

    @Test
    public void testUnexpectedException() throws InterruptedException {
        // Given
        storeProcessor.whenAnyExchangeReceived(exchange -> {
            throw new IllegalArgumentException();
        });

        storeProcessor.expectedMessageCount(1);
        sendProcessor.expectedMessageCount(0);
        revokeProcessor.expectedMessageCount(0);
        sendMessageProcessor.expectedMessageCount(0);
        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
        permanentErrorHandlerEndpoint.expectedMessageCount(1);

        // When
        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.of(Constants.MESSAGE_TYPE, Constants.STORE_MESSAGE));

        // Then
        assertIsSatisfied(storeProcessor);
        assertIsSatisfied(sendProcessor);
        assertIsSatisfied(revokeProcessor);
        assertIsSatisfied(sendMessageProcessor);
        assertIsSatisfied(temporaryErrorHandlerEndpoint);
        assertIsSatisfied(permanentErrorHandlerEndpoint);
    }
}
