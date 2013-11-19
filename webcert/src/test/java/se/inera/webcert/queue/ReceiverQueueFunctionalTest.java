package se.inera.webcert.queue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/process-log-qm-test.xml" })
@DirtiesContext
public class ReceiverQueueFunctionalTest {

    private static final int DELAY = 5000;
    private JmsTemplate jmsTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Before
    public void setup() {
        setConnectionFactory(connectionFactory);
    }

    public void setConnectionFactory(ConnectionFactory cf) {
        this.jmsTemplate = new JmsTemplate(cf);
    }

    @Ignore
    @Test
    public void testTwo()throws  Exception{
        final Connection connection = connectionFactory.createConnection();
        connection.start();
        final Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);
        final Queue queue = session.createTemporaryQueue();
        {
            final MessageProducer producer = session.createProducer(queue);
            final TextMessage message = session.createTextMessage("testing");
            producer.send(message);
        }
        {
            final MessageConsumer consumer = session.createConsumer(queue);
            final TextMessage message = (TextMessage) consumer.receiveNoWait();
            Assert.assertNotNull(message);
            Assert.assertEquals("testing", message.getText());
        }
    }
}
