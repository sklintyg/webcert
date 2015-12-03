package se.inera.intyg.webcert.specifications.spec.web

import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.pages.OmWebcertCookiesPage
import se.inera.intyg.webcert.specifications.pages.OmWebcertFAQPage
import se.inera.intyg.webcert.specifications.pages.OmWebcertIntygPage
import se.inera.intyg.webcert.specifications.pages.OmWebcertPage
import se.inera.intyg.webcert.specifications.pages.OmWebcertSupportPage
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class OmWebcert extends ExceptionHandlingFixture {

    def gaTillOmWebcert() {
        Browser.drive {
            go "/web/dashboard#/webcert/about"
            waitFor {
                at OmWebcertPage
            }
        }
    }

    boolean valjSupport() {
        Browser.drive {
            waitFor {
                page.supportLink.click()
            }
            waitFor {
                at OmWebcertSupportPage
            }
        }
    }

    boolean valjIntygSomStods() {
        Browser.drive {
            waitFor {
                page.intygLink.click()
            }
            waitFor {
                at OmWebcertIntygPage
            }
        }
    }

    boolean valjVanligaFragor() {
        Browser.drive {
            waitFor {
                page.faqLink.click()
            }
            waitFor {
                at OmWebcertFAQPage
            }
        }
    }

    boolean valjOmKakor() {
        Browser.drive {
            waitFor {
                page.cookiesLink.click()
            }
            waitFor {
                at OmWebcertCookiesPage
            }
        }
    }

    boolean avtalsvillkorSynligIMenyn() {
        boolean result = false
        Browser.drive {
            result = page.ppTermsLink?.isDisplayed()
        }
        result
    }
}
