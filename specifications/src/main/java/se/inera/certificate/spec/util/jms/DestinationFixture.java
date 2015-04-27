package se.inera.certificate.spec.util.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;

/**
 * Generic Fixture for retrieving JMS queues.
 */
public abstract class DestinationFixture {

    private static Map<String, Destination> destinations = new HashMap<>();

    /**
     * Get destination.
     *
     * @return the connection
     */
    public static Destination getDestination(String destination) {
        if (destinations.containsKey(destination)) {
            return destinations.get(destination);
        } else {
            throw new IllegalStateException("Destination not configured");
        }
    }

    void addDestination(String name, Destination destination) {
        destinations.put(name, destination);
    }

    /**
     * Register destination.
     */
    public void registerDestinationAs(String name, String lookup) throws JMSException {
    }

    /**
     * Register Queue.
     */
    public void registerQueueAs(String name, String lookup) throws JMSException {
        registerDestinationAs(name, lookup);
    }

    /**
     * Register Topic.
     */
    public void registerTopicAs(String name, String lookup) throws JMSException {
        registerDestinationAs(name, lookup);
    }

}
