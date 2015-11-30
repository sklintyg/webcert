package se.inera.webcert.spec.web

import se.inera.intyg.common.specifications.page.AbstractPage
import se.inera.intyg.common.specifications.spec.Browser
import se.inera.webcert.pages.WelcomePage

class LoggaIn {

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
