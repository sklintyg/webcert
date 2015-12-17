package se.inera.intyg.webcert.web.service.monitoring;

import se.inera.intyg.webcert.web.service.monitoring.dto.HealthStatus;

/**
 * Service for checking the health of the application.
 *
 * @author npet
 *
 */
public interface HealthCheckService {

    /**
     * Check if the database responds.
     */
    HealthStatus checkDB();

    /**
     * Check if the connection to ActiveMQ is up.
     */
    HealthStatus checkJMS();

    /**
     * Returns the size of the queue in the db that holds signed certificates
     * yet to be registered in the Intygstjanst.
     */
    HealthStatus checkSignatureQueue();

    /**
     * Check if the connection to HSA authmgmt over NTjP is up.
     */
    HealthStatus checkHsaAuthorizationmanagement();

    /**
     * Check if the connection to HSA employee over NTjP is up.
     */
    HealthStatus checkHsaEmployee();

    /**
     * Check if the connection to HSA organization over NTjP is up.
     */
    HealthStatus checkHsaOrganization();

    /**
     * Returns the applications uptime.
     */
    HealthStatus checkUptime();

    /**
     * Returns the applications uptime in human readable format.
     */
    String checkUptimeAsString();

    /**
     * Check if the connection to Intygstjansten is up.
     */
    HealthStatus checkIntygstjanst();

    /**
     * Checks the number of logged in users.
     */
    HealthStatus checkNbrOfUsers();

}
