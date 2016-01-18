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

package se.inera.intyg.webcert.specifications.pages

import se.inera.intyg.common.specifications.page.AbstractPage

class HealthCheckPage extends AbstractPage {

    static url = "/healthcheck.jsp"
    static at = { title == "Webcert - Health Check" }

    static content = {
        dbMeasurement {$("#dbMeasurement").text()}
        dbStatus {$("#dbStatus").text()}
        jmsMeasurement {$("#jmsMeasurement").text()}
        jmsStatus {$("#jmsStatus").text()}
        hsaAuthorizationmanagementMeasurement {$("#hsaAuthorizationmanagementMeasurement").text()}
        hsaAuthorizationmanagementStatus {$("#hsaAuthorizationmanagementStatus").text()}

        hsaEmployeeMeasurement {$("#hsaEmployeeMeasurement").text()}
        hsaEmployeeStatus {$("#hsaEmployeeStatus").text()}

        hsaOrganizationMeasurement {$("#hsaOrganizationMeasurement").text()}
        hsaOrganizationStatus {$("#hsaOrganizationStatus").text()}

        intygstjanstMeasurement {$("#intygstjanstMeasurement").text()}
        intygstjanstStatus {$("#intygstjanstStatus").text()}
        signatureQueueMeasurement {$("#signatureQueueMeasurement").text()}
        uptime {$("#uptime").text()}
    }
}
