package se.inera.certificate.spec.util.jms

import javax.jms.Connection
import javax.jms.Destination
import javax.jms.Message
import javax.jms.MessageProducer
import javax.jms.Session

import se.inera.certificate.spec.util.FitnesseHelper

class Producer {

    Destination destination
    
    Producer(String queue) {
        destination = DestinationFixture.getDestination(queue)
    }

    void sendString(String message) {
        Connection conn = null
        Session session = null
        MessageProducer producer = null
        try {
            conn = ActiveMQConnectionFixture.getConnection()
            conn.start()
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)
            producer = session.createProducer(destination)
            Message jmsMessage = session.createTextMessage(message)
            producer.send(jmsMessage)
        } finally {
            JMSUtils.closeQuitely(conn, session, producer)
        }

    }

    void sendFile(String fileName) {
        String message = FitnesseHelper.getFileAsString(fileName)
        sendString(message)
    }
}
