package se.inera.webcert.certificatesender.routes;

import org.apache.camel.LoggingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import se.inera.webcert.certificatesender.exception.TemporaryException;

public class CertificateRouteBuilder extends org.apache.camel.builder.RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(CertificateRouteBuilder.class);

    private static final String STORE_MESSAGE = "STORE";
    private static final String SEND_MESSAGE = "SEND";
    private static final String REVOKE_MESSAGE = "REVOKE";
    private static final String MESSAGE_TYPE = "MESSAGE_TYPE";

    @Override
    public void configure() throws Exception {
        from("receiveCertificateTransferEndpoint").routeId("transferCertificate")
                .onException(TemporaryException.class).handled(true).to("direct:certTemporaryErrorHandlerEndpoint").end()
                .onException(Exception.class).handled(true).to("direct:certPermanentErrorHandlerEndpoint").end()
                .choice()
                .when(header(MESSAGE_TYPE).isEqualTo(STORE_MESSAGE)).to("bean:certificateStoreProcessor").stop()
                .when(header(MESSAGE_TYPE).isEqualTo(SEND_MESSAGE)).to("bean:certificateSendProcessor").stop()
                .when(header(MESSAGE_TYPE).isEqualTo(REVOKE_MESSAGE)).to("bean:certificateRevokeProcessor").stop();

        from("direct:certPermanentErrorHandlerEndpoint").routeId("permanentErrorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("Permanent exception").getText())
                .stop();

        from("direct:certTemporaryErrorHandlerEndpoint").routeId("temporaryErrorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("Temporary exception").getText())
               // .to("activemq:receiveCertificate")

                .stop();
    }

}
