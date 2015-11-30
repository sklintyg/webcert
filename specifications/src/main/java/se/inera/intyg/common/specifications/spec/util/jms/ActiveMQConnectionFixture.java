package se.inera.certificate.spec.util.jms;

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
