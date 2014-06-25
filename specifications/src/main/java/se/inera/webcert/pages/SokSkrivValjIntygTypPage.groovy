package se.inera.webcert.pages

import geb.Page

class SokSkrivValjIntygTypPage extends Page {

    static at = { $("#valj-intyg-typ").isDisplayed() }

    static content = {
        intygtypFortsattKnapp { $("#intygTypeFortsatt") }
        intygTyp { $("#intygType") }
        intygLista { $("#intygLista") }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
    }

    def copyBtn(String intygId) {
        $("#copyBtn-${intygId}")
    }

    def copy(String intygId) {
        copyBtn(intygId).click()
        sleep(1000)
        kopieraDialogKopieraKnapp.click()
    }

    def show(String intygId) {
        $("#showBtn-${intygId}").click()
    }
}
