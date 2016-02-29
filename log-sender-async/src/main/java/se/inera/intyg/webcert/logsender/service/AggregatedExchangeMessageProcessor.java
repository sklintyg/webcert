package se.inera.intyg.webcert.logsender.service;

import com.google.common.base.Throwables;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import se.inera.intyg.common.logmessages.AbstractLogMessage;

import javax.jms.ObjectMessage;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * Accepts a Camel Exchange that must contain a {@link Exchange#GROUPED_EXCHANGE} of (n)
 * log messages that should be sent in a batch to the PDL-log service.
 *
 * Created by eriklupander on 2016-02-29.
 */
public class AggregatedExchangeMessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(AggregatedExchangeMessageProcessor.class);

    @Autowired
    @Qualifier("newAggregatedLogMessageQueue")
    private Queue aggregatedSendQueue;

    @Autowired
    private JmsTemplate jmsTemplate;


    public void process(Exchange exchange) throws Exception {

        List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);

        if (grouped == null || grouped.size() == 0) {
            LOG.info("No aggregated log messages, this is normal if camel aggregator has a batch timeout. Doing nothing.");
            return;
        }

        // Note use of concrete type due to serialization issues.
        ArrayList<AbstractLogMessage> logMessageList = new ArrayList<>();

        for (Exchange oneExchange : grouped) {
            logMessageList.addAll( (ArrayList<AbstractLogMessage>) oneExchange.getIn().getBody());
        }

        // TODO consider transforming messages into JSON or XML so it becomes readable and
        // more safe to serialize.

        // Pass the finalized "grouped" set of messages to the final send queue.
        // We do this over JMS so this last leg becomes transactional and can use AMQ resend.
        sendMessage(logMessageList);
    }

    private void sendMessage(final ArrayList<AbstractLogMessage> logMessageList) throws Exception {
        jmsTemplate.send(aggregatedSendQueue, session -> {
            try {
                ObjectMessage objectMessage = session.createObjectMessage(logMessageList);
                return objectMessage;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }
}
