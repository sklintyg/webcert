package se.inera.webcert.notifications.routes;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.webcert.notifications.TestDataUtil;
import se.inera.webcert.persistence.intyg.model.Intyg;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring/test-properties-context.xml", "/spring/beans-context.xml", "/spring/test-beans-context.xml", "/spring/camel-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ProcessNotificationRequestRouteBuilderTest {

    @Autowired
    CamelContext camelContext;

    @Produce(uri = "direct:processNotificationRequestEndpoint")
    ProducerTemplate processNotificationRequestEndpoint;
    
    @EndpointInject(uri = "mock:certificateStatusUpdateEndpoint")
    MockEndpoint mockCertificateStatusUpdateEndpoint;
        
    @Test
    public void testWithSkapat() throws Exception {
        
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);
        
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-skapat-notification.xml");
        
        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_ID, "intyg-1");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_TYP, "fk7263");
        exchange.getIn().setHeader(RouteHeaders.VARDENHET_HSA_ID, "vardenhet-1");
        
        processNotificationRequestEndpoint.send(exchange);
        
        assertIsSatisfied(camelContext);
        
    }
    
    @Test
    public void testWithSigned() throws Exception {
        
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);
        
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");
        
        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_ID, "intyg-2");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_TYP, "fk7263");
        exchange.getIn().setHeader(RouteHeaders.VARDENHET_HSA_ID, "vardenhet-1");
        
        processNotificationRequestEndpoint.send(exchange);
        
        assertIsSatisfied(camelContext);
        
    }
    
    private Exchange wrapRequestInExchange(Object request, CamelContext camelContext) {

        Exchange exchange = new DefaultExchange(camelContext);
        Message inMsg = new DefaultMessage();
        inMsg.setBody(request);
        exchange.setIn(inMsg);

        return exchange;
    }
    
}
