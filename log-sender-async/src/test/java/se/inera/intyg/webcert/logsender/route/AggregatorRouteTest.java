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

import org.apache.camel.CamelContext;
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

import se.inera.intyg.common.logmessages.ActivityType;
import se.inera.intyg.common.logmessages.type.LogMessageConstants;
import se.inera.intyg.common.logmessages.type.LogMessageType;
import se.inera.intyg.webcert.logsender.helper.TestDataHelper;

import com.google.common.collect.ImmutableMap;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration("/logsender/unit-test-certificate-sender-config.xml")
@BootstrapWith(CamelTestContextBootstrapper.class)
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class}) // Suppresses warning
@MockEndpointsAndSkip("bean:logMessageAggregationProcessor|direct:logMessagePermanentErrorHandlerEndpoint|direct:logMessageTemporaryErrorHandlerEndpoint")
public class AggregatorRouteTest {

    private static final String MESSAGE_BODY = "message";

    @Autowired
    CamelContext camelContext;

    @Produce(uri = "direct://aggregatorRoute")
    private ProducerTemplate producerTemplate;

    @EndpointInject(uri = "mock:bean:logMessageAggregationProcessor")
    private MockEndpoint logMessageAggregationProcessor;

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
        logMessageAggregationProcessor.expectedMessageCount(1);
        logMessagePermanentErrorHandlerEndpoint.expectedMessageCount(0);
        logMessageTemporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        for (int a = 0; a < 5; a++) {
            producerTemplate.sendBodyAndHeaders(TestDataHelper.buildAbstractLogMessageList(ActivityType.READ), ImmutableMap.<String, Object> of(LogMessageConstants.LOG_TYPE, LogMessageType.SINGLE.name()));
        }

        // Then
        assertIsSatisfied(logMessageAggregationProcessor);
        assertIsSatisfied(logMessagePermanentErrorHandlerEndpoint);
        assertIsSatisfied(logMessageTemporaryErrorHandlerEndpoint);
    }

    @Test
    @DirtiesContext
    public void testUnkownTypeLogStoreRoute() throws InterruptedException {
        // Given
        logMessageAggregationProcessor.expectedMessageCount(0);
        logMessagePermanentErrorHandlerEndpoint.expectedMessageCount(0);
        logMessageTemporaryErrorHandlerEndpoint.expectedMessageCount(0);

        // When
        for (int a = 0; a < 4; a++) {
            producerTemplate.sendBodyAndHeaders(TestDataHelper.buildAbstractLogMessageList(ActivityType.READ), ImmutableMap.<String, Object> of(LogMessageConstants.LOG_TYPE, LogMessageType.SINGLE.name()));
        }

        // Then
        assertIsSatisfied(logMessageAggregationProcessor);
        assertIsSatisfied(logMessagePermanentErrorHandlerEndpoint);
        assertIsSatisfied(logMessageTemporaryErrorHandlerEndpoint);
    }
}
