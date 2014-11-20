package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class SokSkrivaIntygPage extends AbstractPage {

    static at = { doneLoading() && $("#skapa-valj-patient").isDisplayed() }

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
