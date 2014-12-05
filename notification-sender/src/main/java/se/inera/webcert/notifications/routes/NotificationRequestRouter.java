package se.inera.webcert.notifications.routes;

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
    
    private static final Logger LOG = LoggerFactory.getLogger(NotificationRequestRouter.class);
    
    @Autowired
    private WebcertRepositoryService webcertRepositoryService;
    
    @Autowired
    private IntygsTypChecker intygsTypChecker;

    @Autowired
    private GetIntygPropertiesProcessor getIntygPropertiesProcessor;

    private Namespaces ns = new Namespaces("not", "urn:inera:webcert:notifications:1");

    @Override
    public void configure() throws Exception {
        from("ref:receiveNotificationRequestEndpoint").routeId("notificationRequestRouter")
        .setHeader(RouteHeaders.INTYGS_ID, ns.xpath("/not:NotificationRequest/not:intygsId/text()"))
        .setHeader(RouteHeaders.INTYGS_TYP, ns.xpath("/not:NotificationRequest/not:intygsTyp/text()"))
        .setHeader(RouteHeaders.HANDELSE, ns.xpath("/not:NotificationRequest/not:handelse/text()"))
        .setHeader(RouteHeaders.LOGISK_ADRESS, ns.xpath("/not:NotificationRequest/not:utfarandeEnhetsId/text()"))
        .filter(isOfAllowedType())
        .process(getIntygPropertiesProcessor)
        .choice()
            .when(header(RouteHeaders.SAKNAS_I_DB))
                .log(LoggingLevel.DEBUG, LOG, "Intyg was not found in database, stopping route.")
                .stop()
            .when(header(RouteHeaders.INTEGRERAD_ENHET).isEqualTo(Boolean.FALSE))
                .log(LoggingLevel.DEBUG, LOG, "Intyg does not belong to an integrated unit, stopping route.")
                .stop()
            .otherwise()
                .to("ref:processNotificationRequestEndpoint");
    }
    
    private Predicate isOfAllowedType() {
        return method(intygsTypChecker).isEqualTo(Boolean.TRUE);
    }
}
