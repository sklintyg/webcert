package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage
import se.inera.certificate.spec.Browser

class SokSkrivaIntygPage extends AbstractPage {

    static at = { doneLoading() && $("#skapa-valj-patient").isDisplayed() }

    static content = {
        personnummer(wait: true) { displayed($("#pnr")) }
        personnummerFortsattKnapp(wait: true) { displayed($("#skapapersonnummerfortsatt")) }
        puFelmeddelande(wait: true) { displayed($("#puerror")) }

        logoutLink(required: false) { $("#logoutLink") }
        sokSkrivIntygLink(required: false) { $("#menu-skrivintyg") }

        radera(required: false) { $("#ta-bort-utkast") }

        valjIntygTyp(required: false) { $("#valj-intyg-typ") }
    }

    def angePatient(String patient) {
        personnummer = patient
        personnummerFortsattKnapp.click()
        waitFor {
            doneLoading()
        }
    }

    def logout() {
        logoutLink.click()
    }

    boolean gotoSokSkrivaIntyg() {
        Browser.drive {
            waitFor {
                page.sokSkrivIntygLink.isDisplayed();
            }
            page.sokSkrivIntygLink.click()
            waitFor {
                at SokSkrivaIntygPage
            }
        }
    }

    boolean raderaUtkast() {
        Browser.drive {
            waitFor {
                page.sokSkrivIntygLink.isDisplayed();
            }
            page.sokSkrivIntygLink.click()
            waitFor {
                at SokSkrivaIntygPage
            }
//            waitFor {
//                page.radera.isDisplayed();
//            }
//            page.radera.click();
//            waitFor {
//                page.konfirmeraRadera.isDisplayed();
//            }
//            page.konfirmeraRadera.click();
        }
    }

    def selectCareUnit(String careUnit) {
        $("#select-active-unit-${careUnit}").click()
    }


}
