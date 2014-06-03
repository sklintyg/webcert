package se.inera.webcert.pages

import geb.Page

class SokSkrivValjIntygTypPage extends Page {

    static at = { $("#valj-intyg-typ").isDisplayed() }

    static content = {
        intygtypFortsattKnapp { $("#intygTypeFortsatt") }
        intygTyp { $("#intygType") }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
    }

    def copy(String intygId) {
        $("#copyBtn-${intygId}").click()
        sleep(300)
        kopieraDialogKopieraKnapp.click()
    }
}
