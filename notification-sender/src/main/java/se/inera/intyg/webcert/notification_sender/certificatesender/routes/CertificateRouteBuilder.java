package se.inera.intyg.webcert.notification_sender.certificatesender.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.intyg.webcert.notification_sender.exception.TemporaryException;
import se.inera.intyg.webcert.common.common.Constants;

public class CertificateRouteBuilder extends SpringRouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(CertificateRouteBuilder.class);

   /*
     * This route depends on the MQ provider (currently ActiveMQ) for redelivery. Any temporary exception thrown
     * by any component in this route is NOT handled by the route, but triggers a transaction rollback in the
     * MQ provider. The MQ provider will then, if properly configured, put the message back into the queue after
     * the proper redelivery wait time has passed.
     *
     * Any permanent exception is handled by the route, however, and will NOT trigger a redelivery.
     */

    @Override
    public void configure() throws Exception {
        errorHandler(transactionErrorHandler().logExhausted(false));

        from("receiveCertificateTransferEndpoint").routeId("transferCertificate")
                .onException(TemporaryException.class).to("direct:certTemporaryErrorHandlerEndpoint").end()
                .onException(Exception.class).handled(true).to("direct:certPermanentErrorHandlerEndpoint").end()
                .transacted()
                .choice()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.STORE_MESSAGE)).to("bean:certificateStoreProcessor").stop()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.SEND_MESSAGE)).to("bean:certificateSendProcessor").stop()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.REVOKE_MESSAGE)).to("bean:certificateRevokeProcessor").stop()
                .otherwise().log(LoggingLevel.ERROR, LOG, simple("Unknown message type: ${in.headers.MESSAGE_TYPE}").getText()).stop();

        from("direct:certPermanentErrorHandlerEndpoint").routeId("permanentErrorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("Permanent exception for intygs-id: ${header[JMSXGroupID]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .stop();

        from("direct:certTemporaryErrorHandlerEndpoint").routeId("temporaryErrorLogging")
                .choice()
                .when(header(Constants.JMS_REDELIVERED).isEqualTo("false"))
                .log(LoggingLevel.ERROR, LOG, simple("Temporary exception for intygs-id: ${header[JMSXGroupID]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .otherwise()
                .log(LoggingLevel.WARN, LOG, simple("Temporary exception for intygs-id: ${header[JMSXGroupID]}, with message: ${exception.message}").getText())
                .stop();
    }

}
