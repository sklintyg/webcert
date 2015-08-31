package se.inera.webcert.spec

import geb.Browser
import se.inera.webcert.pages.PrivatlakarAvtalPage
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.WelcomePage

/**
 * Created by eriklupander on 2015-08-19.
 */
class HanteraAvtal {

    def result

    boolean verifieraPaAvtalSida(boolean expected = true) {
        result = false
        Browser.drive {
            waitFor {
                at PrivatlakarAvtalPage
            }
            result = page.acceptTermsBtn.isDisplayed()
        }
        result = result == expected
    }

    def godkannAvtal() {
        Browser.drive {
            waitFor {
                at PrivatlakarAvtalPage
            }

            waitFor {
                page.acceptTermsBtn.click()
            }

            waitFor {
                at SokSkrivaIntygPage
            }
        }
    }

    def avbojAvtal() {
        Browser.drive {
            waitFor {
                at PrivatlakarAvtalPage
            }

            waitFor {
                page.logoutTermsBtn.click()
            }

            waitFor {
                at WelcomePage
            }
        }
    }
}
