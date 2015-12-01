package se.inera.webcert.spec

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.webcert.pages.*
import se.inera.webcert.spec.util.screenshot.ExceptionHandlingFixture

class HeaderMenu extends ExceptionHandlingFixture {

    def klickaPaFragaSvar() {
        Browser.drive {
            waitFor {
                at HeaderPage
            }
            waitFor {
                page.unhandledQa().click()
            }
            waitFor {
                at UnhandledQAPage
            }
        }
    }

    boolean redigeraAnvandareVisas() {
        def result
        Browser.drive {
            waitFor {
                at HeaderPage
            }
            result = page.editUserLink()?.isDisplayed()
        }
        result
    }

    boolean anvandarensRollVisas(String rollNamn) {
        def result
        Browser.drive {
            waitFor {
                at HeaderPage
            }
            result = page.loggedInRole().text().contains(rollNamn)
        }
        result
    }
}
