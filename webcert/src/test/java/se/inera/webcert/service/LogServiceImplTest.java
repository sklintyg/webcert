package se.inera.webcert.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.web.service.WebCertUserService;
import se.inera.webcert.web.service.WebCertUserServiceImpl;

import javax.jms.*;

import static org.mockito.Mockito.when;

/**
 * Created by pehr on 13/11/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(locations = { "/process-log-qm-test.xml" })
@DirtiesContext
public class LogServiceImplTest {

    private JmsTemplate jmsTemplate;

    @Mock
    protected WebCertUserService webCertUserService = new WebCertUserServiceImpl();

    @InjectMocks
    LogServiceImpl logService = new LogServiceImpl();

    @Autowired
    private ConnectionFactory connectionFactory;

    @Before
    public void setup() {
        setConnectionFactory(connectionFactory);
        org.springframework.test.util.ReflectionTestUtils.setField(logService, "jmsTemplate", this.jmsTemplate);
    }

    public void setConnectionFactory(ConnectionFactory cf) {
        this.jmsTemplate = new JmsTemplate(cf);
    }


    @Test
    @Ignore
    public void testTwo()throws  Exception{
        final Connection connection = connectionFactory.createConnection();
        connection.start();
        final Session session = connection.createSession(false,
                Session.AUTO_ACKNOWLEDGE);

        Destination destination = null; //new ActiveMQQueue("loggning.test.queue");

      //  when(webCertUserService.getWebCertUser()).thenReturn(createWcUser()) ;
      //  logService.logReadOfIntyg("TESTINTYG-1");

        /*
        this.jmsTemplate.send(destination, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage("hello queue world");
                message.setJMSCorrelationID("C12");
                return message;
            }
        });
          */
        {
            final MessageConsumer consumer = session.createConsumer(destination);
            final TextMessage message = (TextMessage) consumer.receiveNoWait();
            Assert.assertNotNull(message);
            String apa =  message.getText();
            System.out.println("MESSAGE :" + apa);

            Assert.assertEquals("hello queue world", message.getText());
        }
    }


    protected WebCertUser createWcUser(){
        WebCertUser wcu = new WebCertUser();
        wcu.setHsaId("HSAID");
        wcu.setNamn("TESTUSER TESTSSSON");


        return wcu;
    }
}
