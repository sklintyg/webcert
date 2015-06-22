package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class SokSkrivaIntygPage extends AbstractPage {

    static at = { doneLoading() && $("#skapa-valj-patient").isDisplayed() }

    static content = {
        personnummer { $("#pnr") }
        personnummerFortsattKnapp { $("#skapapersonnummerfortsatt") }
        puFelmeddelande { $("#puerror") }

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
