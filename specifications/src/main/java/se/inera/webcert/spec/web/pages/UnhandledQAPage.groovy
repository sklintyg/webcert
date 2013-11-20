package se.inera.webcert.spec.web.pages

import geb.Page

class UnhandledQAPage extends Page {

    static at = { $("#unhandled-qa").isDisplayed() }

    static content = {
        unhandledQATable(required: false) { $("#qaTable") }
        careUnitSelector(required: false) { $("#wc-care-unit-clinic-selector") }
        unitstatUnhandledQuestionsBadgde(required: false) { $("#stat-unitstat-unhandled-question-count") }
        advancedFilterForm { $("#advanced-filter-form") }
        advancedFilterSelectDoctor { $("#qp-doctorSelector") }
        filterBtn { $("#filter-qa-btn") }
        replyBy { $("#filter-reply-by") }
        resetSearchForm { $("#reset-search-form") }
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

    def resetCookie(){
        return $("#reset-search-form").click()
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
