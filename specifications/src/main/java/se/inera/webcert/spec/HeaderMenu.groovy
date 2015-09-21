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

    boolean redigeraAnvandareVisas(boolean expected) {
        boolean result = false
        Browser.drive {
            waitFor {
                at HeaderPage
            }
            result = page.editUserLink().isDisplayed()

        }
        return expected == result
    }
}
