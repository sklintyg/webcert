/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.notification_sender.certificatesender.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import se.inera.intyg.webcert.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;

public class CertificateRouteBuilder extends SpringRouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(CertificateRouteBuilder.class);

    @Value("${camel.message.delay.millis}")
    private String messageDelayMillis;

    /*
     * This route depends on the MQ provider (currently ActiveMQ) for redelivery. Any temporary exception thrown
     * by any component in this route is NOT handled by the route, but triggers a transaction rollback in the
     * MQ provider. The MQ provider will then, if properly configured, put the message back into the queue after
     * the proper redelivery wait time has passed.
     *
     * Any permanent exception is handled by the route, however, and will NOT trigger a redelivery.
     *
     * A message may supply a DELAY_MESSAGE header, which will delay processing of that message for
     * ${camel.message.delay.millis}
     * milliseconds.
     */
    @Override
    public void configure() {
        long messageDelay = 0;
        try {
            messageDelay = Long.parseLong(messageDelayMillis);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Cannot build certificate route, supplied message delay could not be parsed: " + e.getMessage());
        }
        errorHandler(transactionErrorHandler().logExhausted(false));

        from("receiveCertificateTransferEndpoint").routeId("transferCertificate")
                .onException(TemporaryException.class).to("direct:certTemporaryErrorHandlerEndpoint").end()
                .onException(Exception.class).handled(true).to("direct:certPermanentErrorHandlerEndpoint").end()
                .transacted("txTemplate")
                .choice()
                .when(header(Constants.DELAY_MESSAGE)).delay(messageDelay).asyncDelayed().endChoice()
                .end()
                .choice()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.STORE_MESSAGE)).to("bean:certificateStoreProcessor").stop()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.SEND_MESSAGE)).to("bean:certificateSendProcessor").stop()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.REVOKE_MESSAGE)).to("bean:certificateRevokeProcessor").stop()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.SEND_MESSAGE_TO_RECIPIENT))
                    .to("bean:sendMessageToRecipientProcessor").stop()
                .when(header(Constants.MESSAGE_TYPE).isEqualTo(Constants.REGISTER_APPROVED_RECEIVERS_MESSAGE))
                    .to("bean:registerApprovedReceiversProcessor").stop()

                .otherwise()
                .log(LoggingLevel.ERROR, LOG, simple("Unknown message type: ${in.headers.MESSAGE_TYPE}").getText()).stop();

        from("direct:certPermanentErrorHandlerEndpoint").routeId("permanentErrorLogging")
                .log(LoggingLevel.ERROR, LOG,
                        simple("Permanent exception for intygs-id: ${header[JMSXGroupID]}, "
                                + "with message: ${exception.message}\n ${exception.stacktrace}")
                                        .getText())
                .stop();

        from("direct:certTemporaryErrorHandlerEndpoint").routeId("temporaryErrorLogging")
                .choice()
                .when(header(Constants.JMS_REDELIVERED).isEqualTo("false"))
                .log(LoggingLevel.ERROR, LOG,
                        simple("Temporary exception for intygs-id: ${header[JMSXGroupID]}, with message: "
                                + "${exception.message}\n ${exception.stacktrace}")
                                        .getText())
                .otherwise()
                .log(LoggingLevel.WARN, LOG,
                        simple("Temporary exception for intygs-id: ${header[JMSXGroupID]}, with message: "
                                + "${exception.message}").getText())
                .stop();
    }

}
