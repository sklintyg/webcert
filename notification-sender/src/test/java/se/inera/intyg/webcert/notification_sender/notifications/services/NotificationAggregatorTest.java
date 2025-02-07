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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.HandelsekodEnum;
import se.inera.intyg.common.support.modules.support.api.notification.NotificationMessage;
import se.inera.intyg.common.util.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.logging.MdcHelper;

@ExtendWith(MockitoExtension.class)
class NotificationAggregatorTest {

    @Spy
    private MdcHelper mdcHelper;

    @InjectMocks
    private final NotificationAggregator aggregator = new NotificationAggregator();

    final ObjectMapper objectMapper = new CustomObjectMapper();

    private static final String INTYGS_ID = "intyg1";

    @Test
    void testProcessNoGroupedExchange() {
        Exchange exchange = mock(Exchange.class);
        when(exchange.getIn()).thenReturn(mock(Message.class));
        List<Message> res = aggregator.process(exchange);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void testProcessGroupedExchangeEmpty() {
        final var messages = Collections.<Message>emptyList();
        final var groupedExchange = buildGroupedExchange(messages);
        List<Message> res = aggregator.process(groupedExchange);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void testProcess() throws Exception {
        final var messages = List.of(
            buildMessage(INTYGS_ID, HandelsekodEnum.ANDRAT, LocalDateTime.now()),
            buildMessage(INTYGS_ID, HandelsekodEnum.SIGNAT, LocalDateTime.now()),
            buildMessage("anotherintyg", HandelsekodEnum.ANDRAT, LocalDateTime.now())
        );

        List<Message> res = aggregator.process(buildGroupedExchange(messages));

        assertNotNull(res);
        assertEquals(1, res.size()); // result is filtered through NotificationMessageDiscardFilter
    }

    private Exchange buildGroupedExchange(List<Message> messages) {
        Exchange exchange = mock(Exchange.class);
        Message outerMessage = buildOuterMessage(messages);
        when(exchange.getIn()).thenReturn(outerMessage);
        return exchange;
    }

    private Message buildOuterMessage(List<Message> messages) {
        Message outerMsg = mock(Message.class);
        List<Exchange> outerBody = buildOuterBody(messages);
        when(outerMsg.getBody(List.class)).thenReturn(outerBody);
        return outerMsg;
    }

    private List<Exchange> buildOuterBody(List<Message> messages) {
        List<Exchange> groupedMessages = new ArrayList<>();
        for (Message message : messages) {
            Exchange innerExchange = mock(Exchange.class);
            when(innerExchange.getIn()).thenReturn(message);
            groupedMessages.add(innerExchange);
        }
        return groupedMessages;
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
