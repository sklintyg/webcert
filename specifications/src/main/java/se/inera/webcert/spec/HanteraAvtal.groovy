package se.inera.webcert.spec
import geb.Browser
import se.inera.webcert.pages.PrivatlakarAvtalPage
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.WelcomePage

/**
 * Created by eriklupander on 2015-08-19.
 */
class HanteraAvtal {


    boolean avtalsidaVisas() {
        def result = false
        Browser.drive {
            waitFor {
                at PrivatlakarAvtalPage
            }
            result = page.acceptTermsBtn.isDisplayed()
        }
        result
    }

    void godkannAvtal() {
        Browser.drive {
            waitFor {
                isAt PrivatlakarAvtalPage
            }
            page.acceptTermsBtn.click()
            waitFor {
                at SokSkrivaIntygPage
            }
        }
    }

    void avbojAvtal() {
        Browser.drive {
            waitFor {
                isAt PrivatlakarAvtalPage
            }
            page.logoutTermsBtn.click()
            waitFor {
                at WelcomePage
            }
        }
    }
}
