package se.inera.webcert.pages

class SokSkrivaIntygPage extends AbstractLoggedInPage {

    static url = "/web/dashboard#/create/choose-patient/index"

    static at = { doneLoading() && $("#skapa-valj-patient").isDisplayed() }

    static content = {
        personnummer { $("#pnr") }
        personnummerFortsattKnapp { $("#skapapersonnummerfortsatt") }
        puFelmeddelande { $("#puerror") }

        sokSkrivIntygLink(required: false) { $("#menu-skrivintyg") }

        valjIntygTyp(required: false) { $("#valj-intyg-typ") }
    }

    def angePatient(String patient) {
        personnummer = patient
        personnummerFortsattKnapp.click()
        waitFor {
            doneLoading()
        }
    }

}
