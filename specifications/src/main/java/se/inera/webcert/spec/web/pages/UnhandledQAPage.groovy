package se.inera.webcert.spec.web.pages

import geb.Page

class UnhandledQAPage extends Page {

    static at = { $("#unhandled-qa").isDisplayed() }

    static content = {
        unhandledQATable(required: false) { $("#qaTable") }
        //userSelect { $("#jsonSelect") }
        //certificateType { $("#certType") }
        //sendCertificateBtn { $("#loginBtn") }
    }

    def selectCareUnit(String careunit){
        $("#select-active-unit-${careunit}").click()
    }

    def showQA(String externid) {
        $("#showqaBtn-${externid}").click()
    }
}
