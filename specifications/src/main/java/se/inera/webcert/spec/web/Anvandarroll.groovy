package se.inera.webcert.spec.web

import se.inera.certificate.spec.Browser

class Anvandarroll {

    def ändraTill(String role) {
        Browser.drive {
            go System.getProperty("webcert.baseUrl") + "api/roles/userrole/${role}"
            getDriver().navigate().back()
        }
    }

}
