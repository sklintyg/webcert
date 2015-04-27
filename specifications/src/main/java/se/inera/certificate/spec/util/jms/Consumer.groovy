package se.inera.certificate.spec.util.jms

import javax.jms.Connection
import javax.jms.Destination
import javax.jms.Message
import javax.jms.MessageConsumer
import javax.jms.QueueBrowser
import javax.jms.Session

class Consumer {

    Destination destination
    def message
    int timeout = 2000
    
    Consumer(String queue) {
        destination = DestinationFixture.getDestination(queue)
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout
    }
    
    public void clearQueue() {
        Connection conn = null;
        Session session = null;
        MessageConsumer consumer = null;
        int itemCount = queueDepth()
        try {
            conn = ActiveMQConnectionFixture.getConnection()
            conn.start()
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)
            consumer = session.createConsumer(destination)
            
            itemCount.times {
                consumer.receive(timeout)
            }
        } finally {
            JMSUtils.closeQuitely(conn, session, consumer)
        }
    }
    
    public void receiveMessage() {
        Connection conn = null;
        Session session = null;
        MessageConsumer consumer = null;
        try {
            conn = ActiveMQConnectionFixture.getConnection()
            conn.start()
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)
            consumer = session.createConsumer(destination)
            message = consumer.receive(timeout)
        } finally {
            JMSUtils.closeQuitely(conn, session, consumer);
        }
    }
    
    public String message() {
        return message.text
    }

    public int queueDepth() {
        Connection conn = null;
        Session session = null;
        QueueBrowser browser = null;
        int depth = 0
        try {
            conn = ActiveMQConnectionFixture.getConnection()
            conn.start()
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)
            browser = session.createBrowser(destination)
            Enumeration<Message> messages = browser.getEnumeration();
            while(messages.hasMoreElements()) {
                depth++;
                messages.nextElement();
            }
            
        } finally {
            JMSUtils.closeQuitely(conn, session, browser);
        }
        return depth;
    }
    
}
