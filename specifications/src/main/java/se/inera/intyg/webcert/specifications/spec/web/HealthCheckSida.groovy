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

package se.inera.intyg.webcert.specifications.spec.web

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.pages.HealthCheckPage
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class HealthCheckSida extends ExceptionHandlingFixture {

    private String databasTid
    private String databasStatus
    private String jmsTid
    private String jmsStatus

    private String hsaAuthorizationmanagementTid
    private String hsaAuthorizationmanagementStatus

    private String hsaEmployeeTid
    private String hsaEmployeeStatus

    private String hsaOrganizationTid
    private String hsaOrganizationStatus

    private String intygstjanstTid
    private String intygstjanstStatus
    private String signeringsKo
    private String upptid

    void execute() {
        Browser.drive {
            to HealthCheckPage
            assert at(HealthCheckPage)
            databasTid = page.dbMeasurement
            databasStatus = page.dbStatus
            jmsTid = page.jmsMeasurement
            jmsStatus = page.jmsStatus

            hsaAuthorizationmanagementTid = page.hsaAuthorizationmanagementMeasurement
            hsaAuthorizationmanagementStatus = page.hsaAuthorizationmanagementStatus

            hsaEmployeeTid = page.hsaEmployeeMeasurement
            hsaEmployeeStatus = page.hsaEmployeeStatus

            hsaOrganizationTid = page.hsaOrganizationMeasurement
            hsaOrganizationStatus = page.hsaOrganizationStatus

            intygstjanstTid = page.intygstjanstMeasurement
            intygstjanstStatus = page.intygstjanstStatus
            signeringsKo = page.signatureQueueMeasurement
            upptid = page.uptime
        }
    }

    String databasTid() {
        databasTid
    }
    String databasStatus() {
        databasStatus
    }
    String jmsTid() {
        jmsTid
    }
    String jmsStatus() {
        jmsStatus
    }

    String hsaAuthorizationmanagementTid() {
        hsaAuthorizationmanagementTid
    }
    String hsaAuthorizationmanagementStatus() {
        hsaAuthorizationmanagementStatus
    }

    String hsaEmployeeTid() {
        hsaEmployeeTid
    }
    String hsaEmployeeStatus() {
        hsaEmployeeStatus
    }

    String hsaOrganizationTid() {
        hsaOrganizationTid
    }
    String hsaOrganizationStatus() {
        hsaOrganizationStatus
    }


    String intygstjanstTid() {
        intygstjanstTid
    }
    String intygstjanstStatus() {
        intygstjanstStatus
    }
    String signeringsKo() {
        signeringsKo
    }
    String upptid() {
        upptid
    }

}
