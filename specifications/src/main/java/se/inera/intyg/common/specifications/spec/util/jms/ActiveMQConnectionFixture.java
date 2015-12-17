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
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Generic Fixture for integrating JMS fixtures with the JVS3/Spring JMS connection and transaction management
 * services.
 */
public class ActiveMQConnectionFixture {

    public static final String DEFAULT_CONNECTION_FACTORY_URL = "tcp://localhost:61616";

    private static ConnectionFactory connectionFactory;
    private static String connectionFactoryUrl = DEFAULT_CONNECTION_FACTORY_URL;

    /**
     * Get database connection.
     *
     * @return the connection
     */
    public static Connection getConnection() {
        try {
            return getConnectionFactory().createConnection();
        } catch (JMSException e) {
            throw new RuntimeException("Could not open connection", e);
        }
    }

    public static synchronized ConnectionFactory getConnectionFactory() {
        if (connectionFactory == null) {
            connectionFactory = new ActiveMQConnectionFactory(connectionFactoryUrl);
        }
        return connectionFactory;
    }

    /**
     * Set connectionFactory URL.
     */
    public void setConnectionFactoryUrl(String connectionFactoryUrl) {
        ActiveMQConnectionFixture.connectionFactoryUrl = connectionFactoryUrl;
    }

}
