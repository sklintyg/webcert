package se.inera.webcert.notifications.routes;

import static org.apache.camel.builder.PredicateBuilder.and;

import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.webcert.notifications.process.IntygsTypChecker;
import se.inera.webcert.notifications.service.WebcertRepositoryService;

public class NotificationRequestRouter extends RouteBuilder {
    
    @Autowired
    WebcertRepositoryService webcertRepositoryService;
    
    @Autowired
    IntygsTypChecker intygsTypChecker;
    
    private Namespaces ns = new Namespaces("not", "urn:inera:webcert:notifications:1");

    @Override
    public void configure() throws Exception {
        from("ref:receiveNotificationRequestEndpoint").routeId("notificationRequestRouter")
        .setHeader(RouteHeaders.INTYGS_ID, ns.xpath("/not:NotificationRequest/not:intygsId/text()"))
        .setHeader(RouteHeaders.INTYGS_TYP, ns.xpath("/not:NotificationRequest/not:intygsTyp/text()"))
        .setHeader(RouteHeaders.VARDENHET_HSA_ID, ns.xpath("/not:NotificationRequest/not:hoSPerson/not:vardenhet/not:hsaId/text()"))
        .choice()
            .when(ns.xpath("/not:NotificationRequest/not:handelse[text() = 'INTYGSUTKAST_RADERAT']"))
                .setHeader(RouteHeaders.RADERAT, constant("INTYGSUTKAST_RADERAT"))
                .to("ref:processNotificationRequestEndpoint")
            .otherwise()
                .filter(filterPredicate())
                .to("ref:processNotificationRequestEndpoint");
    }

    private Predicate filterPredicate() {

        Predicate isPresentInDb = method(webcertRepositoryService, "isIntygsUtkastPresent").isEqualTo(Boolean.TRUE);

        Predicate isOfAllowedType = method(intygsTypChecker).isEqualTo(Boolean.TRUE);

        Predicate isFromIntegreradVardenhet = method(webcertRepositoryService, "isVardenhetIntegrerad").isEqualTo(Boolean.TRUE);

        return and(isPresentInDb, isOfAllowedType, isFromIntegreradVardenhet);
    }
}
