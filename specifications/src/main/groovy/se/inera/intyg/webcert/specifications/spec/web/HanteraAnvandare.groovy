package se.inera.intyg.webcert.specifications.spec.web

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture


class HanteraAnvandare extends ExceptionHandlingFixture  {

    def uppdateraOriginTill(String origin) {
        Browser.drive {
            go System.getProperty("webcert.baseUrl") + "authtestability/user/origin/${origin}"
            getDriver().navigate().back()
        }
    }

}
