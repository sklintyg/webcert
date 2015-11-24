package se.inera.webcert.spec.util

import se.inera.certificate.spec.util.jms.ActiveMQConnectionFixture
import se.inera.certificate.spec.util.jms.DestinationFixture
import se.inera.certificate.spec.util.jms.JMSUtils
import se.inera.intyg.webcert.logmessages.IntygReadMessage

import javax.jms.*

class LogConsumer {

    Destination destination
    def message
    def intygMsg
    def userhsaid
    int timeout = 2000

    LogConsumer(String queue) {
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

    public void receiveObjectMessage() {
        Connection conn = null;
        Session session = null;
        MessageConsumer consumer = null;
        try {
            conn = ActiveMQConnectionFixture.getConnection()
            conn.start()
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)
            consumer = session.createConsumer(destination)
            message = (ObjectMessage)consumer.receive(timeout)
            intygMsg = (IntygReadMessage)message.getObject();
            userhsaid = intygMsg.getUserId();
        } finally {
            JMSUtils.closeQuitely(conn, session, consumer);
        }
    }

    public String message() {
        return message.text
    }
    public String userhsaid() {
        return userhsaid
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
