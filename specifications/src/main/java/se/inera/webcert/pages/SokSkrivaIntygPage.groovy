package se.inera.webcert.pages

import geb.Page

class SokSkrivaIntygPage extends Page {

    static at = { $("#skapa-valj-patient").isDisplayed() }

    static content = {
        careUnitSelector(required: false) { $("#wc-care-unit-clinic-selector") }

        logoutLink { $("#logoutLink") }
    }

    def selectCareUnit(String careUnit) {
        $("#select-active-unit-${careUnit}").click()
    }

    def logout() {
        logoutLink.click()
    }
}
