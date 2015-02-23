package se.inera.webcert.notifications.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import se.inera.webcert.notifications.service.exception.NonRecoverableCertificateStatusUpdateServiceException;

public class ProcessNotificationRequestRouteBuilder extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessNotificationRequestRouteBuilder.class);

    @Value("${errorhanding.maxRedeliveries}")
    private int maxRedeliveries = 3;

    @Value("${errorhanding.redeliveryDelay}")
    private long redeliveryDelay = 10;

    @Override
    public void configure() throws Exception {
        from("receiveNotificationRequestEndpoint").routeId("transformNotification")
                .onException(Exception.class).handled(true).to("direct:errorHandlerEndpoint").end()
                .to("bean:createAndInitCertificateStatusRequestProcessor")
                .to("direct:sendMessageToWS");

        from("direct:sendMessageToWS").routeId("sendNotificationToWS")
                .errorHandler(deadLetterChannel("direct:redeliveryExhaustedEndpoint")
                        .maximumRedeliveries(maxRedeliveries).redeliveryDelay(redeliveryDelay)
                        .useExponentialBackOff())
                .onException(NonRecoverableCertificateStatusUpdateServiceException.class).handled(true).to("direct:errorHandlerEndpoint").end()
                .to("sendCertificateStatusUpdateEndpoint");

        from("direct:errorHandlerEndpoint").routeId("errorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("Un-recoverable exception for intygs-id: ${in.headers.intygsId}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .stop();

        from("direct:redeliveryExhaustedEndpoint").routeId("redeliveryErrorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("Redelivery attempts exhausted for intygs-id: ${in.headers.intygsId}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .to("deadLetterEndpoint")
                .stop();
    }

}
