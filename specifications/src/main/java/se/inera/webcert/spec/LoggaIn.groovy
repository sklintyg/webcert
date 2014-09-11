package se.inera.webcert.spec

import se.inera.webcert.pages.WelcomePage

class LoggaIn {

    def loggaInSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor {
                at WelcomePage
            }
            page.loginAs(id)
        }
    }
}
