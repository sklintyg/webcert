package se.inera.certificate.spec.util.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;


/**
 * Generic Fixture for retrieving JMS queues.
 */
public class ActiveMQDestinationFixture extends DestinationFixture {

    @Override
    public void registerQueueAs(String name, String lookup) throws JMSException {
        Connection connection = null;
        Session session = null;
        try {
            connection = ActiveMQConnectionFixture.getConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(name);
            addDestination(lookup, queue);
        } finally {
            JMSUtils.closeQuitely(connection, session);
        }
    }

    @Override
    public void registerTopicAs(String name, String lookup) throws JMSException {
        Connection connection = ActiveMQConnectionFixture.getConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(name);
        addDestination(lookup, topic);
    }

}
