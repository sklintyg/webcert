package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.AccessDeniedPage
import se.inera.webcert.pages.LoginPage
import se.inera.webcert.pages.UnhandledQAPage

class LoggaUt {

    boolean accessDeniedSidanVisas() {
        Browser.drive {
            waitFor {
                at AccessDeniedPage
            }
        }
    }

    boolean startsidanVisasInte() {
        Browser.drive {
            try {
                waitFor {
                    at UnhandledQAPage
                }
                false
            } catch (WaitTimeoutException) {
                true
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
            waitFor {
                at UnhandledQAPage
            }
            page.logout()
        }
    }
}
