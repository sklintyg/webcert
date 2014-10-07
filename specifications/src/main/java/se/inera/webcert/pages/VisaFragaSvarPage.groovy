package se.inera.webcert.pages

import geb.Page

class VisaFragaSvarPage extends Page {

    static at = { $("#viewQAAndCert").isDisplayed() }

    static content = {
        newQuestionBtn(required: false) { $("#askQuestionBtn") }
        newQuestionForm { $("#newQuestionForm") }
        newQuestionText { $("#newQuestionText") }
        newQuestionTopic { $("#new-question-topic") }
        sendQuestionBtn { $("#sendQuestionBtn") }
        cancelQuestionBtn { $("#cancelQuestionBtn") }

        unhandledQAList { $("#unhandledQACol") }

        questionIsSentToFkMessage(required: false) { $("#question-is-sent-to-fk-message-text") }

        certificateRevokedMessage(required: false) { $("#certificate-is-revoked-message-text") }
        certificateIsSentToFKMessage(required: false) { $("#certificate-is-sent-to-fk-message-text") }
        certificateIsNotSentToFkMessage(required: false) { $("#certificate-is-not-sent-to-fk-message-text") }

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

    def showNewQuestionForm() {
        newQuestionBtn.click()
    }

    def sendQuestion() {
        sendQuestionBtn.click()
    }

    def cancelQuestion() {
        cancelQuestionBtn.click()
    }

    def addAnswerText(String internid, String answer) {
        $("#answerText-${internid}") << answer
    }

    def answerBtn(String internid) {
        $("#sendAnswerBtn-${internid}")
    }

    def forwardBtn(String internid) {
        $("#forwardBtn-${internid}")
    }

    def frageStallarNamn(String internid) {
        $("#fraga-vard-aktor-namn-${internid}")
    }

    def besvarareNamn(String internid) {
        $("#svar-vard-aktor-namn-${internid}")
    }

    def markAsHandledFkOriginBtn(String internid) {
        $("#markAsHandledFkOriginBtn-${internid}")
    }

    def markAsHandledWcOriginBtn(String id) {
        $("#markAsHandledWcOriginBtn-${id}")
    }

    def qaHandledPanel(String internid) {
        $("#qahandled-${internid}")
    }

    def qaUnhandledPanel(String id) {
        $("#qaunhandled-${id}")
    }

    def qaUnhandledPanelWithText(String text) {
        $("#unhandledQACol div", text: text)
    }

    def markAsUnhandledBtn(String internid) {
        $("#markAsUnhandledBtn-${internid}")
    }

    def sendAnswer(String internid) {
        $("#sendAnswerBtn-${internid}").click()
    }

    def fkMeddelandeRubrik(String internid) {
        $("#fkMeddelandeRubrik-${internid}").click()
    }

    def fkKompletteringar(String internid) {
        $("#fkKompletteringar-${internid}").click()
    }

    def fkKontakter(String internid) {
        $("#fkKontakter-${internid}").click()
    }

    def qaFragaSkickadDatum(String internid) {
        $("#qa-skickaddatum-${internid}")
    }

    def qaFragetext(String internid) {
        $("#qa-fragetext-${internid}")
    }

    def qaSvarstext(String internid) {
        $("#answerText-${internid}")
    }

}
