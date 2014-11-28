package se.inera.webcert.notifications.routes;

import static org.apache.camel.builder.PredicateBuilder.and;

import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;

public class NotificationRequestRouter extends RouteBuilder {
    
    private Namespaces ns = new Namespaces("not", "urn:inera:webcert:notifications:1");
    
    private Predicate isPresentInDb = method("webcertRepositoryService", "isIntygsUtkastPresent").isEqualTo(Boolean.TRUE);
    
    private Predicate isOfAllowedType = method("intygsTypChecker").isEqualTo(Boolean.TRUE);
    
    private Predicate isFromIntegreradVardenhet = method("webcertRepositoryService", "isVardenhetIntegrerad").isEqualTo(Boolean.TRUE);
    
    private Predicate filerPredicate = and(isPresentInDb, isOfAllowedType, isFromIntegreradVardenhet);

    @Override
    public void configure() throws Exception {
        from("ref:receiveNotificationRequestEndpoint").routeId("notificationRequestRouter")
        .setHeader(RouteHeaders.INTYGS_ID, ns.xpath("/not:NotificationRequest/not:intygsId/text()"))
        .setHeader(RouteHeaders.INTYGS_TYP, ns.xpath("/not:NotificationRequest/not:intygsTyp/text()"))
        .setHeader(RouteHeaders.VARDENHET_HSA_ID, ns.xpath("/not:NotificationRequest/not:hoSPerson/not:vardenhet/not:hsaId/text()"))
        .filter(filerPredicate)
        .to("ref:processNotificationRequestEndpoint");

    }

}
