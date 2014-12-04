package se.inera.webcert.notifications.routes;

import static org.apache.camel.builder.PredicateBuilder.and;

import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.notifications.process.GetIntygPropertiesProcessor;
import se.inera.webcert.notifications.process.IntygsTypChecker;
import se.inera.webcert.notifications.service.WebcertRepositoryService;

public class NotificationRequestRouter extends RouteBuilder {
    
    @Autowired
    WebcertRepositoryService webcertRepositoryService;
    
    @Autowired
    IntygsTypChecker intygsTypChecker;

    @Autowired
    GetIntygPropertiesProcessor getIntygPropertiesProcessor; 

    private Namespaces ns = new Namespaces("not", "urn:inera:webcert:notifications:1");

    private static final Logger LOG = LoggerFactory.getLogger(NotificationRequestRouter.class); 

    @Override
    public void configure() throws Exception {
        from("ref:receiveNotificationRequestEndpoint").routeId("notificationRequestRouter")
        .setHeader(RouteHeaders.INTYGS_ID, ns.xpath("/not:NotificationRequest/not:intygsId/text()"))
        .setHeader(RouteHeaders.INTYGS_TYP, ns.xpath("/not:NotificationRequest/not:intygsTyp/text()"))
        .setHeader(RouteHeaders.VARDENHET_HSA_ID, ns.xpath("/not:NotificationRequest/not:hoSPerson/not:vardenhet/not:hsaId/text()"))
        .filter(isOfAllowedType())
        .process(getIntygPropertiesProcessor)
        .choice()
            .when(header(RouteHeaders.SAKNAS_I_DB))
                .log(LoggingLevel.DEBUG, LOG, "No intyg found in database, stopping route.")
                .stop()
            .when(ns.xpath("/not:NotificationRequest/not:handelse[text() = 'INTYGSUTKAST_RADERAT']"))
                .setHeader(RouteHeaders.RADERAT, constant("INTYGSUTKAST_RADERAT"))
                .to("ref:processNotificationRequestEndpoint")
            .otherwise()
                //.filter(isFromIntegratedVardenhet())
                .to("ref:processNotificationRequestEndpoint");
    }

    private Predicate isFromIntegratedVardenhet() {
        return method(webcertRepositoryService, "isVardenhetIntegrerad").isEqualTo(Boolean.TRUE);
    }
    
    private Predicate isOfAllowedType() {
        return method(intygsTypChecker).isEqualTo(Boolean.TRUE);
    }
}
