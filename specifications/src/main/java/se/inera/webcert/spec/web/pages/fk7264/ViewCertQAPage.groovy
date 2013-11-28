package se.inera.webcert.spec.web.pages.fk7264

import geb.Page

class ViewCertQAPage extends Page {

    static at = { $("#viewCertAndQA").isDisplayed() }

    static content = {
        unhandledQAList(required: false) { $("#unhandledQACol") }
        askQuestionBtn(required: false) { $("#askQuestionBtn") }
        newQuestionForm { $("#newQuestionForm") }
        newQuestionText { $("#newQuestionText") }
        sendQuestionBtn { $("#sendQuestionBtn") }
        cancelQuestionBtn { $("#cancelQuestionBtn") }
        selectSubjectListbox { $("#new-question-topic") }
    }

    def addAnswerText(String internid, String answer){
        $("#answerText-${internid}")<< answer
    }

    def answerBtn(String internid) {
        $("#sendAnswerBtn-${internid}")
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

    def selectSubject(String amne) {
        selectSubjectListbox = amne
    }
    def sendQuestion() {
        sendQuestionBtn.click()
    }

    def cancelQuestion() {
        cancelQuestionBtn.click()
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
}
