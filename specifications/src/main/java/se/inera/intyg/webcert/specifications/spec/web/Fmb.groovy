package se.inera.intyg.webcert.specifications.spec.web

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class Fmb extends ExceptionHandlingFixture {

    def populeraData() {
        Browser.drive {
            go System.getProperty("webcert.baseUrl") + "authtestability/fmb/updatefmbdata"
            getDriver().navigate().back()
        }
    }

}
