package se.inera.webcert.certificatesender.routes;

import org.apache.camel.LoggingLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import se.inera.webcert.certificatesender.exception.TemporaryException;
import se.inera.webcert.common.Constants;

public class CertificateRouteBuilder extends org.apache.camel.builder.RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(CertificateRouteBuilder.class);

    @Override
    public void configure() throws Exception {
        from("receiveCertificateTransferEndpoint").routeId("transferCertificate")
                .onException(TemporaryException.class).handled(true).to("direct:certTemporaryErrorHandlerEndpoint").end()
                .onException(Exception.class).handled(true).to("direct:certPermanentErrorHandlerEndpoint").end()
                .choice()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.STORE_MESSAGE)).to("bean:certificateStoreProcessor").stop()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.SEND_MESSAGE)).to("bean:certificateSendProcessor").stop()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.REVOKE_MESSAGE)).to("bean:certificateRevokeProcessor").stop();

        from("direct:certPermanentErrorHandlerEndpoint").routeId("permanentErrorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("Permanent exception").getText())
                .stop();

        from("direct:certTemporaryErrorHandlerEndpoint").routeId("temporaryErrorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("Temporary exception").getText())
               // .to("activemq:receiveCertificate")

                .stop();
    }

}
