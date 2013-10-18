package se.inera.webcert.spec.web.pages.fk7264

import geb.Page

class ViewCertQAPage extends Page {

    static at = { $("#viewCertAndQA").isDisplayed() }

    static content = {
        unhandledQAList(required: false) { $("#unhandledQACol") }
        askQuestionBtn { $("#askQuestionBtn") }
        newQuestionForm { $("#newQuestionForm") }
        newQuestionText { $("#newQuestionText") }
        sendQuestionBtn { $("#sendQuestionBtn") }
        cancelQuestionBtn { $("#cancelQuestionBtn") }

    }

    def addAnswerText(String internid, String answer){
        $("#answerText-${internid}")<< answer
    }

    def answerBtn(String internid) {
        $("#sendAnswerBtn-${internid}")
    }
    def markAsHandledFkOriginBtn(String internid) {
        $("#markAsHandledFkOriginBtn-${internid}")
    }
    def markAsHandledWcOriginBtn(String internid) {
        $("#markAsHandledWcOriginBtn-${internid}")
    }
    def qaHandledPanel(String internid) {
        $("#qahandled-${internid}")
    }
    def qaUnhandledPanel(String internid) {
        $("#qaunhandled-${internid}")
    }

    def markAsUnhandledBtn(String internid) {
        $("#markAsUnhandledBtn-${internid}")
    }

    def sendAnswer(String internid) {
        $("#sendAnswerBtn-${internid}").click()
    }

    def initQuestion(){
        $("#askQuestionBtn").click()
    }

    def addQuestionText( String question){
        newQuestionText<< question
    }

    def sendQuestion() {
       sendQuestionBtn.click()
    }

    def cancelQuestion() {
        cancelQuestionBtn.click()
    }
}
