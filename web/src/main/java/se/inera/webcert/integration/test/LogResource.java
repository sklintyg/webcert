package se.inera.webcert.integration.test;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

import se.inera.log.messages.AbstractLogMessage;

@Transactional
public class LogResource {

    private long timeOut = 1000;
    
    LogResource() {}
    
    LogResource(long timeOut) {
        this.timeOut = timeOut;
    }
    
    @Autowired
    @Qualifier("jmsPDLLogTemplate")
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue queue;

    @DELETE
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLogMessages() {
        int count = countMessages();
        long originalTimeout = jmsTemplate.getReceiveTimeout();
        try {
            jmsTemplate.setReceiveTimeout(timeOut);
            for (int i = 0; i < count; i++) {
                jmsTemplate.receive(queue);
            }
        } finally {
            jmsTemplate.setReceiveTimeout(originalTimeout);
        }
        return Response.ok().build();
    }


    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public int countMessages() {
        return jmsTemplate.browse(new BrowserCallback<Integer>() {
            @Override
            public Integer doInJms(Session session, QueueBrowser browser) throws JMSException {
                Enumeration<?> messages = browser.getEnumeration();
                int total = 0;
                while (messages.hasMoreElements()) {
                    messages.nextElement();
                    total++;
                }
                
                return total;
            }
        });
    }
    
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public AbstractLogMessage getLogMessage() {
        long originalTimeout = jmsTemplate.getReceiveTimeout();
        try {
            jmsTemplate.setReceiveTimeout(timeOut);
            Message message = jmsTemplate.receive(queue);
            return (AbstractLogMessage) ((ObjectMessage) message).getObject();
        } catch (JMSException e) {
            throw new RuntimeException("Could not retreive log message: " + e.getMessage(), e);
        } finally {
            jmsTemplate.setReceiveTimeout(originalTimeout);
        }
    }
}
