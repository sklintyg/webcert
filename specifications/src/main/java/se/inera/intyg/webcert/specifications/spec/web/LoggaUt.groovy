package se.inera.intyg.webcert.specifications.spec.web

import se.inera.intyg.common.specifications.page.AbstractPage
import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.pages.AccessDeniedPage
import se.inera.intyg.webcert.specifications.pages.IndexPage
import se.inera.intyg.webcert.specifications.pages.UnhandledQAPage
import se.inera.intyg.webcert.specifications.pages.WelcomePage
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class LoggaUt extends ExceptionHandlingFixture {

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
            waitFor {
                result = isAt(IndexPage) || isAt(WelcomePage)
            }
        }
        result
    }

    def loggaUt() {
        Browser.drive {
            page.logout()
        }
    }
}
