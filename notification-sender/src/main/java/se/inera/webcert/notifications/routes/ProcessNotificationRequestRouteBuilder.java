package se.inera.webcert.notifications.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;

public class ProcessNotificationRequestRouteBuilder extends RouteBuilder {
    
    Namespaces ns = new Namespaces("not", "urn:inera:webcert:notifications:1");
    
    @Override
    public void configure() throws Exception {
        from("ref:notificationRequestEndpoint").routeId("processNotificationRequest")
        .setHeader(ProcessNotificationRequestRouteHeaders.INTYGS_ID, ns.xpath("/not:NotificationRequest/not:intygsId/text()"))
        .setHeader(ProcessNotificationRequestRouteHeaders.INTYGS_TYP, ns.xpath("/not:NotificationRequest/not:intygsTyp/text()"))
        .unmarshal("notificationRequestJaxb")
        .processRef("createAndInitCertificateStatusRequestProcessor")
        .enrichRef("intygRepositoryEndpoint", "enrichWithIntygDataStrategy")
        .to("ref:certificateStatusUpdateEndpoint");

    }
}
