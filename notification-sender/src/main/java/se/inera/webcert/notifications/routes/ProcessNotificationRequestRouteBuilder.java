package se.inera.webcert.notifications.routes;

import org.apache.camel.builder.RouteBuilder;

public class ProcessNotificationRequestRouteBuilder extends RouteBuilder {
        
    @Override
    public void configure() throws Exception {
        from("ref:processNotificationRequestEndpoint").routeId("processNotificationRequest")
        .unmarshal("notificationRequestJaxb")
        .processRef("createAndInitCertificateStatusRequestProcessor")
        .enrichRef("getIntygFromWebcertRepositoryServiceEndpoint", "enrichWithIntygDataStrategy")
        .to("ref:sendCertificateStatusUpdateEndpoint");
    }
}
