package se.inera.webcert.notifications.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.notifications.service.exception.NonRecoverableCertificateStatusUpdateServiceException;

public class ProcessNotificationRequestRouteBuilder extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessNotificationRequestRouteBuilder.class);

    @Override
    public void configure() throws Exception {
        from("receiveNotificationRequestEndpoint").routeId("transformNotification")
                .onException(Exception.class).handled(true).to("direct:errorHandlerEndpoint").end()
                .transacted()
                .unmarshal("notificationMessageDataFormat")
                .to("bean:notificationTransformer")
                .marshal("jaxbMessageDataFormat")
                .to("sendNotificationWSEndpoint");

        from("sendNotificationWSEndpoint").routeId("sendNotificationToWS")
                .errorHandler(noErrorHandler())
                .onException(NonRecoverableCertificateStatusUpdateServiceException.class).handled(true).to("direct:errorHandlerEndpoint").end()
                .transacted()
                .unmarshal("jaxbMessageDataFormat")
                .to("bean:notificationWSClient");

        from("direct:errorHandlerEndpoint").routeId("errorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("Un-recoverable exception for intygs-id: ${in.headers.intygsId}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .stop();
    }

}
