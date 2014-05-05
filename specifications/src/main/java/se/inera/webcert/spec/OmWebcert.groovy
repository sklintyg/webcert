package se.inera.webcert.spec

import se.inera.webcert.pages.OmWebcertSupportPage
import se.inera.webcert.pages.OmWebcertIntygPage
import se.inera.webcert.pages.OmWebcertFAQPage
import se.inera.webcert.pages.OmWebcertCookiesPage
import se.inera.webcert.pages.WelcomePage

class OmWebcert {

    def loggaInSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor {
                at WelcomePage
            }
            page.loginAs(id)
        }
    }

    def gaTillOmWebcertSupport() {
        Browser.drive {
            go "/web/dashboard#/support/about"
            waitFor {
                at OmWebcertSupportPage
            }
        }
    }

    boolean undersidanSupportVisas() {
        Browser.drive {
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
