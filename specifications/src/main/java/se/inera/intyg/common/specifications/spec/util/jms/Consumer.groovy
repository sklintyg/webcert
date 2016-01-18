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
