package se.inera.intyg.webcert.specifications.spec.web

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

/**
 * Created by mango on 14/12/15.
 */
class HanteraAnvandare extends ExceptionHandlingFixture  {

    def uppdateraOriginTill(String origin) {
        Browser.drive {
            go System.getProperty("webcert.baseUrl") + "testability/anvandare/origin/${origin}"
            getDriver().navigate().back()
        }
    }

}
