package se.inera.webcert.notifications.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.exception.TemporaryException;

public class NotificationRouteBuilder extends SpringRouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationRouteBuilder.class);

    @Override
    public void configure() throws Exception {
        from("receiveNotificationRequestEndpoint").routeId("transformNotification")
                .onException(Exception.class).handled(true).to("direct:permanentErrorHandlerEndpoint").end()
                .transacted()
                .unmarshal("notificationMessageDataFormat")
                .to("bean:notificationTransformer")
                .marshal("jaxbMessageDataFormat")
                .to("sendNotificationWSEndpoint");

        from("sendNotificationWSEndpoint").routeId("sendNotificationToWS")
                .errorHandler(transactionErrorHandler().logExhausted(false))
                .onException(TemporaryException.class).to("direct:temporaryErrorHandlerEndpoint").end()
                .onException(Exception.class).handled(true).to("direct:permanentErrorHandlerEndpoint").end()
                .transacted()
                .unmarshal("jaxbMessageDataFormat")
                .to("bean:notificationWSClient");

        from("direct:permanentErrorHandlerEndpoint").routeId("errorLogging")
                .log(LoggingLevel.ERROR, LOG,
                        simple("Permanexception for intygs-id: ${header[intygsId]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .stop();

        from("direct:temporaryErrorHandlerEndpoint").routeId("temporaryErrorLogging")
                .log(LoggingLevel.WARN, LOG,
                        simple("Temporary exception for intygs-id: ${header[intygsId]}, with message: ${exception.message}").getText())
                .stop();
    }

}
