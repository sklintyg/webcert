package se.inera.webcert.notifications.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.util.toolbox.AggregationStrategies;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.notifications.process.EnrichWithIntygDataStrategy;
import se.inera.webcert.notifications.process.EnrichWithIntygModelDataStrategy;

public class ProcessNotificationRequestRouteBuilder extends RouteBuilder {
        
    @Autowired
    private EnrichWithIntygModelDataStrategy intygModelEnricher;
    
    @Autowired
    private EnrichWithIntygDataStrategy intygPropertiesEnricher;
    
    @Override
    public void configure() throws Exception {
        from("ref:processNotificationRequestEndpoint").routeId("processNotificationRequest")
        .unmarshal("notificationRequestJaxb")
        .processRef("createAndInitCertificateStatusRequestProcessor")
        .enrich("getIntygFromWebcertRepositoryServiceEndpoint", AggregationStrategies.bean(intygPropertiesEnricher, "enrichWithIntygProperties"))
        .enrich("getIntygModelFromWebcertRepositoryServiceEndpoint", AggregationStrategies.bean(intygModelEnricher, "enrichWithArbetsformagorAndDiagnos"))
        .to("ref:sendCertificateStatusUpdateEndpoint");
    }
}
