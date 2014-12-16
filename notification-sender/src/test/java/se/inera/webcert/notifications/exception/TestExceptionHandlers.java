package se.inera.webcert.notifications.exception;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import se.inera.webcert.notifications.TestDataUtil;
import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.routes.RouteHeaders;
import se.inera.webcert.notifications.service.exception.CertificateStatusUpdateServiceException;
import se.inera.webcert.notifications.service.exception.NonRecoverableCertificateStatusUpdateServiceException;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/spring/test-properties-context.xml", "/spring/beans-context.xml", "/spring/test-service-context.xml",
        "/spring/camel-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles(profiles = "unittest")
public class TestExceptionHandlers {
    
    private static final Logger LOG = LoggerFactory.getLogger(TestExceptionHandlers.class);
    
    // Expect this number of messages
    private static final int EXPECTED_MESSAGE_COUNT = 4;

    @Autowired
    private CamelContext camelContext;

    @Produce(uri = "direct:processNotificationRequestEndpoint")
    private ProducerTemplate processNotificationRequestEndpoint;

    @EndpointInject(uri = "mock:certificateStatusUpdateEndpoint")
    private MockEndpoint mockCertificateStatusUpdateEndpoint;

    private static final String VARDENHET_1_ADDR = "vardenhet-1";

    @Test
    public void testApplicationException() throws InterruptedException {
        mockCertificateStatusUpdateEndpoint.whenAnyExchangeReceived(new Processor() {
            
            private int attempts = 1;
            
            @Override
            public void process(Exchange exchange) throws Exception {
                LOG.debug("Recieving {}", attempts++);
                throw new CertificateStatusUpdateServiceException("Testing application error, with exhausted retries");
            }
        });
        // Check for 4 messages, 1 original and 3 retries
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(EXPECTED_MESSAGE_COUNT);
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");

        Exchange exchange = buildExchange(requestPayload);

        processNotificationRequestEndpoint.send(exchange);
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
    }

    @Test
    public void testTechnicalException() throws InterruptedException {
        mockCertificateStatusUpdateEndpoint.whenAnyExchangeReceived(new Processor() {
            private int attempts = 1;
            @Override
            public void process(Exchange exchange) throws Exception {
                LOG.debug("Recieving {}", attempts++);
                throw new NonRecoverableCertificateStatusUpdateServiceException("Testing technical error");
            }
        });

        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");

        Exchange exchange = buildExchange(requestPayload);

        processNotificationRequestEndpoint.send(exchange);
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
    }
    
    @Test
    public void testWithWrappedIOExceptionShouldCauseResend() throws InterruptedException {
        mockCertificateStatusUpdateEndpoint.whenAnyExchangeReceived(new Processor() {
            private int attempts = 1;
            @Override
            public void process(Exchange exchange) throws Exception {
                LOG.debug("Recieving {}", attempts++);
                IOException ioe = new IOException("This is the IO exception");
                throw new javax.xml.ws.WebServiceException("This is the WebServiceException", ioe);
            }
        });

        mockCertificateStatusUpdateEndpoint.expectedMessageCount(EXPECTED_MESSAGE_COUNT);
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");

        Exchange exchange = buildExchange(requestPayload);

        processNotificationRequestEndpoint.send(exchange);
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
    }

    private Exchange buildExchange(String requestPayload) {
        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_ID, "intyg-2");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_TYP, "fk7263");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_STATUS, IntygsStatus.SIGNED);
        exchange.getIn().setHeader(RouteHeaders.HANDELSE, HandelseType.INTYGSUTKAST_SIGNERAT.toString());
        exchange.getIn().setHeader(RouteHeaders.LOGISK_ADRESS, VARDENHET_1_ADDR);
        return exchange;
    }

    private Exchange wrapRequestInExchange(Object request, CamelContext camelContext) {
        Exchange exchange = new DefaultExchange(camelContext);
        Message inMsg = new DefaultMessage();
        inMsg.setBody(request);
        exchange.setIn(inMsg);
        return exchange;
    }
}
