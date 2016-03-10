package se.inera.intyg.webcert.logsender.service;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.logmessages.ActivityType;
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

    @Test
    public void testOkGroupedExchange() throws Exception {
        ArrayList<String> output = testee.process(buildGroupedExchange(1));
        assertEquals(1, output.size());
    }

    @Test(expected = PermanentException.class)
    public void testEmptyGroupedExchange() throws Exception {
        testee.process(buildGroupedExchange(0));
    }

    private Exchange buildGroupedExchange(int size) {
        Exchange exchange = mock(Exchange.class);
        List list = buildExchangeList(size);
        when(exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class)).thenReturn(list);
        return exchange;
    }

    private List buildExchangeList(int size) {
        List<Exchange> exchangeList = new ArrayList<>();
        for (int a = 0; a < size; a++) {
            Exchange exchange = mock(Exchange.class);
            Message m = buildMockMessage();
            when(exchange.getIn()).thenReturn(m);
            exchangeList.add(exchange);
        }
        return exchangeList;
    }

    private Message buildMockMessage() {
        Message m = mock(Message.class);
        when(m.getBody()).thenReturn(TestDataHelper.buildBasePdlLogMessageListAsJson(ActivityType.READ));
        return m;
    }
}
