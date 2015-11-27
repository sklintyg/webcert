package se.inera.intyg.webcert.notification_sender.notifications.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.notification_sender.exception.TemporaryException;

public class NotificationRouteBuilder extends SpringRouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationRouteBuilder.class);

    /*
     * The second half of this route, sendNotificationToWS, is supposed to be used with redelivery. The
     * route depends on the MQ provider (currently ActiveMQ) for redelivery. Any temporary exception thrown
     * by any component in this route is NOT handled by the route, but triggers a transaction rollback in the
     * MQ provider. The MQ provider will then, if properly configured, put the message back into the queue after
     * the proper redelivery wait time has passed.
     *
     * Any permanent exception is handled by the route, however, and will NOT trigger a redelivery.
     */

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
                .log(LoggingLevel.ERROR, LOG, simple("Permanent exception for intygs-id: ${header[intygsId]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .stop();

        from("direct:temporaryErrorHandlerEndpoint").routeId("temporaryErrorLogging")
                .choice()
                .when(header(Constants.JMS_REDELIVERED).isEqualTo("false"))
                .log(LoggingLevel.ERROR, LOG, simple("Temporary exception for intygs-id: ${header[intygsId]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .otherwise()
                .log(LoggingLevel.WARN, LOG, simple("Temporary exception for intygs-id: ${header[intygsId]}, with message: ${exception.message}").getText())
                .stop();
    }

}
