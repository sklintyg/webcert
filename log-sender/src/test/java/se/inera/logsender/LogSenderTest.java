package se.inera.logsender;

import static org.junit.Assert.assertEquals;
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

        Thread.sleep(1000);

        when(storeLogMock.storeLog(anyString(), capture.capture())).thenReturn(storeLogResponse(ResultCodeType.OK));

        logSender.sendLogEntries();

        // ensure that all three entries are sent to loggtjänst
        StoreLogRequestType request = capture.getValue();
        assertEquals(3, request.getLog().size());

        assertEquals(logEntries.get(0).getLogId(), request.getLog().get(0).getLogId());
        assertEquals(logEntries.get(1).getLogId(), request.getLog().get(1).getLogId());
        assertEquals(logEntries.get(2).getLogId(), request.getLog().get(2).getLogId());

        // ensure that queue is empty
        assertEquals(0, queueSize());
    }

    @Test
    public void testBulkSendingMultipleTimes() throws InterruptedException {

        ArgumentCaptor<StoreLogRequestType> capture = ArgumentCaptor.forClass(StoreLogRequestType.class);

        for (AbstractLogMessage logMessage : logEntries) {
            sendLogMessage(logMessage);
        }

        Thread.sleep(1000);

        when(storeLogMock.storeLog(anyString(), capture.capture())).thenReturn(storeLogResponse(ResultCodeType.OK));

        logSender.sendLogEntries();

        // ensure that all three entries are sent to loggtjänst
        List<StoreLogRequestType> request = capture.getAllValues();
        // assertEquals(3, request.getLog().size());
        // TODO - check every log entry

        // ensure that queue is empty
        assertEquals(0, queueSize());
    }

    @Test
    public void testBulkSendingFailingSecondTime() throws InterruptedException {

        for (AbstractLogMessage logMessage : logEntries) {
            sendLogMessage(logMessage);
        }

        Thread.sleep(1000);

        when(storeLogMock.storeLog(anyString(), any(StoreLogRequestType.class))).thenReturn(
                storeLogResponse(ResultCodeType.OK)).thenReturn(storeLogResponse(ResultCodeType.ERROR));

        logSender.sendLogEntries();

        // ensure that queue still contains last messages
        assertEquals(1, queueSize());
        // TODO - check that remaining element is 'fff'
    }

    @Test
    public void testBulkSendingWithFailingLoggtjanst() throws InterruptedException {

        for (AbstractLogMessage logMessage : logEntries.subList(0, 3)) {
            sendLogMessage(logMessage);
        }

        Thread.sleep(1000);

        when(storeLogMock.storeLog(anyString(), any(StoreLogRequestType.class))).thenReturn(
                storeLogResponse(ResultCodeType.ERROR));

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
