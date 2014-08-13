package se.inera.webcert.pages

import geb.Page

class VisaIntygPage extends Page {

    static at = { $("#viewCertAndQA").isDisplayed() }

    static content = {
        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
        certificateIsSentToRecipientMessage(required: false) { $("#certificate-is-sent-to-recipient-message-text") }
        copyButton { $("#copyBtn") }
        intygVy { $('#intyg-vy-laddad') }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
        skickaDialogCheck { $("#patientSamtycke") }
        skickaDialogSkickaKnapp { $("#button1send-dialog") }
    }

    def copy() {
        $("#copyBtn").click()
        sleep(300)
        kopieraDialogKopieraKnapp.click()
    }

    def send() {
        $("#sendBtn").click()
        sleep(1000)
        skickaDialogCheck.click()
        sleep(100)
        skickaDialogSkickaKnapp.click()
    }
}
