package se.inera.webcert.spec

import se.inera.webcert.pages.AccessDeniedPage
import se.inera.webcert.pages.LoginPage

class LoggaUt {

    boolean accessDeniedSidanVisas() {
        Browser.drive {
            waitFor {
                at AccessDeniedPage
            }
        }
    }

    def gaTillStartsidan() {
        Browser.drive {
            go "/web/dashboard"
        }
    }

    boolean loginSidanVisas() {
        Browser.drive {
            waitFor {
                at LoginPage
            }
        }
    }

    def loggaUt() {
        Browser.drive {
            page.logout()
        }
    }
}
