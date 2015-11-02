package se.inera.webcert.certificatesender.route;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringDelegatingTestContextLoader;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.ImmutableMap;

import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import se.inera.webcert.certificatesender.routes.CertificateRouteBuilder;
import se.inera.webcert.common.Constants;
import se.inera.webcert.exception.PermanentException;
import se.inera.webcert.exception.TemporaryException;

//@RunWith(CamelSpringJUnit4ClassRunner.class)
//@ContextConfiguration //("/certificates/unit-test-certificate-sender-config.xml")
//@MockEndpointsAndSkip("bean:certificateStoreProcessor|bean:certificateSendProcessor|bean:certificateRevokeProcessor|direct:certPermanentErrorHandlerEndpoint|direct:certTemporaryErrorHandlerEndpoint")

//@RunWith(CamelSpringJUnit4ClassRunner.class)
//@ContextConfiguration("/certificates/unit-test-certificate-sender-config.xml")
//classes = {CertificateRouteBuilder.class}, loader = CamelSpringDelegatingTestContextLoader.class)
//@MockEndpointsAndSkip("bean:certificateStoreProcessor|bean:certificateSendProcessor|bean:certificateRevokeProcessor|direct:certPermanentErrorHandlerEndpoint|direct:certTemporaryErrorHandlerEndpoint")

public class RouteTest { //extends AbstractJUnit4SpringContextTests {

//    private static final String MESSAGE_BODY = "message";
//
//  //  @Autowired
//  //  CamelContext camelContext;
//
//    @Produce(uri = "direct://receiveCertificateTransferEndpoint")
//    private ProducerTemplate producerTemplate;
//
//    @EndpointInject(uri = "mock:bean:certificateStoreProcessor")
//    private MockEndpoint storeProcessor;
//
//    @EndpointInject(uri = "mock:bean:certificateSendProcessor")
//    private MockEndpoint sendProcessor;
//
//    @EndpointInject(uri = "mock:bean:certificateRevokeProcessor")
//    private MockEndpoint revokeProcessor;
//
//    @EndpointInject(uri = "mock:direct:certPermanentErrorHandlerEndpoint")
//    private MockEndpoint permanentErrorHandlerEndpoint;
//
//    @EndpointInject(uri = "mock:direct:certTemporaryErrorHandlerEndpoint")
//    private MockEndpoint temporaryErrorHandlerEndpoint;
//
//    @Before
//    public void setup() {
//        //MockEndpoint.resetMocks(camelContext);
//    }
//
//    @Test
//    @DirtiesContext
//    public void testNormalStoreRoute() throws InterruptedException {
//        // Given
//        storeProcessor.expectedMessageCount(1);
//        sendProcessor.expectedMessageCount(0);
//        revokeProcessor.expectedMessageCount(0);
//        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
//        permanentErrorHandlerEndpoint.expectedMessageCount(0);
//
//        // When
//        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.<String, Object> of(Constants.MESSAGE_TYPE, Constants.STORE_MESSAGE));
//
//        // Then
//        assertIsSatisfied(storeProcessor);
//        assertIsSatisfied(sendProcessor);
//        assertIsSatisfied(revokeProcessor);
//        assertIsSatisfied(temporaryErrorHandlerEndpoint);
//        assertIsSatisfied(permanentErrorHandlerEndpoint);
//    }
//
//    @Test
//    public void testNormalSendRoute() throws InterruptedException {
//        // Given
//        storeProcessor.expectedMessageCount(0);
//        sendProcessor.expectedMessageCount(1);
//        revokeProcessor.expectedMessageCount(0);
//        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
//        permanentErrorHandlerEndpoint.expectedMessageCount(0);
//
//        // When
//        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.<String, Object> of(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE));
//
//        // Then
//        assertIsSatisfied(storeProcessor);
//        assertIsSatisfied(sendProcessor);
//        assertIsSatisfied(revokeProcessor);
//        assertIsSatisfied(temporaryErrorHandlerEndpoint);
//        assertIsSatisfied(permanentErrorHandlerEndpoint);
//    }
//
//    @Test
//    public void testNormalRevokeRoute() throws InterruptedException {
//        // Given
//        storeProcessor.expectedMessageCount(0);
//        sendProcessor.expectedMessageCount(0);
//        revokeProcessor.expectedMessageCount(1);
//        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
//        permanentErrorHandlerEndpoint.expectedMessageCount(0);
//
//        // When
//        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.<String, Object> of(Constants.MESSAGE_TYPE, Constants.REVOKE_MESSAGE));
//
//        // Then
//        assertIsSatisfied(storeProcessor);
//        assertIsSatisfied(sendProcessor);
//        assertIsSatisfied(revokeProcessor);
//        assertIsSatisfied(temporaryErrorHandlerEndpoint);
//        assertIsSatisfied(permanentErrorHandlerEndpoint);
//    }
//
//    @Test
//    public void testUnknownMessageType() throws InterruptedException {
//        // Given
//        storeProcessor.expectedMessageCount(0);
//        sendProcessor.expectedMessageCount(0);
//        revokeProcessor.expectedMessageCount(0);
//        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
//        permanentErrorHandlerEndpoint.expectedMessageCount(0);
//
//        // When
//        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.<String, Object> of(Constants.MESSAGE_TYPE, "non-existant"));
//
//        // Then
//        assertIsSatisfied(storeProcessor);
//        assertIsSatisfied(sendProcessor);
//        assertIsSatisfied(revokeProcessor);
//        assertIsSatisfied(temporaryErrorHandlerEndpoint);
//        assertIsSatisfied(permanentErrorHandlerEndpoint);
//    }
//
//    @Test
//    public void testPermanentException() throws InterruptedException {
//        // Given
//        sendProcessor.whenAnyExchangeReceived(new Processor() {
//            @Override
//            public void process(Exchange exchange) throws Exception {
//                throw new PermanentException();
//            }
//        });
//
//        storeProcessor.expectedMessageCount(0);
//        sendProcessor.expectedMessageCount(1);
//        revokeProcessor.expectedMessageCount(0);
//        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
//        permanentErrorHandlerEndpoint.expectedMessageCount(1);
//
//        // When
//        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.<String, Object> of(Constants.MESSAGE_TYPE, Constants.SEND_MESSAGE));
//
//        // Then
//        assertIsSatisfied(storeProcessor);
//        assertIsSatisfied(sendProcessor);
//        assertIsSatisfied(revokeProcessor);
//        assertIsSatisfied(temporaryErrorHandlerEndpoint);
//        assertIsSatisfied(permanentErrorHandlerEndpoint);
//    }
//
//    @Test(expected = CamelExecutionException.class)
//    public void testTemporaryException() throws InterruptedException {
//        // Given
//        revokeProcessor.whenAnyExchangeReceived(new Processor() {
//            @Override
//            public void process(Exchange exchange) throws Exception {
//                throw new TemporaryException();
//            }
//        });
//
//        storeProcessor.expectedMessageCount(0);
//        sendProcessor.expectedMessageCount(0);
//        revokeProcessor.expectedMessageCount(1);
//        temporaryErrorHandlerEndpoint.expectedMessageCount(1);
//        permanentErrorHandlerEndpoint.expectedMessageCount(0);
//
//        // When
//        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.<String, Object> of(Constants.MESSAGE_TYPE, Constants.REVOKE_MESSAGE));
//
//        // Then
//        assertIsSatisfied(storeProcessor);
//        assertIsSatisfied(sendProcessor);
//        assertIsSatisfied(revokeProcessor);
//        assertIsSatisfied(temporaryErrorHandlerEndpoint);
//        assertIsSatisfied(permanentErrorHandlerEndpoint);
//    }
//
//    @Test
//    public void testUnexpectedException() throws InterruptedException {
//        // Given
//        storeProcessor.whenAnyExchangeReceived(new Processor() {
//            @Override
//            public void process(Exchange exchange) throws Exception {
//                throw new IllegalArgumentException();
//            }
//        });
//
//        storeProcessor.expectedMessageCount(1);
//        sendProcessor.expectedMessageCount(0);
//        revokeProcessor.expectedMessageCount(0);
//        temporaryErrorHandlerEndpoint.expectedMessageCount(0);
//        permanentErrorHandlerEndpoint.expectedMessageCount(1);
//
//        // When
//        producerTemplate.sendBodyAndHeaders(MESSAGE_BODY, ImmutableMap.<String, Object> of(Constants.MESSAGE_TYPE, Constants.STORE_MESSAGE));
//
//        // Then
//        assertIsSatisfied(storeProcessor);
//        assertIsSatisfied(sendProcessor);
//        assertIsSatisfied(revokeProcessor);
//        assertIsSatisfied(temporaryErrorHandlerEndpoint);
//        assertIsSatisfied(permanentErrorHandlerEndpoint);
//    }
//
//
//    @Configuration
//    public static class ContextConfig extends SingleRouteCamelConfiguration {
//        @Bean
//        public RouteBuilder route() {
//            return new RouteBuilder() {
//                public void configure() {
//                    from("direct:start").filter(header("foo").isEqualTo("bar")).to("mock:result");
//                }
//            };
//        }
//    }

}
