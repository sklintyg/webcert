/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
import org.apache.camel.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.infra.logmessages.ActivityType;
import se.inera.intyg.common.util.integration.integration.json.CustomObjectMapper;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.logsender.helper.TestDataHelper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2016-03-08.
 */
@RunWith(MockitoJUnitRunner.class)
public class LogMessageAggregationProcessorTest {

    private LogMessageAggregationProcessor testee = new LogMessageAggregationProcessor();

    private ObjectMapper objectMapper = new CustomObjectMapper();

    @Test
    public void testOkGroupedExchange() throws Exception {
        String body = testee.process(buildGroupedExchange(1, 1));
        List<String> output = objectMapper.readValue(body, ArrayList.class);
        assertEquals(1, output.size());
    }

    /**
     * Even though we have a splitter before this step, this step will forward with multiple
     * resources - if they are for the same patient, it is valid.
     */
    @Test
    public void testGroupedExchangeWithMultipleResources() throws Exception {
        String body = testee.process(buildGroupedExchange(3, 5));
        List<String> output = objectMapper.readValue(body, ArrayList.class);
        assertEquals(3, output.size());
    }

    @Test(expected = PermanentException.class)
    public void testEmptyGroupedExchange() throws Exception {
        testee.process(buildGroupedExchange(0, 1));
    }

    private Exchange buildGroupedExchange(int exchangeSize, int resourcesPerMessageSize) {
        Exchange exchange = mock(Exchange.class);
        List list = buildExchangeList(exchangeSize, resourcesPerMessageSize);
        when(exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class)).thenReturn(list);
        return exchange;
    }

    private List buildExchangeList(int exchangeSize, int resourcesPerMessageSize) {
        List<Exchange> exchangeList = new ArrayList<>();
        for (int a = 0; a < exchangeSize; a++) {
            Exchange exchange = mock(Exchange.class);
            Message m = buildMockMessage(resourcesPerMessageSize);
            when(exchange.getIn()).thenReturn(m);
            exchangeList.add(exchange);
        }
        return exchangeList;
    }

    private Message buildMockMessage(int resourcesPerMessageSize) {
        Message m = mock(Message.class);
        when(m.getBody()).thenReturn(TestDataHelper.buildBasePdlLogMessageAsJson(ActivityType.READ, resourcesPerMessageSize));
        return m;
    }
}
