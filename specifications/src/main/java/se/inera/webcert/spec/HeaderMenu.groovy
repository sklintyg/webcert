package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.*

class HeaderMenu {

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
