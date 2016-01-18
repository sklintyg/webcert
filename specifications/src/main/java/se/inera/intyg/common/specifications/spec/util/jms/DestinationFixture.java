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
