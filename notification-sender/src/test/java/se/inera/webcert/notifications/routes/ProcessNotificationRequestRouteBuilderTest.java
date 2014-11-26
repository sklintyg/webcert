package se.inera.webcert.notifications.routes;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import se.inera.webcert.persistence.intyg.model.Intyg;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring/test-properties-context.xml", "/spring/beans-context.xml", "/spring/camel-context.xml"})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ProcessNotificationRequestRouteBuilderTest {

    @Autowired
    CamelContext camelContext;

    @Produce(uri = "direct:notificationRequestEndpoint")
    ProducerTemplate notificationRequestEndpoint;
    
    @EndpointInject(uri = "mock:certificateStatusUpdateEndpoint")
    MockEndpoint mockCertificateStatusUpdateEndpoint;
    
    @EndpointInject(uri = "mock:mockIntygRepositoryEndpoint")
    MockEndpoint mockIntygRepositoryFacadeEndpoint;
    
    @Test
    public void test() throws Exception {
        
        mockIntygRepositoryFacadeEndpoint.whenAnyExchangeReceived(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                Intyg intygsUtkast = createMockIntygsUtkast();
                exchange.getIn().setBody(intygsUtkast);
            }
        });
        
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);
        
        String requestPayload = readRequestFromFile("data/intygsutkast-skapat-notification.xml");
        
        notificationRequestEndpoint.sendBody(requestPayload);
        
        assertIsSatisfied(camelContext);
    }
 
    static Intyg createMockIntygsUtkast() {
        Intyg intyg = new Intyg();
        intyg.setIntygsId("abcd1234");
        intyg.setIntygsTyp("fk7263");
        return intyg;
    }

    private String readRequestFromFile(String filePath) {
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            return IOUtils.toString(resource.getInputStream(), "UTF-8");
        } catch (IOException e) {
            return null;
        }   
    }
}
