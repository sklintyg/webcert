package se.inera.webcert.notifications.exception;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

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
@ContextConfiguration({"/spring/test-properties-context.xml", "/spring/beans-context.xml", "/spring/test-service-context.xml", "/spring/camel-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles( profiles = "unittest" )
public class TestExceptionHandlers {
    
    @Autowired
    CamelContext camelContext;

    @Produce(uri = "direct:processNotificationRequestEndpoint")
    ProducerTemplate processNotificationRequestEndpoint;
    
    @EndpointInject(uri = "mock:certificateStatusUpdateEndpoint")
    MockEndpoint mockCertificateStatusUpdateEndpoint;
    
    private static final String VARDENHET_1_ADDR = "vardenhet-1";
    
    @Test
    public void testApplicationException() throws InterruptedException {
        mockCertificateStatusUpdateEndpoint.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                    throw new CertificateStatusUpdateServiceException("Testing application error, with exhausted retries");
            }
        });
        // Check for 7 messages, 1 original and 6 retries
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(7);
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");
        
        Exchange exchange = buildExchange(requestPayload);

        processNotificationRequestEndpoint.send(exchange);
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
    }

    @Test
    public void testTechnicalException() throws InterruptedException {
        mockCertificateStatusUpdateEndpoint.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                    throw new NonRecoverableCertificateStatusUpdateServiceException("Testing technical error");
            }
        });

        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);
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
