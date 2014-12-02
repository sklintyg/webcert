package se.inera.webcert.notifications.routes;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import se.inera.webcert.notifications.TestDataUtil;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring/test-properties-context.xml", "/spring/beans-context.xml", "/spring/test-service-context.xml", "/spring/camel-context.xml"})
@MockEndpointsAndSkip("direct:processNotificationRequestEndpoint")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class NotificationRequestRouterTest {
    
    @Autowired
    CamelContext camelContext;
    
    @Autowired
    ApplicationContext applicationContext;

    @Produce(uri = "direct:recieveNotificationRequestEndpoint")
    ProducerTemplate recieveNotificationRequestEndpoint;
    
    @EndpointInject(uri = "mock:direct:processNotificationRequestEndpoint")
    MockEndpoint mockProcessNotificationRequestEndpoint;
    
    @Test
    public void testRouter() throws Exception {
        
        mockProcessNotificationRequestEndpoint.expectedMessageCount(1);
        mockProcessNotificationRequestEndpoint.expectedHeaderReceived(RouteHeaders.INTYGS_ID, "intyg-1");
        mockProcessNotificationRequestEndpoint.expectedHeaderReceived(RouteHeaders.INTYGS_TYP, "fk7263");
        mockProcessNotificationRequestEndpoint.expectedHeaderReceived(RouteHeaders.VARDENHET_HSA_ID, "vardenhet-1");
                
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-skapat-notification.xml");
        
        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        
        recieveNotificationRequestEndpoint.send(exchange);
        
        assertIsSatisfied(mockProcessNotificationRequestEndpoint);
        
    }

    @Test
    public void testDeletedMessage() throws Exception {
        
        mockProcessNotificationRequestEndpoint.expectedMessageCount(1);
        mockProcessNotificationRequestEndpoint.expectedHeaderReceived(RouteHeaders.INTYGS_ID, "intyg-1");
        mockProcessNotificationRequestEndpoint.expectedHeaderReceived(RouteHeaders.INTYGS_TYP, "fk7263");
        mockProcessNotificationRequestEndpoint.expectedHeaderReceived(RouteHeaders.VARDENHET_HSA_ID, "vardenhet-1");
        mockProcessNotificationRequestEndpoint.expectedHeaderReceived(RouteHeaders.RADERAT, "INTYGSUTKAST_RADERAT");
        
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-raderat-notification.xml");
        
        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        
        recieveNotificationRequestEndpoint.send(exchange);
        
        assertIsSatisfied(mockProcessNotificationRequestEndpoint);
        
    }
    
    protected Exchange wrapRequestInExchange(Object request, CamelContext camelContext) {

        Exchange exchange = new DefaultExchange(camelContext);
        Message inMsg = new DefaultMessage();
        inMsg.setBody(request);
        exchange.setIn(inMsg);

        return exchange;
    }
}
