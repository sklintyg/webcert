package se.inera.intyg.webcert.specifications.spec.web

import se.inera.intyg.common.specifications.page.AbstractPage
import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.pages.WelcomePage
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class LoggaIn extends ExceptionHandlingFixture {

    def loggaInSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor {
                at WelcomePage
            }
            page.loginAs(id)
            waitFor {
                AbstractPage.doneLoading()
            }
        }
    }
}
