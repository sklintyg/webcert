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

package se.inera.intyg.common.specifications.spec.util.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueBrowser;
import javax.jms.Session;

public final class JMSUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JMSUtils.class);

    private JMSUtils() {
    }

    public static void closeQuitely(Connection conn, Session session, MessageConsumer consumer) {
        closeQuitely(consumer);
        closeQuitely(session);
        closeQuitely(conn);
    }

    public static void closeQuitely(Connection conn, Session session, MessageProducer producer) {
        closeQuitely(producer);
        closeQuitely(session);
        closeQuitely(conn);
    }

    public static void closeQuitely(Connection conn, Session session, QueueBrowser browser) {
        closeQuitely(browser);
        closeQuitely(session);
        closeQuitely(conn);
    }

    public static void closeQuitely(Connection conn, Session session) {
        closeQuitely(session);
        closeQuitely(conn);
    }

    public static void closeQuitely(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                LOG.debug("Close error", e);
            }
        }
    }

    public static void closeQuitely(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                LOG.debug("Close error", e);
            }
        }
    }

    public static void closeQuitely(MessageConsumer consumer) {
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                LOG.debug("Close error", e);
            }
        }
    }

    public static void closeQuitely(MessageProducer producer) {
        if (producer != null) {
            try {
                producer.close();
            } catch (JMSException e) {
                LOG.debug("Close error", e);
            }
        }
    }

    public static void closeQuitely(QueueBrowser browser) {
        if (browser != null) {
            try {
                browser.close();
            } catch (JMSException e) {
                LOG.debug("Close error", e);
            }
        }
    }
}
