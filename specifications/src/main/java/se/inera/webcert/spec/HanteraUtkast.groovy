package se.inera.webcert.spec

import se.inera.webcert.pages.EditeraIntygPage
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.UnsignedIntygPage
import se.inera.webcert.pages.WelcomePage

class HanteraUtkast {

    def loggaInSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor {
                at WelcomePage
            }
            page.loginAs(id)
        }
    }

    def gaTillEjSigneradeIntyg() {
        Browser.drive {
            go "/web/dashboard#/unsigned"
            waitFor {
                at UnsignedIntygPage
            }
        }
    }

    boolean ejSigneradeIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at UnsignedIntygPage
            }
        }
    }

    boolean ingaEjSigneradeIntygVisas() {
        Browser.drive {
            waitFor {
                at UnsignedIntygPage
            }
            waitFor {
                page.ingaEjSigneradeIntyg.isDisplayed()
            }
        }
    }

    def gaTillEditeraIntygMedTypOchIntygid(String typ, String intygid) {
        Browser.drive {
            go "/web/dashboard#/$typ/edit/$intygid"
        }
    }

    boolean editeraSidanVisas() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
        }
    }

    boolean raderaUtkast() {
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            waitFor {
                page.radera.click()
            }
            waitFor {
                page.konfirmeraRadera.click()
            }
        }
    }

    String utkastId() {
        def url
        Browser.drive {
            waitFor {
                at EditeraIntygPage
            }
            url = getDriver().getCurrentUrl().split('/').last()
        }
        url
    }

    boolean skapaIntygForPersonnummerMedTyp(String personnummer, String typ) {
        Browser.drive {
            at SokSkrivaIntygPage
            page.personnummer = personnummer
            page.personnummerFortsattKnapp.click()
            page.intygTyp = typ
            page.intygTypFortsatt.click()
        }
    }
}
