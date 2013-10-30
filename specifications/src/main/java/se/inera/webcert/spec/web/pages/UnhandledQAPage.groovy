package se.inera.webcert.spec.web.pages

import geb.Page

class UnhandledQAPage extends Page {

    static at = { $("#unhandled-qa").isDisplayed() }

    static content = {
        unhandledQATable(required: false) { $("#qaTable") }
        advancedFilterForm { $("#advanced-filter-form") }
        filterBtn { $("#filter-qa-btn") }

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

    def isQAVisible(String internid) {
        $("#showqaBtn-${internid}").isDisplayed()
    }

    def showAdvancedFilter(){
        $("#show-advanced-filter-btn").click()
    }

    def selectFragestallareFK(){
        $("#frageStallareFK").click()
    }
    def selectFragestallareWC(){
        $("#frageStallareWC").click()
    }
    def selectFragestallareAlla(){
        $("#frageStallareAlla").click()
    }

}
