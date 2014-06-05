package se.inera.webcert.pages

import geb.Page

class SokSkrivaIntygPage extends Page {

    static at = { $("#skapa-valj-patient").isDisplayed() }

    static content = {
        personnummer { $("#pnr") }
        personnummerFortsattKnapp { $("#skapapersonnummerfortsatt") }

        logoutLink { $("#logoutLink") }
    }

    def selectCareUnit(String careUnit) {
        $("#select-active-unit-${careUnit}").click()
    }

    def logout() {
        logoutLink.click()
    }
}
