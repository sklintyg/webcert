package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class VisaFragaSvarPage extends AbstractPage {

    static at = { doneLoading() && $("#viewQAAndCert").isDisplayed() }

    static content = {

        intygSaknas { $("#cert-load-error") }
        intygVy { $('#intyg-vy-laddad') }

        newQuestionBtn(required: false) { $("#askQuestionBtn") }
        newQuestionForm { $("#newQuestionForm") }
        newQuestionText { $("#newQuestionText") }
        newQuestionTopic { $("#new-question-topic") }
        sendQuestionBtn { $("#sendQuestionBtn") }
        cancelQuestionBtn { $("#cancelQuestionBtn") }
        skrivUtBtn(required: false) { $("#downloadprint") }
        kopieraBtn(required: false){ $("#copyBtn") }
        makuleraBtn(required: false){ $("#makuleraBtn") }
        skickaTillFkBtn(required: false){ $("#sendBtn") }

        qaOnlyDialog(required: false) { $("#qa-only-warning-dialog") }
        qaOnlyDialogFortsatt(required: false) { $("#button1continue-dialog") }
        qaOnlyDialogCancel(required: false) { $("#button2qa-only-warning-dialog") }

        unhandledQAList { $("#unhandledQACol") }

        questionIsSentToFkMessage(required: false) { $("#question-is-sent-to-fk-message-text") }
        closeSentMessage { $("#question-is-sent-to-fk-message-text > button") }

        certificateRevokedMessage(required: false) { $("#certificate-is-revoked-message-text") }
        certificateIsSentToFKMessage(required: false) { $("#certificate-is-sent-to-fk-message-text") }
        certificateIsNotSentToFkMessage(required: false) { $("#certificate-is-not-sent-to-fk-message-text") }

        copyButton { $("#copyBtn") }
        makuleraButton { $("#makuleraBtn") }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
        makuleraDialogKopieraKnapp { $("#button1makulera-dialog") }
        makuleraConfirmationOkButton { $("#confirmationOkButton") }
        skickaDialogCheck { $("#patientSamtycke") }
        skickaDialogSkickaKnapp { $("#button1send-dialog") }

        tillbakaButton { $("#tillbakaButton") }

        hanteraButton { $("#button1checkhanterad-dialog-hantera") }
        ejHanteraButton { $("#button1checkhanterad-dialog-ejhantera") }
        hanteraTillbakaButton { $("#button1checkhanterad-dialog-tillbaka") }

        qaCheckEjHanteradDialog {$("#qa-check-hanterad-dialog")}

    }

    def copy() {
        $("#copyBtn").click()
        waitFor {
            doneLoading()
        }
        kopieraDialogKopieraKnapp.click()
    }

    def makulera() {
        $("#makuleraBtn").click()
        waitFor {
            doneLoading()
        }
        makuleraDialogKopieraKnapp.click()
    }

    def send() {
        $("#sendBtn").click()
        waitFor {
            doneLoading()
        }
        skickaDialogCheck.click()
        waitFor {
            doneLoading()
        }
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
        $("#unhandled-fkKompletteringar-${internid}").click()
    }

    def fkKontakter(String internid) {
        $("#unhandled-fkKontakter-${internid}").click()
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

    def tillbakaButtonClick() {
        $("#tillbakaButton").click()
    }

    def hanteraButtonClick() {
        hanteraButton.click()
    }

    def ejHanteraButtonClick() {
        ejHanteraButton.click()
    }

    def hanteraTillbakaButtonClick() {
        hanteraTillbakaButton.click()
    }

    def preferenceSkipShowUnhandledCheck(){
        $("#preferenceSkipShowUnhandledDialog").click()
    }
}
