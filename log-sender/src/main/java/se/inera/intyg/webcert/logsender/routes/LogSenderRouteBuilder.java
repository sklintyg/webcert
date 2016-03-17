/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.logsender.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.processor.aggregate.GroupedExchangeAggregationStrategy;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.common.sender.exception.TemporaryException;

/**
 * Defines the LogSender Camel route which accepts {@link se.inera.intyg.common.logmessages.PdlLogMessage} in JSON-
 * serialized TextMessages.
 *
 * @author eriklupander
 */
public class LogSenderRouteBuilder extends SpringRouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(LogSenderRouteBuilder.class);

    @Value("${logsender.bulkSize}")
    private String batchSize;

    @Value("${receiveAggregatedLogMessageEndpointUri}")
    private String newAggregatedLogMessageQueue;

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

        // 1. Starts by splitting any inbound PdlLogMessage instances having more than one PdlResource into separate
        //    PdlLogMessage instances, one per each PdlResource.
        //    Then the route Aggregates (n) messages together and passes them to a custom bean which will transform the content
        //    into a single list of PdlLogMessage.
        //    The bean:logMessageAggregationProcessor outputs a List of PdlLogMessage which is passed to a JMS queue.
        from("receiveLogMessageEndpoint").routeId("aggregatorRoute")
                .split().method("logMessageSplitProcessor")
                .aggregate(new GroupedExchangeAggregationStrategy())
                .constant(true)
                .completionPredicate(header("CamelAggregatedSize").isEqualTo(Integer.parseInt(batchSize)))
                .to("bean:logMessageAggregationProcessor")
                .to(newAggregatedLogMessageQueue)
                .stop();


        // 2. In a transaction, reads from jms/AggregatedLogSenderQueue and uses custom bean:logMessageProcessor
        //    to convert into ehr:logstore format and send. Exception handling delegates resends to AMQ.
        from("receiveAggregatedLogMessageEndpoint").routeId("aggregatedJmsToSenderRoute")
                .onException(TemporaryException.class).to("direct:logMessageTemporaryErrorHandlerEndpoint").end()
                .onException(Exception.class).handled(true).to("direct:logMessagePermanentErrorHandlerEndpoint").end()
                .transacted()
                .to("bean:logMessageSendProcessor").stop();


        // Error handling
        from("direct:logMessagePermanentErrorHandlerEndpoint").routeId("permanentErrorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("ENTER - Permanent exception for LogMessage: ${header[" + Constants.LOG_ID + "]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .stop();

        from("direct:logMessageTemporaryErrorHandlerEndpoint").routeId("temporaryErrorLogging")
                .choice()
                .when(header(Constants.JMS_REDELIVERED).isEqualTo("false"))
                .log(LoggingLevel.ERROR, LOG, simple("ENTER - Temporary exception for logMessage: ${header[" + Constants.LOG_ID + "]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .otherwise()
                .log(LoggingLevel.WARN, LOG, simple("ENTER - Temporary exception  (redelivered) for logMessage: ${header[" + Constants.LOG_ID + "]}, with message: ${exception.message}").getText())
                .stop();
    }

}
