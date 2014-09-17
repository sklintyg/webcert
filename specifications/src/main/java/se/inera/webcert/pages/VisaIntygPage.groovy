package se.inera.webcert.pages

import geb.Page

class VisaIntygPage extends Page {

    static at = { $("#viewCertAndQA").isDisplayed() }

    static content = {
        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
        certificateIsSentToRecipientMessage(required: false) { $("#certificate-is-sent-to-recipient-message-text") }
        certificateIsRevokedMessage(required: false) { $("#certificate-is-revoked-message-text") }
        copyButton { $("#copyBtn") }
        makuleraButton { $("#makuleraBtn") }
        intygVy { $('#intyg-vy-laddad') }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
        makuleraDialogKopieraKnapp { $("#button1makulera-dialog") }
        makuleraConfirmationOkButton { $("#confirmationOkButton") }
        skickaDialogCheck { $("#patientSamtycke") }
        skickaDialogSkickaKnapp { $("#button1send-dialog") }
    }

    def copy() {
        $("#copyBtn").click()
        sleep(300)
        kopieraDialogKopieraKnapp.click()
    }

    def makulera() {
        $("#makuleraBtn").click()
        sleep(300)
        makuleraDialogKopieraKnapp.click()
    }

    def send() {
        $("#sendBtn").click()
        sleep(1000)
        skickaDialogCheck.click()
        sleep(100)
        skickaDialogSkickaKnapp.click()
    }
}
