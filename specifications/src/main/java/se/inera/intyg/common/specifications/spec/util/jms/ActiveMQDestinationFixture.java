/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.common.specifications.spec.util.jms;

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
