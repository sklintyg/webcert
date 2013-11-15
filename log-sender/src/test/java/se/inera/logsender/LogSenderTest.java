package se.inera.logsender;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Enumeration;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
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

    @Test
    public void testBulkSendingOfLogMessages() throws InterruptedException {

        ArgumentCaptor<StoreLogRequestType> capture = ArgumentCaptor.forClass(StoreLogRequestType.class);

        simpleSend("aaa");
        simpleSend("bbb");
        simpleSend("ccc");

        Thread.sleep(1000);

        when(storeLogMock.storeLog(anyString(), capture.capture())).thenReturn(storeLogResponse(ResultCodeType.OK));

        logSender.sendLogEntries();

        // ensure that all three entries are sent to loggtjänst
        StoreLogRequestType request = capture.getValue();
        assertEquals(3, request.getLog().size());
        // TODO - check every log entry

        // ensure that queue is empty
        assertEquals(0, queueSize());
    }

    @Test
    public void testBulkSendingMultipleTimes() throws InterruptedException {

        ArgumentCaptor<StoreLogRequestType> capture = ArgumentCaptor.forClass(StoreLogRequestType.class);

        simpleSend("aaa");
        simpleSend("bbb");
        simpleSend("ccc");
        simpleSend("ddd");
        simpleSend("eee");
        simpleSend("fff");

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

        simpleSend("aaa");
        simpleSend("bbb");
        simpleSend("ccc");
        simpleSend("ddd");
        simpleSend("eee");
        simpleSend("fff");

        Thread.sleep(1000);

        when(storeLogMock.storeLog(anyString(), any(StoreLogRequestType.class)))
                .thenReturn(storeLogResponse(ResultCodeType.OK))
                .thenReturn(storeLogResponse(ResultCodeType.ERROR));

        logSender.sendLogEntries();

        // ensure that queue still contains last messages
        assertEquals(1, queueSize());
        //TODO - check that remaining element is 'fff'
    }

    @Test
    public void testBulkSendingWithFailingLoggtjanst() throws InterruptedException {

        simpleSend("aaa");
        simpleSend("bbb");
        simpleSend("ccc");

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

    private void simpleSend(final String intyg) {

        Destination destination = new ActiveMQQueue("logging.queue");

        this.jmsTemplate.send(destination, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(intyg);

                System.out.println("Putting message " + intyg + " on queue");
                return message;
            }
        });
    }
}
