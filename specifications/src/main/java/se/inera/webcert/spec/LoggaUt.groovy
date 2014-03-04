package se.inera.webcert.spec

import se.inera.webcert.pages.LoginPage

class LoggaUt {

    def gåTillStartsidan() {
        Browser.drive {
            go "/web/dashboard"
        }
    }

    def loggaUt() {
        Browser.drive {
            page.logout()
        }
    }

    boolean loginsidanVisas() {
        Browser.drive {
            waitFor {
                at LoginPage
            }
        }
    }
}
