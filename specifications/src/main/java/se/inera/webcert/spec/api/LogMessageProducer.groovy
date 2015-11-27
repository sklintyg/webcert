package se.inera.webcert.spec.api

import org.joda.time.LocalDateTime
import se.inera.certificate.spec.util.jms.ActiveMQConnectionFixture
import se.inera.certificate.spec.util.jms.JMSUtils
import se.inera.certificate.spec.util.jms.Producer
import se.inera.intyg.webcert.logmessages.IntygReadMessage

import javax.jms.Connection
import javax.jms.Message
import javax.jms.MessageProducer
import javax.jms.Session

/**
 * @author andreaskaltenbach
 */
class LogMessageProducer extends Producer {

    Connection conn
    Session session
    MessageProducer producer

    def aktivitet
    def vardperson
    def enhet
    def vardgivare
    def personnummer

    LogMessageProducer(String queue) {
        super(queue)
    }

    def beginTable() {
        conn = ActiveMQConnectionFixture.getConnection()
        conn.start()
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)
        producer = session.createProducer(destination)
    }

    def execute() {
        def m
        if (aktivitet == "läsaIntyg") {
            m = new IntygReadMessage()
            m.systemId = "Fitnesse"
            m.timestamp = new LocalDateTime()
            m.userId = vardperson
            m.enhetId = enhet
            m.vardgivareId = vardgivare
            Message jmsMessage = session.createObjectMessage(m)
            producer.send(jmsMessage)
            Thread.sleep(5000)
        }
    }

    def endTable() {
        JMSUtils.closeQuitely(conn, session, producer)
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
}
