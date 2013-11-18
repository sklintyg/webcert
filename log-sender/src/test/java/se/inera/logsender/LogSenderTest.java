package se.inera.logsender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.inera.logsender.messages.AbstractLogMessage;
import se.inera.logsender.messages.IntygReadMessage;
import se.riv.ehr.log.store.storelog.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponseType;
import se.riv.ehr.log.store.v1.ResultType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResultCodeType;

/**
 * @author andreaskaltenbach
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-context.xml")
@ActiveProfiles(profiles = "dev")
@DirtiesContext
public class LogSenderTest {

    @Autowired
    private StoreLogResponderInterface storeLogMock;

    @Autowired
    @Qualifier("nonTransactedJmsTemplate")
    private JmsTemplate jmsTemplate;

    @Autowired
    private LogSender logSender;

    @Autowired
    private Queue queue;

    private List<AbstractLogMessage> logEntries = new ArrayList<AbstractLogMessage>() {
        {
            add(intygReadMessage("2013-01-01T10:00"));
            add(intygReadMessage("2013-01-02T10:00"));
            add(intygReadMessage("2013-01-03T10:00"));
            add(intygReadMessage("2013-01-04T10:00"));
            add(intygReadMessage("2013-01-05T10:00"));
            add(intygReadMessage("2013-01-06T10:00"));
        }
    };

    private AbstractLogMessage intygReadMessage(String timestamp) {
        IntygReadMessage intygReadMessage = new IntygReadMessage();
        intygReadMessage.setEnhetId("enhet1");
        intygReadMessage.setSystemId("webcert");
        intygReadMessage.setTimestamp(new LocalDateTime(timestamp));
        intygReadMessage.setUserId("user1");
        intygReadMessage.setVardgivareId("vg1");
        return intygReadMessage;
    }

    @Test
    public void testSendingLogMessages() throws InterruptedException {

        ArgumentCaptor<StoreLogRequestType> capture = ArgumentCaptor.forClass(StoreLogRequestType.class);

        for (AbstractLogMessage intygReadMessage : logEntries.subList(0, 3)) {
            sendLogMessage(intygReadMessage);
        }

        when(storeLogMock.storeLog(anyString(), capture.capture())).thenReturn(storeLogResponse(ResultCodeType.OK));

        Thread.sleep(1000);

        logSender.sendLogEntries();

        // ensure that all three entries are sent to loggtjänst
        StoreLogRequestType request = capture.getValue();
        assertEquals(3, request.getLog().size());

        List<String> logIds = new ArrayList<String>() {
            {
                add(logEntries.get(0).getLogId());
                add(logEntries.get(1).getLogId());
                add(logEntries.get(2).getLogId());
            }
        };

        for (LogType logType : request.getLog()) {
            assertTrue(logIds.contains(logType.getLogId()));
        }

        // ensure that queue is empty
        assertEquals(0, queueSize());
    }

    @Test
    public void testSendingAndSplittingInChunks() throws InterruptedException {

        ArgumentCaptor<StoreLogRequestType> capture = ArgumentCaptor.forClass(StoreLogRequestType.class);

        for (AbstractLogMessage logMessage : logEntries) {
            sendLogMessage(logMessage);
        }

        when(storeLogMock.storeLog(anyString(), capture.capture())).thenReturn(storeLogResponse(ResultCodeType.OK));

        Thread.sleep(1000);

        logSender.sendLogEntries();

        // ensure that messages are split into two chunks
        List<StoreLogRequestType> requests = capture.getAllValues();
        assertEquals(5, requests.get(0).getLog().size());

        List<String> logIds = new ArrayList<String>() {
            {
                add(logEntries.get(0).getLogId());
                add(logEntries.get(1).getLogId());
                add(logEntries.get(2).getLogId());
                add(logEntries.get(3).getLogId());
                add(logEntries.get(4).getLogId());
            }
        };
        for (LogType logType : requests.get(0).getLog()) {
            assertTrue(logIds.contains(logType.getLogId()));
        }

        assertEquals(1, requests.get(1).getLog().size());
        assertEquals(logEntries.get(5).getLogId(), requests.get(1).getLog().get(0).getLogId());

        // ensure that queue is empty
        assertEquals(0, queueSize());
    }

    @Test
    public void testBulkSendingFailingSecondTime() throws InterruptedException, JMSException {

        for (AbstractLogMessage logMessage : logEntries) {
            sendLogMessage(logMessage);
        }

        when(storeLogMock.storeLog(anyString(), any(StoreLogRequestType.class))).thenReturn(
                storeLogResponse(ResultCodeType.OK)).thenReturn(storeLogResponse(ResultCodeType.ERROR));

        Thread.sleep(1000);

        logSender.sendLogEntries();

        // ensure that queue still contains last messages
        assertEquals(1, queueSize());

        // ensure that remaining message is last log entry
        Message message = jmsTemplate.receive();
        ObjectMessage objectMessage = (ObjectMessage) message;
        AbstractLogMessage logMessage = (AbstractLogMessage) objectMessage.getObject();
        assertEquals(logEntries.get(5).getLogId(), logMessage.getLogId());
    }

    @Test
    public void testBulkSendingWithFailingLoggtjanst() throws InterruptedException {

        for (AbstractLogMessage logMessage : logEntries.subList(0, 3)) {
            sendLogMessage(logMessage);
        }

        when(storeLogMock.storeLog(anyString(), any(StoreLogRequestType.class))).thenReturn(
                storeLogResponse(ResultCodeType.ERROR));

        Thread.sleep(1000);

        logSender.sendLogEntries();

        // messages should still be in queue
        assertEquals(3, queueSize());
    }

    private int queueSize() {
        return jmsTemplate.execute(new SessionCallback<Integer>() {
            @Override
            public Integer doInJms(Session session) throws JMSException {
                QueueBrowser queueBrowser = session.createBrowser(queue);
                Enumeration queueMessageEnum = queueBrowser.getEnumeration();
                int count = 0;
                while (queueMessageEnum.hasMoreElements()) {
                    queueMessageEnum.nextElement();
                    count++;
                }
                return count;
            }
        }, true);
    }

    private StoreLogResponseType storeLogResponse(ResultCodeType resultCode) {
        StoreLogResponseType response = new StoreLogResponseType();
        response.setResultType(new ResultType());
        response.getResultType().setResultCode(resultCode);
        return response;
    }

    private void sendLogMessage(final AbstractLogMessage intygReadMessage) {
        this.jmsTemplate.send(queue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage(intygReadMessage);
            }
        });
    }
}
