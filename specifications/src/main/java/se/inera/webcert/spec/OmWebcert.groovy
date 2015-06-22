package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.OmWebcertCookiesPage
import se.inera.webcert.pages.OmWebcertFAQPage
import se.inera.webcert.pages.OmWebcertIntygPage
import se.inera.webcert.pages.OmWebcertPage
import se.inera.webcert.pages.OmWebcertSupportPage

class OmWebcert {

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
}
