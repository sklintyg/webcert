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
package se.inera.intyg.webcert.logsender.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.intyg.common.logmessages.PdlLogMessage;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;

import java.util.ArrayList;
import java.util.List;

/**
 * Accepts a Camel Exchange that must contain a {@link Exchange#GROUPED_EXCHANGE} of (n)
 * log messages that should be sent in a batch to the PDL-log service.
 *
 * The resulting list of {@link PdlLogMessage} is passed on so Camel can supply it to the next consumer.
 *
 * Created by eriklupander on 2016-02-29.
 */
public class LogMessageAggregationProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LogMessageAggregationProcessor.class);

    private ObjectMapper objectMapper = new CustomObjectMapper();

    /**
     * Transforms the contents of the grouped exchange into a list of {@link PdlLogMessage}.
     *
     * @param exchange
     *      An exchange typically containing (n) number of exchanges that has been aggregated into a grouped exchange.
     * @return
     *      An ArrayList<PdlLogMessage>. Note use of ArrayList implementation type due to serialization issues when
     *      passed onto a JMS queue.
     * @throws PermanentException
     *      If the exchange could not be read or did not contain any grouped exchanges, just ignore.
     */
    public ArrayList<String> process(Exchange exchange) throws Exception {

        List<Exchange> grouped = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);

        if (grouped == null || grouped.size() == 0) {
            LOG.info("No aggregated log messages, this is normal if camel aggregator has a batch timeout. Doing nothing.");
            throw new PermanentException("No aggregated messages, no reason to retry");
        }

        // Note use of concrete type due to serialization issues.
        ArrayList<String> logMessageList = new ArrayList<>();

        for (Exchange oneExchange : grouped) {

            // The JSON parsing here is actually unnecessary, it's just to make sure the received JSON is parsable.
            // We should just pass the String body onto the list and pass it on.
            String body = (String) oneExchange.getIn().getBody();
            PdlLogMessage pdlLogMessage = objectMapper.readValue(body, PdlLogMessage.class);
            logMessageList.add(objectMapper.writeValueAsString(pdlLogMessage));
        }

        return logMessageList;
    }
}
