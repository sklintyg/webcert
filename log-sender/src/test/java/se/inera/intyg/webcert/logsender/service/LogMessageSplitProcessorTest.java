package se.inera.intyg.webcert.logsender.service;

import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.junit.Test;
import se.inera.intyg.common.logmessages.ActivityType;
import se.inera.intyg.webcert.common.sender.exception.PermanentException;
import se.inera.intyg.webcert.logsender.helper.TestDataHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by eriklupander on 2016-03-16.
 */
public class LogMessageSplitProcessorTest {

    private LogMessageSplitProcessor testee = new LogMessageSplitProcessor();

    @Test
    public void testSingleResource() throws Exception {
        List<Message> messages = testee.process(buildMessage(1));
        assertEquals(1, messages.size());
    }

    @Test
    public void testMultipleResources() throws Exception {
        List<Message> messages = testee.process(buildMessage(3));
        assertEquals(3, messages.size());
    }

    @Test(expected = PermanentException.class)
    public void testNoResource() throws Exception {
        testee.process(buildMessage(0));
    }

    private Message buildMessage(int numberOfResources) {
        DefaultMessage msg = new DefaultMessage();
        msg.setBody(buildBody(numberOfResources));
        return msg;
    }

    private String buildBody(int numberOfResources) {
        return TestDataHelper.buildBasePdlLogMessageAsJson(ActivityType.READ, numberOfResources);
    }
}
