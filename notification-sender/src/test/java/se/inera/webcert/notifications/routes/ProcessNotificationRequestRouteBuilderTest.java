package se.inera.webcert.notifications.routes;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.HandelseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HandelsekodCodeRestrictionType;
import se.inera.webcert.notifications.TestDataUtil;
import se.inera.webcert.persistence.intyg.model.IntygsStatus;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({"/spring/test-properties-context.xml", "/spring/beans-context.xml", "/spring/test-service-context.xml", "/spring/camel-context.xml"})
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
        
        CertificateStatusUpdateForCareType statusUpdateType = mockCertificateStatusUpdateEndpoint.getExchanges().get(0).getIn().getBody(CertificateStatusUpdateForCareType.class);
        assertNotNull(statusUpdateType);
        assertEquals(HandelsekodCodeRestrictionType.HAN_1, statusUpdateType.getUtlatande().getHandelse().getHandelsekod());
        assertNull(statusUpdateType.getUtlatande().getFragorOchSvar());
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
    
    @Test
    public void testWithDeleted() throws InterruptedException {
        mockCertificateStatusUpdateEndpoint.expectedHeaderReceived(RouteHeaders.INTYGS_STATUS, IntygsStatus.SIGNED);
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);
        
        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-raderat-notification.xml");
        
        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_ID, "intyg-2");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_TYP, "fk7263");
        exchange.getIn().setHeader(RouteHeaders.VARDENHET_HSA_ID, "vardenhet-1");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_STATUS, "SIGNED");
        exchange.getIn().setHeader(RouteHeaders.RADERAT, "INTYGSUTKAST_RADERAT");

        processNotificationRequestEndpoint.send(exchange);
        assertIsSatisfied(camelContext);
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
    }

    @Test
    public void testSignedContainsFragaSvarDecorations() throws InterruptedException {
        mockCertificateStatusUpdateEndpoint.expectedHeaderReceived(RouteHeaders.INTYGS_STATUS, IntygsStatus.SIGNED);
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);

        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");
        
        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_ID, "intyg-2");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_TYP, "fk7263");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_STATUS, "SIGNED");
        exchange.getIn().setHeader(RouteHeaders.VARDENHET_HSA_ID, "vardenhet-1");

        processNotificationRequestEndpoint.send(exchange);
        assertIsSatisfied(camelContext);
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
        CertificateStatusUpdateForCareType body = mockCertificateStatusUpdateEndpoint.getExchanges().get(0).getIn().getBody(CertificateStatusUpdateForCareType.class);
        assertTrue(body.getUtlatande().getFragorOchSvar() != null);
    }

    private Exchange wrapRequestInExchange(Object request, CamelContext camelContext) {

        Exchange exchange = new DefaultExchange(camelContext);
        Message inMsg = new DefaultMessage();
        inMsg.setBody(request);
        exchange.setIn(inMsg);

        return exchange;
    }
    
}
