package se.inera.webcert.spec

import se.inera.certificate.page.AbstractPage
import se.inera.certificate.spec.Browser
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
