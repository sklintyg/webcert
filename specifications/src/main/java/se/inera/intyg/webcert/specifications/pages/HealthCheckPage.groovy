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
