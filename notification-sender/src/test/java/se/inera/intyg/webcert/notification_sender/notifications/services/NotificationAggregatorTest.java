/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Test;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NotificationAggregatorTest {

    private static final String INTYGS_ID = "intyg1";

    private ObjectMapper objectMapper = new CustomObjectMapper();

    private NotificationAggregator aggregator = new NotificationAggregator();

    @Test
    public void testProcessNoGroupedExchange() throws Exception {
        Exchange exchange = mock(Exchange.class);
        List<Message> res = aggregator.process(exchange);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testProcessGroupedExchangeEmpty() throws Exception {
        Exchange exchange = mock(Exchange.class);
        when(exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class)).thenReturn(new ArrayList<>());
        List<Message> res = aggregator.process(exchange);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testProcess() throws Exception {
        Exchange exchange = mock(Exchange.class);
        Exchange exchange1 = mock(Exchange.class);
        Exchange exchange2 = mock(Exchange.class);
        Exchange exchange3 = mock(Exchange.class);
        Message message1 = buildMessage(INTYGS_ID, HandelsekodEnum.ANDRAT, LocalDateTime.now());
        Message message2 = buildMessage(INTYGS_ID, HandelsekodEnum.SIGNAT, LocalDateTime.now());
        Message message3 = buildMessage("anotherintyg", HandelsekodEnum.ANDRAT, LocalDateTime.now());
        when(exchange1.getIn()).thenReturn(message1);
        when(exchange2.getIn()).thenReturn(message2);
        when(exchange3.getIn()).thenReturn(message3);
        when(exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class)).thenReturn(Arrays.asList(exchange1, exchange2, exchange3));
        List<Message> res = aggregator.process(exchange);

        assertNotNull(res);
        assertEquals(1, res.size()); // result is filtered through NotificationMessageDiscardFilter
    }

    private Message buildMessage(String intygsId, HandelsekodEnum ht, LocalDateTime tid) throws JsonProcessingException {
        Message message = mock(Message.class);
        NotificationMessage nf = new NotificationMessage();
        nf.setIntygsId(intygsId);
        nf.setHandelse(ht);
        nf.setHandelseTid(tid);
        when(message.getBody()).thenReturn(objectMapper.writeValueAsString(nf));
        return message;
    }
}
