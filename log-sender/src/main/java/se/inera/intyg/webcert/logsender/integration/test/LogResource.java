package se.inera.intyg.webcert.logsender.integration.test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import se.inera.intyg.webcert.logmessages.AbstractLogMessage;

public class LogResource {

    @Autowired
    @Qualifier("nonTransactedJmsTemplate")
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue queue;

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendLogMessage(final AbstractLogMessage message) {
        jmsTemplate.send(queue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                ObjectMessage om = session.createObjectMessage();
                om.setObject(message);
                return om;
            }
        });
        return Response.ok().build();
    }
}
