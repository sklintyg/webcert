package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class UnhandledQAPage extends AbstractPage {

    static at = { doneLoading() && $("#unhandled-qa").isDisplayed() }

    static content = {
        unitstatUnhandledQuestionsBadgde(required: false) { $("#stat-unitstat-unhandled-question-count") }
        careUnitSelector(required: false) { $("div#wc-care-unit-clinic-selector") }
        careUnitModal(required: false) { $("a#wc-care-unit-clinic-selector") }
        careUnitModalBody(required: false) { $(".modal-body") }
        unhandledQATable(required: false) { $("#qaTable") }

        noResultsOnUnitInfo { $("#current-list-noResults-unit") }
        noResultsForQueryInfo { $("#current-list-noResults-query") }

        advancedFilterBtn { $("#show-advanced-filter-btn") }
        advancedFilterForm { $("#advanced-filter-form") }
        advandecFilterFormFragestallare { $("input", name: "frageStallare") }
        advancedFilterSelectDoctor { $("#qp-lakareSelector") }
        advancedFilterVidarebefordrad { $("input", name: "vidarebefordrad") }
        advancedFilterChangeDateFrom { $("#filter-changedate-from") }
        advancedFilterChangeDateTo { $("#filter-changedate-to") }
        advancedFilterStatus { $("#qp-showStatus") }
        advancedFilterSearchBtn { $("#filter-qa-btn") }
        advancedFilterResetBtn { $("#reset-search-form") }
        visaAllaFragaBtn(required: false){$("#select-active-unit-wc-all")}
        vcCentrumVastBtn(required: false){$("select-active-unit-centrum-vast")}
        fetchMoreBtn { $("#hamtaFler") }

        logoutLink { $("#logoutLink") }
    }

    def visaAllaFragor() {
        visaAllaFragaBtn.click();
    }

    def selectCareUnit(String careUnit) {
        $("#select-active-unit-${careUnit}").click()
    }

    def isCareUnitVisible(String careUnit) {
        $("#select-active-unit-${careUnit}").isDisplayed()
    }

    def clickCareUnitModal() {
        careUnitModal.click();
    }

    def expandEnhetModal(String id) {
        $("#expand-enhet-${id}").click()
    }

    def modalIsDisplayed() {
        careUnitModalBody.isDisplayed();
    }

    def isCareUnitModalVisible(String careUnit) {
        $("#select-active-unit-${careUnit}-modal").isDisplayed()
    }

    def selectCareUnitModal(String careUnit) {
        $("#select-active-unit-${careUnit}-modal").click()
    }

    boolean isNumberPresent(String careUnit, String expected) {
        expected == $("#select-active-unit-${careUnit}").find(".qa-circle").text()
    }

    boolean isNumberPresentInModal(String careUnit, String expected) {
        expected == $("#fraga-svar-stat-${careUnit}").text()
    }

    def showQA(String internReferens) {
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

    def patientIdSyns(String internReferens) {
        def patientId = $("#patientId-${internReferens}")
        patientId.text() != ""
    }

    boolean hamtaFler() {
        if (fetchMoreBtn.isDisplayed()) {
            fetchMoreBtn.click()
            sleep(1000)
        }
        return true
    }

    def logout() {
        logoutLink.click()
    }

}
