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

import se.inera.intyg.common.logmessages.type.LogMessageConstants;
import se.inera.intyg.common.logmessages.type.LogMessageType;
import se.inera.intyg.webcert.common.common.Constants;
import se.inera.intyg.webcert.logsender.exception.TemporaryException;

public class LogSenderRouteBuilder extends SpringRouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(LogSenderRouteBuilder.class);

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

        // 1. Takes individual JMS messages and passes them to the aggregator route
        //    if type is the expected one.
        from("receiveLogMessageEndpoint").routeId("transferLogMessage")
                .choice()
                .when(header(LogMessageConstants.LOG_TYPE).isEqualTo(LogMessageType.SINGLE.name()))
                // Pass to new direct:aggregate due to when() expression not scoping aggregation expressions properly
                // if directly inlined. Should be possible to fix...
                .to("direct:aggregate").stop()
                .otherwise().log(LoggingLevel.ERROR, LOG, simple("Unknown message type: ${in.headers.MESSAGE_TYPE}").getText()).stop();

        // 2. Aggregates 5 messages together and passes them to a custom bean which will transform the content
        //    into a single list of AbstractLogMessage. Of course, the "5" will become parameterized.
        //    The bean uses a JmsTemplate and writes to the JMS queue which the aggregatedJmsToProcessorRoute is
        //    listening to.
        from("direct:aggregate").routeId("aggregatorRoute")
                .aggregate(new GroupedExchangeAggregationStrategy())
                .constant(true)
                .completionPredicate(header("CamelAggregatedSize").isEqualTo(5))
                // Pass to Java Bean that transforms the GroupedExchange into something
                // writable on JMS.
                .to("bean:aggregatedExchangeMessageProcessor").stop();

        // 3. In a transaction, reads from jms/AggregatedLogSenderQueue and uses custom bean:logMessageProcessor
        //    to convert into ehr:logstore format and send. Exception handling delegates resends to AMQ.
        from("receiveAggregatedLogMessageEndpoint").routeId("aggregatedJmsToProcessorRoute")
                .onException(TemporaryException.class).to("direct:logMessageTemporaryErrorHandlerEndpoint").end()
                .onException(Exception.class).handled(true).to("direct:logMessagePermanentErrorHandlerEndpoint").end()
                .transacted()
                .to("bean:logMessageProcessor").stop();


        from("direct:logMessagePermanentErrorHandlerEndpoint").routeId("permanentErrorLogging")
                .log(LoggingLevel.ERROR, LOG, simple("ENTER - Permanent exception for LogMessage: ${header[JMSXGroupID]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .stop();

        from("direct:logMessageTemporaryErrorHandlerEndpoint").routeId("temporaryErrorLogging")
                .choice()
                .when(header(Constants.JMS_REDELIVERED).isEqualTo("false"))
                .log(LoggingLevel.ERROR, LOG, simple("ENTER - Temporary exception (with redelivered == FALSE) for logMessage: ${header[JMSXGroupID]}, with message: ${exception.message}\n ${exception.stacktrace}").getText())
                .otherwise()
                .log(LoggingLevel.WARN, LOG, simple("ENTER - Temporary exception  (with redelivered == TRUE) for logMessage: ${header[JMSXGroupID]}, with message: ${exception.message}").getText())
                .stop();
    }

}
