package se.inera.webcert.spec.web

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.AccessDeniedPage
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.UnhandledQAPage
import se.inera.webcert.pages.UnsignedIntygPage

class Features {

    void gaTillSvaraOchFraga() {
        Browser.drive {
            to UnhandledQAPage
            waitFor {
                at UnhandledQAPage
            }
        }
    }

    void gaTillSokSkrivIntyg() {
        Browser.drive {
            to SokSkrivaIntygPage
            waitFor {
                at SokSkrivaIntygPage
            }
        }
    }

    void gaTillUnsignedIntyg() {
        Browser.drive {
            to UnsignedIntygPage
            waitFor {
                at UnsignedIntygPage
            }
        }
    }

    boolean accessDeniedSidanVisas() {
        boolean result
        Browser.drive {
            result = isAt AccessDeniedPage
        }
        result
    }

    boolean fragaSvarSynsIMenyn() {
        boolean result
        Browser.drive {
            result = isAt(AbstractLoggedInPage) && page.unhandledQa.isDisplayed()
        }
        result
    }

    boolean omWebcertSynsIMenyn() {
        boolean result
        Browser.drive {
            result = isAt(AbstractLoggedInPage) && page.omWebcert.isDisplayed()
        }
        result
    }

    boolean sokSkrivIntygSynsIMenyn() {
        boolean result
        Browser.drive {
            result = isAt(AbstractLoggedInPage) && page.skrivIntyg.isDisplayed()
        }
        result
    }

    boolean ejSigneradeUtkastSynsIMenyn() {
        boolean result
        Browser.drive {
            result = isAt(AbstractLoggedInPage) && page.unsigned.isDisplayed()
        }
        result
    }

    boolean personnummerSyns() {
        boolean result
        Browser.drive {
            result = page.$('#pnr')?.isDisplayed()
        }
        result
    }

    /*
    def restUtkast() {
        Browser.drive {
            go "/api/utkast"
            waitFor {
                page.$('body').text() == 'Not available since feature is not active'
            }
        }
        true
    }

    def restModuleUtkastGet() {
        Browser.drive {
            go "/moduleapi/utkast/fk7263/webcert-fitnesse-features-1"
            waitFor {
                page.$('body').text() == 'Not available since feature is not active'
            }
        }
        true
    }
    */

}
