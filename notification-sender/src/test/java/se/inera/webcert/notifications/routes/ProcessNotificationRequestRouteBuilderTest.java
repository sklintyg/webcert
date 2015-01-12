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

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HandelsekodCodeRestrictionType;
import se.inera.webcert.notifications.TestDataUtil;
import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.service.exception.CertificateStatusUpdateServiceException;
import se.inera.webcert.persistence.utkast.model.UtkastStatus;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/spring/test-properties-context.xml", "/spring/beans-context.xml", "/spring/test-service-context.xml",
        "/spring/camel-context.xml" })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles(profiles = "unittest")
public class ProcessNotificationRequestRouteBuilderTest {

    private static final int MESSAGES_EXPECTED = 3;

    @Autowired
    private CamelContext camelContext;

    @Produce(uri = "direct:processNotificationRequestEndpoint")
    private ProducerTemplate processNotificationRequestEndpoint;

    @EndpointInject(uri = "mock:certificateStatusUpdateEndpoint")
    private MockEndpoint mockCertificateStatusUpdateEndpoint;

    private static final Logger LOG = LoggerFactory.getLogger(ProcessNotificationRequestRouteBuilderTest.class);

    private static final String VARDENHET_1_ADDR = "vardenhet-1";

    @Test
    public void testWithSkapat() throws Exception {

        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);

        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-skapat-notification.xml");

        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_ID, "intyg-1");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_TYP, "fk7263");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_STATUS, UtkastStatus.DRAFT_INCOMPLETE);
        exchange.getIn().setHeader(RouteHeaders.HANDELSE, HandelseType.INTYGSUTKAST_SKAPAT.toString());
        exchange.getIn().setHeader(RouteHeaders.LOGISK_ADRESS, VARDENHET_1_ADDR);

        processNotificationRequestEndpoint.send(exchange);

        assertIsSatisfied(camelContext);

        CertificateStatusUpdateForCareType statusUpdateType = mockCertificateStatusUpdateEndpoint.getExchanges().get(0).getIn()
                .getBody(CertificateStatusUpdateForCareType.class);
        assertNotNull(statusUpdateType);
        assertEquals(HandelsekodCodeRestrictionType.HAN_1, statusUpdateType.getUtlatande().getHandelse().getHandelsekod());
        assertNotNull(statusUpdateType.getUtlatande().getFragorOchSvar());
    }

    @Test
    public void testWithSigned() throws Exception {

        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);

        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");

        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_ID, "intyg-2");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_TYP, "fk7263");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_STATUS, UtkastStatus.SIGNED);
        exchange.getIn().setHeader(RouteHeaders.HANDELSE, HandelseType.INTYGSUTKAST_SIGNERAT.toString());
        exchange.getIn().setHeader(RouteHeaders.LOGISK_ADRESS, VARDENHET_1_ADDR);

        processNotificationRequestEndpoint.send(exchange);

        assertIsSatisfied(camelContext);

        CertificateStatusUpdateForCareType statusUpdateType = mockCertificateStatusUpdateEndpoint.getExchanges().get(0).getIn()
                .getBody(CertificateStatusUpdateForCareType.class);
        assertNotNull(statusUpdateType);
        assertEquals(HandelsekodCodeRestrictionType.HAN_2, statusUpdateType.getUtlatande().getHandelse().getHandelsekod());
        assertNotNull(statusUpdateType.getUtlatande().getFragorOchSvar());
        assertEquals(1, statusUpdateType.getUtlatande().getArbetsformaga().size());
    }

    @Test
    public void testWithDeleted() throws InterruptedException {
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);

        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-raderat-notification.xml");

        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_ID, "intyg-4");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_TYP, "fk7263");
        exchange.getIn().setHeader(RouteHeaders.HANDELSE, HandelseType.INTYGSUTKAST_RADERAT.toString());
        exchange.getIn().setHeader(RouteHeaders.LOGISK_ADRESS, VARDENHET_1_ADDR);

        processNotificationRequestEndpoint.send(exchange);

        assertIsSatisfied(camelContext);
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);

        CertificateStatusUpdateForCareType statusUpdateType = mockCertificateStatusUpdateEndpoint.getExchanges().get(0).getIn()
                .getBody(CertificateStatusUpdateForCareType.class);
        assertNotNull(statusUpdateType);
        assertNotNull(statusUpdateType.getUtlatande().getSkapadAv());
        assertEquals(HandelsekodCodeRestrictionType.HAN_4, statusUpdateType.getUtlatande().getHandelse().getHandelsekod());
        assertTrue(statusUpdateType.getUtlatande().getArbetsformaga().isEmpty());
        assertNull(statusUpdateType.getUtlatande().getDiagnos());
        assertNotNull(statusUpdateType.getUtlatande().getFragorOchSvar());
    }

    @Test
    public void testSignedContainsFragaSvarDecorations() throws InterruptedException {
        mockCertificateStatusUpdateEndpoint.expectedHeaderReceived(RouteHeaders.INTYGS_STATUS, UtkastStatus.SIGNED);
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(1);

        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");

        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_ID, "intyg-2");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_TYP, "fk7263");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_STATUS, UtkastStatus.SIGNED);
        exchange.getIn().setHeader(RouteHeaders.HANDELSE, HandelseType.INTYGSUTKAST_SIGNERAT.toString());
        exchange.getIn().setHeader(RouteHeaders.LOGISK_ADRESS, VARDENHET_1_ADDR);

        processNotificationRequestEndpoint.send(exchange);
        assertIsSatisfied(camelContext);
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
        CertificateStatusUpdateForCareType body = mockCertificateStatusUpdateEndpoint.getExchanges().get(0).getIn()
                .getBody(CertificateStatusUpdateForCareType.class);
        assertNotNull(body.getUtlatande().getFragorOchSvar());
        assertEquals(1, body.getUtlatande().getArbetsformaga().size());
    }

    @Test
    public void testException() throws InterruptedException {
        mockCertificateStatusUpdateEndpoint.whenAnyExchangeReceived(new Processor() {
            private int counter = 0;

            @Override
            public void process(Exchange exchange) throws Exception {
                counter++;
                if (counter <= 2) {
                    LOG.debug("{} attempt - throwing exception", counter);
                    throw new CertificateStatusUpdateServiceException("fail!");
                } else if (counter > 2) {
                    LOG.debug("Ok message received on attempt {}", counter);
                }
            }
        });
        mockCertificateStatusUpdateEndpoint.expectedMessageCount(MESSAGES_EXPECTED);

        String requestPayload = TestDataUtil.readRequestFromFile("data/intygsutkast-signerat-notification.xml");

        Exchange exchange = wrapRequestInExchange(requestPayload, camelContext);
        exchange.getIn().setHeader(RouteHeaders.INTYGS_ID, "intyg-2");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_TYP, "fk7263");
        exchange.getIn().setHeader(RouteHeaders.INTYGS_STATUS, UtkastStatus.SIGNED);
        exchange.getIn().setHeader(RouteHeaders.HANDELSE, HandelseType.INTYGSUTKAST_SIGNERAT.toString());
        exchange.getIn().setHeader(RouteHeaders.LOGISK_ADRESS, VARDENHET_1_ADDR);

        processNotificationRequestEndpoint.send(exchange);
        assertIsSatisfied(mockCertificateStatusUpdateEndpoint);
    }

    private Exchange wrapRequestInExchange(Object request, CamelContext camelContext) {

        Exchange exchange = new DefaultExchange(camelContext);
        Message inMsg = new DefaultMessage();
        inMsg.setBody(request);
        exchange.setIn(inMsg);

        return exchange;
    }

}
