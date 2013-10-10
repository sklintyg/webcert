package se.inera.webcert.spec.web.pages.fk7264

import geb.Page

class ViewCertQAPage extends Page {

    static at = { $("#viewCertAndQA").isDisplayed() }

    static content = {
        unhandledQAList(required: false) { $("#unhandledQACol") }
        //userSelect { $("#jsonSelect") }
        //certificateType { $("#certType") }
        //sendAnswerBtn { $("#sendAnswerBtn") }
    }

    def addAnswerText(String internid, String answer){
        $("#answerText-${internid}")<< answer
    }

    def answerBtn(String internid) {
        $("#sendAnswerBtn-${internid}")
    }

    def sendAnswer(String internid) {
        $("#sendAnswerBtn-${internid}").click()
    }
}
