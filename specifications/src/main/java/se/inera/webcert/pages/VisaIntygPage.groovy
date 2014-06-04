package se.inera.webcert.pages

import geb.Page

class VisaIntygPage extends Page {

    static at = { $("#viewCertAndQA").isDisplayed() }

    static content = {
        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
        copyButton { $("#copyBtn") }
        intygVy { $('#intyg-vy-laddad') }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
    }

    def copy() {
        $("#copyBtn").click()
        sleep(300)
        kopieraDialogKopieraKnapp.click()
    }
}
