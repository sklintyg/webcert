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
