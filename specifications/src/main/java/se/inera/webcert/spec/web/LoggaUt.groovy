package se.inera.webcert.spec.web

import se.inera.certificate.page.AbstractPage
import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.AccessDeniedPage
import se.inera.webcert.pages.IndexPage
import se.inera.webcert.pages.UnhandledQAPage
import se.inera.webcert.pages.WelcomePage

class LoggaUt {

    boolean accessDeniedSidanVisas() {
        Browser.drive {
            waitFor {
                at AccessDeniedPage
            }
        }
    }

    boolean startsidanVisas() {
        boolean result
        Browser.drive {
            result = isAt UnhandledQAPage
        }
        result
    }

    def gaTillStartsidan() {
        Browser.drive {
            go "/web/dashboard"
            waitFor {
                doneLoading()
            }
        }
    }

    boolean loginSidanVisas() {
        boolean result
        Browser.drive {
            result = isAt(IndexPage) || isAt(WelcomePage)
        }
        result
    }

    def loggaUt() {
        Browser.drive {
            page.logout()
        }
    }
}
