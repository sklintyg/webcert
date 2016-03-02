package se.inera.intyg.webcert.logsender.service;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.common.logmessages.AbstractLogMessage;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;

import java.util.ArrayList;
import java.util.List;

/**
 * Accepts a Camel Exchange that must contain a {@link Exchange#GROUPED_EXCHANGE} of (n)
 * log messages that should be sent in a batch to the PDL-log service.
 *
 * The resulting list of {@link AbstractLogMessage} is passed on so Camel can supply it to the next consumer.
 *
 * Created by eriklupander on 2016-02-29.
 */
public class LogMessageAggregationProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LogMessageAggregationProcessor.class);

    /**
     * Transforms the contents of the grouped exchange into a list of {@link AbstractLogMessage}.
     *
     * @param exchange
     *      An exchange typically containing (n) number of exchanges that has been aggregated into a grouped exchange.
     * @return
     *      An ArrayList<AbstractLogMessage>. Note use of ArrayList implementation type due to serialization issues when
     *      passed onto a JMS queue.
     * @throws PermanentException
     *      If the exchange could not be read or did not contain any grouped exchanges, just ignore.
     */
    public ArrayList<AbstractLogMessage> process(Exchange exchange) throws Exception {

        List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);

        if (grouped == null || grouped.size() == 0) {
            LOG.info("No aggregated log messages, this is normal if camel aggregator has a batch timeout. Doing nothing.");
            throw new PermanentException("No aggregated messages, no reason to retry");
        }

        // Note use of concrete type due to serialization issues.
        ArrayList<AbstractLogMessage> logMessageList = new ArrayList<>();

        for (Exchange oneExchange : grouped) {
            Object body = oneExchange.getIn().getBody();
            if (body instanceof AbstractLogMessage) {
                logMessageList.add( (AbstractLogMessage) body);
            } else if (body instanceof ArrayList) {
                logMessageList.addAll( (ArrayList<AbstractLogMessage>) body);
            } else {
                throw new PermanentException("Unknown log payload: " + body.getClass().getName());
            }
        }

        return logMessageList;
    }
}
