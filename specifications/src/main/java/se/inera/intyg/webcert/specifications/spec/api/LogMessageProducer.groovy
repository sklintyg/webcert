/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.specifications.spec.api

import java.time.LocalDateTime
import se.inera.intyg.common.logmessages.PdlLogMessage
import se.inera.intyg.common.specifications.spec.util.jms.ActiveMQConnectionFixture
import se.inera.intyg.common.specifications.spec.util.jms.JMSUtils
import se.inera.intyg.common.specifications.spec.util.jms.Producer

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
            m = mew PdlLogMessage()
            m.systemId = "Fitnesse"
            m.timestamp = LocalDateTime.now()
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
