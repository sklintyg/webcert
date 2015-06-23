package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class SokSkrivaIntygPage extends AbstractPage {

    static at = { doneLoading() && $("#skapa-valj-patient").isDisplayed() }

    static content = {
        personnummer(wait: true) { displayed($("#pnr")) }
        personnummerFortsattKnapp(wait: true) { displayed($("#skapapersonnummerfortsatt")) }
        puFelmeddelande(wait: true) { displayed($("#puerror")) }

        logoutLink { $("#logoutLink") }
        valjIntygTyp { $("#valj-intyg-typ")}
    }

    def selectCareUnit(String careUnit) {
        $("#select-active-unit-${careUnit}").click()
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
}
