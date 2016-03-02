/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.logsender.route;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import java.util.Arrays;

import javax.xml.ws.WebServiceException;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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

import se.inera.intyg.common.logmessages.ActivityType;
import se.inera.intyg.common.logmessages.type.LogMessageConstants;
import se.inera.intyg.common.logmessages.type.LogMessageType;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;
import se.inera.intyg.webcert.logsender.helper.TestDataHelper;

import com.google.common.collect.ImmutableMap;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/logsender/unit-test-certificate-sender-config.xml")
@BootstrapWith(CamelTestContextBootstrapper.class)
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class}) // Suppresses warning
@MockEndpointsAndSkip("bean:logMessageSendProcessor|direct:logMessagePermanentErrorHandlerEndpoint|direct:logMessageTemporaryErrorHandlerEndpoint")
public class ReceiveAggregatedLogMessageRouteTest {

    @Autowired
    CamelContext camelContext;

    @Produce(uri = "direct://receiveAggregatedLogMessageEndpoint")
    private ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:bean:logMessageSendProcessor")
    private MockEndpoint logMessageSendProcessor;

    @EndpointInject(uri = "mock:direct:logMessagePermanentErrorHandlerEndpoint")
    private MockEndpoint logMessagePermanentErrorHandlerEndpoint;

    @EndpointInject(uri = "mock:direct:logMessageTemporaryErrorHandlerEndpoint")
    private MockEndpoint logMessageTemporaryErrorHandlerEndpoint;

    @Before
    public void setup() {
        MockEndpoint.resetMocks(camelContext);
    }

    @Test
    @DirtiesContext
    public void testNormalLogStoreRoute() throws InterruptedException {
        // Given
        logMessageSendProcessor.expectedMessageCount(1);
        logMessagePermanentErrorHandlerEndpoint.expectedMessageCount(0);
        logMessageTemporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        for (int a = 0; a < 1; a++) {
            producerTemplate.sendBodyAndHeaders(Arrays.asList(TestDataHelper.buildAbstractLogMessageList(ActivityType.READ)), ImmutableMap.<String, Object> of(LogMessageConstants.LOG_TYPE, LogMessageType.SINGLE.name()));
        }

        // Then
        assertIsSatisfied(logMessageSendProcessor);
        assertIsSatisfied(logMessagePermanentErrorHandlerEndpoint);
        assertIsSatisfied(logMessageTemporaryErrorHandlerEndpoint);
    }

    @Test
    @DirtiesContext
    public void testPermanentException() throws InterruptedException {
        // Given
        //        // Given
        logMessageSendProcessor.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                throw new PermanentException("");
            }
        });
        logMessageSendProcessor.expectedMessageCount(1);
        logMessagePermanentErrorHandlerEndpoint.expectedMessageCount(1);
        logMessageTemporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        for (int a = 0; a < 1; a++) {
            producerTemplate.sendBodyAndHeaders(Arrays.asList(TestDataHelper.buildAbstractLogMessageList(ActivityType.READ)), ImmutableMap.<String, Object> of(LogMessageConstants.LOG_TYPE, LogMessageType.SINGLE.name()));
        }

        // Then
        assertIsSatisfied(logMessageSendProcessor);
        assertIsSatisfied(logMessagePermanentErrorHandlerEndpoint);
        assertIsSatisfied(logMessageTemporaryErrorHandlerEndpoint);
    }

    @Test(expected = CamelExecutionException.class)
    @DirtiesContext
    public void testTemporaryException() throws InterruptedException {
        // Given
        //        // Given
        logMessageSendProcessor.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                throw new TemporaryException("");
            }
        });
        logMessageSendProcessor.expectedMessageCount(1);
        logMessagePermanentErrorHandlerEndpoint.expectedMessageCount(0);
        logMessageTemporaryErrorHandlerEndpoint.expectedMessageCount(1);

        // When
        for (int a = 0; a < 1; a++) {
            producerTemplate.sendBodyAndHeaders(Arrays.asList(TestDataHelper.buildAbstractLogMessageList(ActivityType.READ)), ImmutableMap.<String, Object> of(LogMessageConstants.LOG_TYPE, LogMessageType.SINGLE.name()));
        }

        // Then
        assertIsSatisfied(logMessageSendProcessor);
        assertIsSatisfied(logMessagePermanentErrorHandlerEndpoint);
        assertIsSatisfied(logMessageTemporaryErrorHandlerEndpoint);
    }

    @Test
    @DirtiesContext
    public void testWebServiceException() throws InterruptedException {
        // Given
        //        // Given
        logMessageSendProcessor.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                throw new WebServiceException("");
            }
        });
        logMessageSendProcessor.expectedMessageCount(1);
        logMessagePermanentErrorHandlerEndpoint.expectedMessageCount(1);
        logMessageTemporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        for (int a = 0; a < 1; a++) {
            producerTemplate.sendBodyAndHeaders(Arrays.asList(TestDataHelper.buildAbstractLogMessageList(ActivityType.READ)), ImmutableMap.<String, Object> of(LogMessageConstants.LOG_TYPE, LogMessageType.SINGLE.name()));
        }

        // Then
        assertIsSatisfied(logMessageSendProcessor);
        assertIsSatisfied(logMessagePermanentErrorHandlerEndpoint);
        assertIsSatisfied(logMessageTemporaryErrorHandlerEndpoint);
    }
}
