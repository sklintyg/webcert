package se.inera.intyg.webcert.specifications.spec.web

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class Anvandarroll extends ExceptionHandlingFixture {

    def ändraTill(String role) {
        Browser.drive {
            go System.getProperty("webcert.baseUrl") + "authtestability/user/role/${role}"
            getDriver().navigate().back()
        }
    }

}
