package se.inera.webcert.pages.fk7263

import geb.Page

class ViewCertQAPage extends Page {

    static at = { $("#viewCertAndQA").isDisplayed() }

    static content = {
        newQuestionBtn(required: false) { $("#askQuestionBtn") }
        newQuestionForm { $("#newQuestionForm") }
        newQuestionText { $("#newQuestionText") }
        newQuestionTopic { $("#new-question-topic") }
        sendQuestionBtn { $("#sendQuestionBtn") }
        cancelQuestionBtn { $("#cancelQuestionBtn") }

        unhandledQAList { $("#unhandledQACol") }

        certificateRevokedMessage(required: false) { $("#certificate-is-revoked-message-text") }
        certificateIsSentToFKMessage(required: false) { $("#certificate-is-sent-to-fk-message-text") }

        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }


        field1yes{$("#field1yes")}
        field1no{$("#field1no")}
        field2{$("#field2")}
        field3{$("#field3")}
        field4{$("#field4")}
        field4b{$("#field4b")}
        field5{$("#field5")}
        field6a{$("#field6a")}
        field6b{$("#field6b")}
        field7{$("#field7")}
        field8a{$("#field8")}
        field8b{$("#field8b")}
        field9{$("#field9")}
        field10{$("#field10")}
        field11{$("#field11")}
        field12{$("#field12")}
        field13{$("#field13")}
        field17{$("#field17")}
        field_vardperson_namn{$("#vardperson_namn")}
        field_vardperson_enhetsnamn{$("#vardperson_enhetsnamn")}
    }

    def showNewQuestionForm(){
        newQuestionBtn.click()
    }

    def sendQuestion() {
        sendQuestionBtn.click()
    }

    def cancelQuestion() {
        cancelQuestionBtn.click()
    }

    def qaUnhandledPanel(String text) {
        $("#unhandledQACol div", text: text)
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
    def markAsHandledWcOriginBtn(String id) {
        $("#markAsHandledWcOriginBtn-${id}")
    }
    def qaHandledPanel(String internid) {
        $("#qahandled-${internid}")
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

        //$("qaHandledSvarstext-#${id}")
    }
}
