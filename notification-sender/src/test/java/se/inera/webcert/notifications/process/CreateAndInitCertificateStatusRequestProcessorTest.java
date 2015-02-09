package se.inera.webcert.notifications.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.ValueBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.types.v1.HandelsekodKodRestriktion;
import se.inera.webcert.notifications.message.v1.HandelseType;
import se.inera.webcert.notifications.message.v1.HoSPersonType;
import se.inera.webcert.notifications.message.v1.NotificationRequestType;
import se.inera.webcert.notifications.message.v1.VardenhetType;
import se.inera.webcert.notifications.routes.RouteHeaders;

public class CreateAndInitCertificateStatusRequestProcessorTest extends CamelTestSupport {

    @Produce(uri = "direct:start")
    protected ProducerTemplate producer;
    
    @EndpointInject(uri = "mock:end")
    protected MockEndpoint end;
    
    @Test
    public void testSend() throws InterruptedException {
        NotificationRequestType body = createNotificationRequest(HandelseType.INTYGSUTKAST_SKAPAT);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put(RouteHeaders.INTYGS_ID, "intyg-1");
        
        end.expectedMessageCount(1);
        
        producer.sendBodyAndHeaders(body, headers);
        
        end.assertIsSatisfied();
        
        List<Exchange> exchanges = end.getExchanges();
        
        CertificateStatusUpdateForCareType statusType = exchanges.get(0).getIn().getBody(CertificateStatusUpdateForCareType.class);
        assertNotNull(statusType.getUtlatande());
        assertNotNull(statusType.getUtlatande().getUtlatandeId());
        assertEquals("intyg-1", statusType.getUtlatande().getUtlatandeId().getExtension());
        assertEquals("FK7263", statusType.getUtlatande().getTypAvUtlatande().getCode());
        
        assertNull(statusType.getUtlatande().getSkapadAv());
        
        assertNotNull(statusType.getUtlatande().getHandelse());
        assertNotNull(statusType.getUtlatande().getHandelse().getHandelsekod());
        assertNotNull(statusType.getUtlatande().getHandelse().getHandelsetidpunkt());
        
        assertNotNull(statusType.getUtlatande().getHandelse().getHandelsekod().getCodeSystem());
        assertNotNull(statusType.getUtlatande().getHandelse().getHandelsekod().getCodeSystemName());
        assertEquals(HandelsekodKodRestriktion.HAN_1.value(), statusType.getUtlatande().getHandelse().getHandelsekod().getCode());
        assertEquals(HandelseType.INTYGSUTKAST_SKAPAT.toString(), statusType.getUtlatande().getHandelse().getHandelsekod().getDisplayName());
        
    }
    
    @Test
    public void testSend2() throws InterruptedException {
        NotificationRequestType body = createNotificationRequest(HandelseType.INTYGSUTKAST_RADERAT);
        
        Map<String, Object> headers = new HashMap<>();
        headers.put(RouteHeaders.INTYGS_ID, "intyg-1");
        
        end.expectedMessageCount(1);
        
        producer.sendBodyAndHeaders(body, headers);
        
        end.assertIsSatisfied();
        
        List<Exchange> exchanges = end.getExchanges();
        
        CertificateStatusUpdateForCareType statusType = exchanges.get(0).getIn().getBody(CertificateStatusUpdateForCareType.class);
        assertNotNull(statusType.getUtlatande());
        
        assertNotNull(statusType.getUtlatande().getSkapadAv());
        assertNotNull(statusType.getUtlatande().getSkapadAv().getFullstandigtNamn());
        assertEquals("person-1", statusType.getUtlatande().getSkapadAv().getPersonalId().getExtension());
        
        assertNotNull(statusType.getUtlatande().getSkapadAv().getEnhet());
        assertEquals("vardenhet-1", statusType.getUtlatande().getSkapadAv().getEnhet().getEnhetsId().getExtension());
        
        assertEquals(HandelsekodKodRestriktion.HAN_4.value(), statusType.getUtlatande().getHandelse().getHandelsekod().getCode());
        assertEquals(HandelseType.INTYGSUTKAST_RADERAT.toString(), statusType.getUtlatande().getHandelse().getHandelsekod().getDisplayName());
        
    }

    private NotificationRequestType createNotificationRequest(HandelseType handelseType) {
        NotificationRequestType body = new NotificationRequestType();
        body.setHandelse(handelseType);
        body.setIntygsId("intyg-1");
        body.setIntygsTyp("fk7263");
        body.setHandelseTidpunkt(LocalDateTime.now());
        body.setUtfardandeEnhetsId("enhet-1");
        HoSPersonType hoSPerson = createHoSPerson();
        body.setHoSPerson(hoSPerson);
        return body;
    }

    private HoSPersonType createHoSPerson() {
        HoSPersonType hoSPerson = new HoSPersonType();
        hoSPerson.setFullstandigtNamn("A A");
        hoSPerson.setHsaId("person-1");
        VardenhetType vardenhet = new VardenhetType();
        vardenhet.setEnhetsNamn("Vårdenheten");
        vardenhet.setHsaId("vardenhet-1");
        hoSPerson.setVardenhet(vardenhet);
        return hoSPerson;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        
        return new RouteBuilder() {
            
            @Override
            public void configure() throws Exception {
                from("direct:start").process(new CreateAndInitCertificateStatusRequestProcessor()).to("mock:end");
            }
        };
    }
}
