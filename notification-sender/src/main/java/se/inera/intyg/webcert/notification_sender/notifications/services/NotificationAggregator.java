/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.notification_sender.notifications.services;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.webcert.logging.MdcHelper;
import se.inera.intyg.webcert.logging.MdcLogConstants;
import se.inera.intyg.webcert.notification_sender.notifications.filter.NotificationMessageDiscardFilter;

/**
 * Extracts the List of individual {@link Exchange}s that have been aggregated during the last minute and lets
 * the {@link NotificationMessageDiscardFilter} filter out those not applicable to be forwarded to the
 * notificationQueue.
 *
 * Note that this processor returns a List of messages as its output - typically the next processor should be a standard
 * camel "split" Processor that splits the List into its individual messages and then forwards them to the
 * notificationQueue, i.e:
 *
 * M(1...n) -> Aggregator (this) -> M(List of 1...n messages) -> Split -> M(1...n)
 *
 * Created by eriklupander on 2016-07-04.
 */

@Slf4j
public class NotificationAggregator {

    @Autowired
    private MdcHelper mdcHelper;

    public List<Message> process(Exchange exchange) {

        try {
            MDC.put(MdcLogConstants.TRACE_ID_KEY, mdcHelper.traceId());
            MDC.put(MdcLogConstants.SPAN_ID_KEY, mdcHelper.spanId());

            List<Exchange> grouped = exchange.getIn().getBody(List.class);

            if (grouped == null || grouped.isEmpty()) {
                log.info("No aggregated log messages, this is normal if camel aggregator has a batch timeout. Doing nothing.");
                return Collections.emptyList();
            }

            // Pull the inbound messages from the exchanges
            List<Message> aggregatedList = grouped.stream()
                .map(Exchange::getIn)
                .toList();

            // And finally let the filtering component do its job, passing on a List of those messages we want to pass down
            // the route.
            return new NotificationMessageDiscardFilter().process(aggregatedList);
        } finally {
            MDC.clear();
        }
    }
}
