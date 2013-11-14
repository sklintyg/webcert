package se.inera.logsender;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Timer;
import java.util.TimerTask;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
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

    private JmsTemplate jmsTemplate;

    private Timer timer = new Timer();

    @Autowired
    private StoreLogResponderInterface storeLogMock;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Before
    public void setup() {
        this.jmsTemplate = new JmsTemplate(connectionFactory);
    }

    @Test
    public void testBulkSendingOfLogMessages() throws InterruptedException {

        ArgumentCaptor<StoreLogRequestType> capture = ArgumentCaptor.forClass(StoreLogRequestType.class);

        StoreLogResponseType response = new StoreLogResponseType();
        response.setResultType(new ResultType());
        response.getResultType().setResultCode(ResultCodeType.OK);

        when(storeLogMock.storeLog(anyString(), capture.capture())).thenReturn(response);

        simpleSend("aaa");
        simpleSend("bbb");
        simpleSend("ccc");
        simpleSend("ddd");
        simpleSend("eee");
        simpleSend("fff");
        simpleSend("ggg");
        simpleSend("hhh");
        simpleSend("iii");
        simpleSend("jjj");
        simpleSend("kkk");
        simpleSend("lll");
        simpleSend("mmm");
        simpleSend("nnn");

        Thread.sleep(1000);
        StoreLogRequestType request = capture.getValue();

        //TODO - verify request
        assertEquals(10, request.getLog().size());
    }

    @Test
    public void testWithFixedRate() throws InterruptedException {

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                simpleSend("m" + System.currentTimeMillis());
            }
        }, 0, 1000);

        Thread.sleep(100000);

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
