package se.inera.webcert.service.monitoring;

import se.inera.webcert.service.monitoring.dto.HealthStatus;

/**
 * Service for checking the health of the application.
 *
 * @author npet
 *
 */
public interface HealthCheckService {

    /**
     * Check if the database responds.
     *
     * @return
     */
    HealthStatus checkDB();

    /**
     * Check if the connection to ActiveMQ is up.
     *
     * @return
     */
    HealthStatus checkJMS();

    /**
     * Returns the size of the queue in the db that holds signed certificates
     * yet to be registered in the Intygstjanst.
     *
     * @return
     */
    HealthStatus checkSignatureQueue();

    /**
     * Check if the connection to HSA is up.
     *
     * @return
     */
    HealthStatus checkHSA();

    /**
     * Returns the applications uptime in human readable format.
     *
     * @return
     */
    HealthStatus checkUptime();

    /**
     * Check if the connection to Intygstjansten is up.
     *
     * @return
     */
    HealthStatus checkIntygstjanst();
    
    /**
     * Checks the number of logged in users.
     * 
     * @return
     */
    HealthStatus checkNbrOfUsers();
}
