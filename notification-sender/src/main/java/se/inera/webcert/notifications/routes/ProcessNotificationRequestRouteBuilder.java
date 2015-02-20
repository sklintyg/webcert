package se.inera.webcert.notifications.routes;

import javax.xml.bind.JAXBException;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import se.inera.webcert.notifications.service.exception.NonRecoverableCertificateStatusUpdateServiceException;

public class ProcessNotificationRequestRouteBuilder extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessNotificationRequestRouteBuilder.class);

    @Value("${errorhanding.maxRedeliveries}")
    private int maxRedeliveries;

    @Value("${errorhanding.redeliveryDelay}")
    private long redeliveryDelay;

    @Override
    public void configure() throws Exception {
        //Setup error handling strategy, using redelivery of 3 secs and then exponentially increasing the time interval
        errorHandler(deadLetterChannel("redeliveryExhaustedEndpoint")
                .maximumRedeliveries(maxRedeliveries).redeliveryDelay(redeliveryDelay).useExponentialBackOff());

        onException(NonRecoverableCertificateStatusUpdateServiceException.class)
        .handled(true)
        .to("errorHandlerEndpoint");

        onException(JAXBException.class)
        .handled(true)
        .to("errorHandlerEndpoint");

        from("ref:receiveNotificationRequestEndpoint").routeId("processNotificationRequest")
                .to("createAndInitCertificateStatusRequestProcessor")
                .to("sendCertificateStatusUpdateEndpoint");

        from("errorHandlerEndpoint").routeId("errorLogging")
            .log(LoggingLevel.ERROR, LOG, simple("Un-recoverable exception for intygs-id: ${in.headers.intygsId}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
            .stop();

        from("redeliveryExhaustedEndpoint").routeId("redeliveryErrorLogging")
            .log(LoggingLevel.ERROR, LOG, simple("Redelivery attempts exhausted for intygs-id: ${in.headers.intygsId}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
            .stop();
    }
}
