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

package se.inera.intyg.common.specifications.spec.util.jms

import javax.jms.Connection
import javax.jms.Destination
import javax.jms.Message
import javax.jms.MessageProducer
import javax.jms.Session

import se.inera.intyg.common.specifications.spec.util.FitnesseHelper

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
