package se.inera.webcert.notifications.route;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import java.io.IOException;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.webcert.notifications.service.exception.CertificateStatusUpdateServiceException;
import se.inera.webcert.notifications.service.exception.NonRecoverableCertificateStatusUpdateServiceException;

import javax.xml.bind.JAXBException;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/test-notification-sender-config.xml", "/spring/unit-test-properties-context.xml" })
@MockEndpointsAndSkip("bean:createAndInitCertificateStatusRequestProcessor")
@MockEndpoints("(direct:errorHandlerEndpoint|direct:redeliveryExhaustedEndpoint)")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RouteTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(RouteTest.class);

    private static final String INTYG_JSON = "{\"id\":\"1234\",\"typ\":\"fk7263\"}";

    private static final String NOTIFICATION_MESSAGE = "{\"intygsId\":\"1234\",\"intygsTyp\":\"fk7263\",\"logiskAdress\":\"SE12345678-1234\",\"handelseTid\":\"2001-12-31T12:34:56.789\",\"handelse\":\"INTYGSUTKAST_ANDRAT\",\"utkast\":"
            + INTYG_JSON + ",\"fragaSvar\":{\"antalFragor\":0,\"antalSvar\":0,\"antalHanteradeFragor\":0,\"antalHanteradeSvar\":0}}";

    @Autowired
    private CamelContext camelContext;

    @Produce(uri = "direct:receiveNotificationRequestEndpoint")
    private ProducerTemplate processNotificationRequestEndpoint;

    @EndpointInject(uri = "mock:certificateStatusUpdateEndpoint")
    private MockEndpoint mockCertificateStatusUpdateEndpoint;

    @EndpointInject(uri = "mock:bean:createAndInitCertificateStatusRequestProcessor")
    private MockEndpoint mockRequestProcessorEndpoint;

    @EndpointInject(uri = "mock:direct:errorHandlerEndpoint")
    private MockEndpoint mockErrorHandlerEndpoint;

    @EndpointInject(uri = "mock:direct:redeliveryExhaustedEndpoint")
    private MockEndpoint mockRedeliveryEndpoint;

    @Before
    public void setup() {
        camelContext.setTracing(true);
        mockRequestProcessorEndpoint.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                LOG.info("Receiving");
                exchange.getIn().setBody(new CertificateStatusUpdateForCareType());
            }
        });
    }

    @Test
    public void testNormalRoute() throws InterruptedException {
        // Given
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);
        mockErrorHandlerEndpoint.expectedMessageCount(0);
        mockRedeliveryEndpoint.expectedMessageCount(0);

        // When
        processNotificationRequestEndpoint.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
        assertIsSatisfied(mockErrorHandlerEndpoint);
        assertIsSatisfied(mockRedeliveryEndpoint);
    }

    @Test
    public void testTransformationException() throws InterruptedException {
        // Given
        mockRequestProcessorEndpoint.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                LOG.info("Receiving");
                throw new JAXBException("Testing transformation exception");
            }
        });

        mockCertificateStatusUpdateEndpoint.expectedMessageCount(0);
        mockErrorHandlerEndpoint.expectedMessageCount(1);
        mockRedeliveryEndpoint.expectedMessageCount(0);

        // When
        processNotificationRequestEndpoint.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
        assertIsSatisfied(mockErrorHandlerEndpoint);
        assertIsSatisfied(mockRedeliveryEndpoint);
    }

    @Test
    public void testRuntimeException() throws InterruptedException {
        // Given
        mockRequestProcessorEndpoint.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                LOG.info("Receiving {}");
                throw new RuntimeException("Testing runtime exception");
            }
        });

        mockCertificateStatusUpdateEndpoint.expectedMessageCount(0);
        mockErrorHandlerEndpoint.expectedMessageCount(1);
        mockRedeliveryEndpoint.expectedMessageCount(0);

        // When
        processNotificationRequestEndpoint.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
        assertIsSatisfied(mockErrorHandlerEndpoint);
        assertIsSatisfied(mockRedeliveryEndpoint);
    }

    @Test
    public void testWebserviceExceptionWithRedelivery() throws InterruptedException {
        // Given
        mockCertificateStatusUpdateEndpoint.whenAnyExchangeReceived(new Processor() {
            private int attempts = 1;
            @Override
            public void process(Exchange exchange) throws Exception {
                LOG.info("Receiving {}", attempts++);
                throw new CertificateStatusUpdateServiceException("Testing application error, with exhausted retries");
            }
        });

        mockCertificateStatusUpdateEndpoint.expectedMessageCount(4);
        mockErrorHandlerEndpoint.expectedMessageCount(0);
        mockRedeliveryEndpoint.expectedMessageCount(1);

        // When
        processNotificationRequestEndpoint.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
        assertIsSatisfied(mockErrorHandlerEndpoint);
        assertIsSatisfied(mockRedeliveryEndpoint);
    }

    @Test
    public void testTechnicalException() throws InterruptedException {
        // Given
        mockCertificateStatusUpdateEndpoint.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                LOG.debug("Recieving.");
                throw new NonRecoverableCertificateStatusUpdateServiceException("Testing technical error");
            }
        });

        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);
        mockErrorHandlerEndpoint.expectedMessageCount(1);
        mockRedeliveryEndpoint.expectedMessageCount(0);

        // When
        processNotificationRequestEndpoint.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
        assertIsSatisfied(mockErrorHandlerEndpoint);
        assertIsSatisfied(mockRedeliveryEndpoint);
    }
    
    @Test
    public void testWithWrappedIOExceptionShouldCauseResend() throws InterruptedException {
        // Given
        mockCertificateStatusUpdateEndpoint.whenAnyExchangeReceived(new Processor() {
            private int attempts = 1;
            @Override
            public void process(Exchange exchange) throws Exception {
                LOG.debug("Recieving {}", attempts++);
                IOException ioe = new IOException("This is the IO exception");
                throw new javax.xml.ws.WebServiceException("This is the WebServiceException", ioe);
            }
        });

        mockCertificateStatusUpdateEndpoint.expectedMessageCount(4);
        mockErrorHandlerEndpoint.expectedMessageCount(0);
        mockRedeliveryEndpoint.expectedMessageCount(1);

        // When
        processNotificationRequestEndpoint.sendBody(NOTIFICATION_MESSAGE);

        // Then
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
        assertIsSatisfied(mockErrorHandlerEndpoint);
        assertIsSatisfied(mockRedeliveryEndpoint);
    }

}
