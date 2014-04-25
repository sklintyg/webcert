package se.inera.webcert.pages

import geb.Page

class UnsignedIntygPage extends Page {

    static at = { $("#unsigned").isDisplayed() }

    static content = {
        unitstatUnsignedIntygsBadge(required: false) { $("#stat-unitstat-unsigned-certs-count") }
        unsignedIntygTable(required: false) { $("#unsignedCertTable") }

 /*       advancedFilterBtn { $("#show-advanced-filter-btn") }
        advancedFilterForm { $("#advanced-filter-form") }
        advandecFilterFormFragestallare { $("input", name: "frageStallare") }
        advancedFilterSelectDoctor { $("#qp-lakareSelector") }
        advancedFilterVidarebefordrad { $("input", name: "vidarebefordrad") }
        advancedFilterChangeDateFrom { $("#filter-changedate-from") }
        advancedFilterChangeDateTo { $("#filter-changedate-to") }
        advancedFilterStatus { $("#qp-showStatus") }
        advancedFilterSearchBtn { $("#filter-qa-btn") }
        advancedFilterResetBtn { $("#reset-search-form") }
*/
        logoutLink { $("#logoutLink") }
    }
/*
    def showIntyg(String internReferens) {
        $("#showqaBtn-${internReferens}").click()
    }

    def isQAVisible(String internid) {
        $("#showqaBtn-${internid}").isDisplayed()
    }

    def showAdvancedFilter() {
        advancedFilterBtn.click()
    }

    def resetAdvancedFilter() {
        advancedFilterResetBtn.click()
    }

    def logout() {
        logoutLink.click()
    }
    */
}
