package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class AbstractViewCertPage extends AbstractLoggedInPage {
    
    static at = { doneLoading() && $("#viewCertAndQA").isDisplayed() }
    
    static content = {
        intygSaknas { $('#cert-load-error') }
        intygLaddat { $('#intyg-vy-laddad') }

        skrivUtKnapp(required: false) { $("#downloadprint") }
        skrivUtKnappEmployer(required: false){ $("#downloadprintemployer") }
        kopieraKnapp(required: false) { $("#copyBtn") }
        makuleraKnapp(required: false) { $("#makuleraBtn") }
        sekretessmarkering { $("#sekretessmarkering") }

        skickaDialogCheck { $("#patientSamtycke") }
        skickaDialogSkickaKnapp { $("#button1send-dialog") }
        skickaKnapp(required: false) { $("#sendBtn") }
        
        kopieraDialogMsgForlangningSjukskrivning(required:false) { $("#msgForlangningSjukskrivning") }

        tillbakaButton(required: false) { $("#tillbakaButton") }

        visaVadSomSaknasLista(required: false) {$("#visa-vad-som-saknas-lista")}
        
        // Kopiera
        kopieraDialogKopieraKnapp(to: AbstractEditCertPage, toWait: true) { $("#button1copy-dialog") }
        kopieraDialogAvbrytKnapp { $("#button2copy-dialog") }
        kopieraDialogVisaInteIgen { $("#dontShowAgain") }

        // makulera
        makuleraDialogMakuleraKnapp(wait: true) { $("#button1makulera-dialog") }
        makuleraConfirmationOkButton(wait: true) { $("#confirmationOkButton") }

        // messages
        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
        certificateIsSentToRecipientMessage(required: false) { $("#certificate-is-sent-to-recipient-message-text") }
        certificateIsRevokedMessage(required: false) { $("#certificate-is-revoked-message-text") }
        certificateIsOnQueueToITMessage(required: false) { $('#certificate-is-on-sendqueue-to-it-message-text') }

        // Questions and Answers
        newQuestionBtn(required: false) { $("#askQuestionBtn") }
        newQuestionForm(required: false) {$("#newQuestionForm") }
        newQuestionText { $("#newQuestionText") }
        newQuestionTopic { $("#new-question-topic") }
        sendQuestionBtn(required: false) { $("#sendQuestionBtn") }
        cancelQuestionBtn { $("#cancelQuestionBtn") }

        qaOnlyDialog(required: false) { $("#qa-only-warning-dialog") }
        qaOnlyDialogFortsatt(required: false) { $("#button1continue-dialog") }
        qaOnlyDialogCancel(required: false) { $("#button2qa-only-warning-dialog") }

        unhandledQAList { $("#unhandledQACol") }
        unhandledQAPanel(required: false) {internid -> $("#qaunhandled-${internid}")}
        unhandledQAPanelWithText(required: false) {text -> $("#unhandledQACol div", text: text)}
        
        handledQAList { $("#handledQACol") }
        handledQAPanel(required: false) {internid -> $("#qahandled-${internid}")}
        
        answerText {internid -> $("#answerText-${internid}")}
        sendAnswerBtn {internid -> $("#sendAnswerBtn-${internid}")}
        forwardBtn {internid -> $("#forwardBtn-${internid}")}
        markQuestionAsHandledBtn {internid -> $("#markAsHandledWcOriginBtn-${internid}")}
        markAnswerAsHandledBtn {internid -> $("#markAsHandledFkOriginBtn-${internid}")}
        markAsUnhandledBtn {internid -> $("#markAsUnhandledBtn-${internid}")}
        
        frageStallarNamn {internid -> $("#fraga-vard-aktor-namn-${internid}")}
        besvarareNamn {internid -> $("#svar-vard-aktor-namn-${internid}")}
        fkMeddelandeRubrik {internid -> $("#fkMeddelandeRubrik-${internid}")}
        fkKompletteringar {internid -> $("#unhandled-fkKompletteringar-${internid}")}
        fkKontakter {internid -> $("#unhandled-fkKontakter-${internid}")}
        qaFragaSkickadDatum {internid -> $("#qa-skickaddatum-${internid}")}
        qaFragetext {internid -> $("#qa-fragetext-${internid}")}
        qaSvarstext {internid -> $("#answerText-${internid}")}
    
        questionIsSentToFkMessage(required: false) { $("#question-is-sent-to-fk-message-text") }
        closeSentMessage(wait: true) { displayed($("#question-is-sent-to-fk-message-text > button")) }

        certificateRevokedMessage(required: false) { $("#certificate-is-revoked-message-text") }
        certificateIsSentToFKMessage(required: false) { $("#certificate-is-sent-to-fk-message-text")}
        certificateIsNotSentToFkMessage(required: false) { $("#certificate-is-not-sent-to-fk-message-text") }

        hanteraButton(to: UnhandledQAPage, toWait: true) { $("#button1checkhanterad-dialog-hantera") }
        ejHanteraButton(to: UnhandledQAPage, toWait: true) { $("#button1checkhanterad-dialog-ejhantera") }
        hanteraTillbakaButton { $("#button1checkhanterad-dialog-tillbaka") }

        qaCheckEjHanteradDialog { $("#qa-check-hanterad-dialog") }
        preferenceSkipShowUnhandledDialog(required: false) { $("#preferenceSkipShowUnhandledDialog") }

        modalBackdrop(required:false) {$('.modal-backdrop')}
    }

    def copy() {
        kopieraKnapp.click()
        waitFor {
            doneLoading()
        }
        kopieraDialogKopieraKnapp.click()
    }

    def openCopyDialog() {
        kopieraKnapp.click()
        waitFor {
            doneLoading()
        }
    }

    def closeCopyDialog() {
        $("#button2copy-dialog").click()
        waitFor {
            doneLoading()
        }
    }

    boolean makuleraKnappSyns() {
        makuleraKnapp?.isDisplayed()
    }

    boolean makuleraStatusSyns(){
        waitFor{
            certificateIsRevokedMessage?.isDisplayed()
        }
    }


    def makulera() {
        makuleraKnapp.click()
        waitFor {
            doneLoading()
        }
        makuleraDialogMakuleraKnapp.click()
        waitFor {
            doneLoading()
        }
    }

    def bekraftaMakulera() {
        waitFor {
            doneLoading() && makuleraConfirmationOkButton
        }
        makuleraConfirmationOkButton.click()
        waitFor {
            !modalBackdrop.isDisplayed();
        }
    }


    /**
     * Generic send, does not validate content of send dialog body text
     * @return
     */
    void send() {
        skickaKnapp.click()
        waitFor {
            doneLoading()
        }
        skickaDialogCheck.click()
        waitFor {
            doneLoading()
        }
        skickaDialogSkickaKnapp.click()
        waitFor {
            doneLoading()
        }
    }

    def tillbaka() {
        AbstractPage.scrollIntoView(tillbakaButton.attr('id'));
        tillbakaButton.click()
        waitFor {
            doneLoading()
        }
    }

    def showNewQuestionForm() {
        newQuestionBtn.click()
        waitFor {
            doneLoading()
        }
    }

    def sendQuestion() {
        sendQuestionBtn.click()
        waitFor {
            doneLoading()
        }
    }

    def cancelQuestion() {
        cancelQuestionBtn.click()
        waitFor {
            doneLoading()
        }
    }

    void addAnswerText(String internid, String answer) {
        answerText(internid) << answer
    }

    def markAnswerAsHandled(String internid) {
        markAnswerAsHandledBtn(internid).click()
        waitFor {
            doneLoading()
        }
    }

    void markQuestionAsHandled(String internid) {
        markQuestionAsHandledBtn(internid).click()
        waitFor {
            doneLoading()
        }
    }

    def markAsUnhandled(String internid) {
        markAsUnhandledBtn(internid).click()
        waitFor {
            doneLoading()
        }
    }

    void sendAnswer(String internid) {
        sendAnswerBtn(internid).click()
        waitFor {
            return doneLoading() && handledQAPanel(internid).isDisplayed()
        }
    }

    void hanteraButtonClick() {
        hanteraButton.click()
        waitFor {
            doneLoading()
        }
    }

    void ejHanteraButtonClick() {
        ejHanteraButton.click()
        waitFor {
            doneLoading()
        }
    }

    void hanteraTillbakaButtonClick() {
        hanteraTillbakaButton.click()
        waitFor {
            doneLoading()
        }
    }

    void preferenceSkipShowUnhandledCheck() {
        preferenceSkipShowUnhandledDialog.click()
        waitFor {
            doneLoading()
        }
    }

}
