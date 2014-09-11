package se.inera.webcert.pages

import geb.Page

class SokSkrivValjIntygTypPage extends Page {

    static at = { $("#valj-intyg-typ").isDisplayed() }

    static content = {
        patientNamn { $("#patientNamn") }
        intygtypFortsattKnapp { $("#intygTypeFortsatt") }
        intygTyp { $("#intygType") }
        intygLista { $("#intygLista") }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
        fortsattKnapp { $("#intygTypeFortsatt") }
    }

    def copyBtn(String intygId) {
        $("#copyBtn-${intygId}")
    }

    def copy(String intygId) {
        copyBtn(intygId).click()
        sleep(500)
        kopieraDialogKopieraKnapp.click()
    }

    def show(String intygId) {
        $("#showBtn-${intygId}").click()
    }

    def valjIntygstypFk7263() {
        intygTyp.value("1")
    }
    def valjIntygstypTsBas() {
        intygTyp.value("2")
    }
    def valjIntygstypTsDiabetes() {
        intygTyp.value("3")
    }
}
