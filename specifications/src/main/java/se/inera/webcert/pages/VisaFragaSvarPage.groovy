package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage
import se.inera.certificate.spec.Browser

class VisaFragaSvarPage extends AbstractPage {

    static at = { doneLoading() && $("#viewQAAndCert").isDisplayed() }

    static content = {

        intygSaknas { $("#cert-load-error") }
        intygVy(wait: true) { displayed($('#intyg-vy-laddad')) }

        newQuestionBtn(required: false, wait: true) { displayed($("#askQuestionBtn")) }
        newQuestionBtnNoWait(required: false) { $("#askQuestionBtn") }
        newQuestionForm(required: false, wait: true) { displayed($("#newQuestionForm")) }
        newQuestionFormNoWait(required: false) {$("#newQuestionForm") }
        newQuestionText { $("#newQuestionText") }
        newQuestionTopic { $("#new-question-topic") }
        sendQuestionBtn(required: false, wait: true) { displayed($("#sendQuestionBtn")) }
        cancelQuestionBtn { $("#cancelQuestionBtn") }
        skrivUtBtn(required: false, wait: true) { displayed($("#downloadprint")) }
        kopieraBtn(required: false, wait: true) { displayed($("#copyBtn")) }
        kopieraBtnNoWait(required: false) { $("#copyBtn") }
        makuleraBtn(required: false, wait: true) { displayed($("#makuleraBtn")) }
        makuleraBtnNoWait(required: false) { $("#makuleraBtn") }
        skickaTillFkBtn(required: false, wait: true) { displayed($("#sendBtn")) }
        skickaTillFkBtnNoWait(required: false) { $("#sendBtn") }

        qaOnlyDialog(required: false, wait: true) { displayed($("#qa-only-warning-dialog")) }
        qaOnlyDialogFortsatt(required: false, wait: true) { displayed($("#button1continue-dialog")) }
        qaOnlyDialogCancel(required: false) { $("#button2qa-only-warning-dialog") }

        unhandledQAList(wait: true) { displayed($("#unhandledQACol")) }

        questionIsSentToFkMessage(required: false, wait: true) { displayed($("#question-is-sent-to-fk-message-text")) }
        questionIsSentToFkMessageNoWait(required: false) { $("#question-is-sent-to-fk-message-text") }
        closeSentMessage(wait: true) { displayed($("#question-is-sent-to-fk-message-text > button")) }

        certificateRevokedMessage(required: false, wait: true) { displayed($("#certificate-is-revoked-message-text")) }
        certificateIsSentToFKMessage(required: false, wait: true) {
            displayed($("#certificate-is-sent-to-fk-message-text"))
        }
        certificateIsSentToFKMessageNoWait(required: false) {
            $("#certificate-is-sent-to-fk-message-text")
        }
        certificateIsNotSentToFkMessage(required: false) { $("#certificate-is-not-sent-to-fk-message-text") }

        copyButton(wait: true) { displayed($("#copyBtn")) }
        makuleraButton { $("#makuleraBtn") }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
        makuleraDialogKopieraKnapp { $("#button1makulera-dialog") }
        makuleraConfirmationOkButton(wait: true) { displayed($("#confirmationOkButton")) }
        skickaDialogCheck { $("#patientSamtycke") }
        skickaDialogSkickaKnapp { $("#button1send-dialog") }

        tillbakaButton(wait: true) { displayed($("#tillbakaButton")) }

        hanteraButton(wait: true) { displayed($("#button1checkhanterad-dialog-hantera")) }
        ejHanteraButton(wait: true) { displayed($("#button1checkhanterad-dialog-ejhantera")) }
        hanteraTillbakaButton(wait: true) { displayed($("#button1checkhanterad-dialog-tillbaka")) }

        qaCheckEjHanteradDialog(wait: true) { displayed($("#qa-check-hanterad-dialog")) }

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
        Browser.drive {
            waitFor {
                $("#answerText-${internid}") << answer
            }
        }
    }

    def answerBtn(String internid) {
        Browser.drive {
            waitFor {
                $("#sendAnswerBtn-${internid}")
            }
        }
    }

    def forwardBtn(String internid) {
        Browser.drive {
            waitFor {
                $("#forwardBtn-${internid}")
            }
        }
    }

    def frageStallarNamn(String internid) {

        Browser.drive {
            waitFor {
                $("#fraga-vard-aktor-namn-${internid}")
            }
        }
    }

    def besvarareNamn(String internid) {
        Browser.drive {
            waitFor {
                $("#svar-vard-aktor-namn-${internid}")
            }
        }
    }

    def markAsHandledFkOriginBtn(String internid) {
        Browser.drive {
            waitFor {
                $("#markAsHandledFkOriginBtn-${internid}")
            }
        }
    }

    def markAsHandledWcOriginBtnClick(String id) {
        Browser.drive {
            waitFor {
                $("#markAsHandledWcOriginBtn-${id}").click()
            }
        }
    }

    boolean qaHandledPanel(String internid, boolean expected) {
        Browser.drive {
            def ref = "#qahandled-${internid}";
            if (expected) {
                waitFor {
                    $(ref).isDisplayed()
                }
            } else {
                !$(ref).isDisplayed()
            }
        }
    }

    boolean qaUnhandledPanel(String id, boolean expected) {
        Browser.drive {
            def ref = "#qaunhandled-${id}";
            if (expected) {
                waitFor {
                    $(ref).isDisplayed()
                }
            } else {
                !$(ref).isDisplayed()
            }
        }
    }

    def qaUnhandledPanelWithText(String text) {
        Browser.drive {
            waitFor {
                $("#unhandledQACol div", text: text)
            }
        }
    }

    def markAsUnhandledBtnClick(String internid, boolean expected) {
        Browser.drive {
            if (expected){
                waitFor {
                    $("#markAsUnhandledBtn-${internid}").click()
                }
            }
        }
    }

    def sendAnswer(String internid) {
        Browser.drive {
            waitFor {
                $("#sendAnswerBtn-${internid}").click()
            }
        }
    }

    def fkMeddelandeRubrik(String internid) {
        Browser.drive {
            waitFor {
                $("#fkMeddelandeRubrik-${internid}").isDisplayed()
            }
        }
    }

    def fkKompletteringar(String internid) {
        Browser.drive {
            waitFor {
                $("#unhandled-fkKompletteringar-${internid}").isDisplayed()
            }
        }
    }

    def fkKontakter(String internid) {
        Browser.drive {
            waitFor {
                $("#unhandled-fkKontakter-${internid}").isDisplayed()
            }
        }
    }

    def qaFragaSkickadDatum(String internid) {
        Browser.drive {
            waitFor {
                $("#qa-skickaddatum-${internid}")
            }
        }
    }

    def qaFragetext(String internid) {
        Browser.drive {
            waitFor {
                $("#qa-fragetext-${internid}")
            }
        }
    }

    def qaSvarstext(String internid) {
        Browser.drive {
            waitFor {
                $("#answerText-${internid}")
            }
        }
    }

    def tillbakaButtonClick() {
        Browser.drive {
            waitFor {
                $("#tillbakaButton").click()
            }
        }
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

    def preferenceSkipShowUnhandledCheck() {
        Browser.drive {
            waitFor {
                $("#preferenceSkipShowUnhandledDialog").click()
            }
        }
    }
}

