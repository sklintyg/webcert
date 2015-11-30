package se.inera.webcert.spec.web

import se.inera.intyg.common.specifications.spec.Browser

class Anvandarroll {

    def ändraTill(String role) {
        Browser.drive {
            go System.getProperty("webcert.baseUrl") + "authtestability/roles/userrole/${role}"
            getDriver().navigate().back()
        }
    }

}
