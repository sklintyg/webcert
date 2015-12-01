package se.inera.intyg.webcert.specifications.spec
import geb.Browser
import se.inera.intyg.webcert.specifications.pages.PrivatlakarAvtalPage
import se.inera.intyg.webcert.specifications.pages.SokSkrivaIntygPage
import se.inera.intyg.webcert.specifications.pages.WelcomePage
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

/**
 * Created by eriklupander on 2015-08-19.
 */
class HanteraAvtal extends ExceptionHandlingFixture {


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
