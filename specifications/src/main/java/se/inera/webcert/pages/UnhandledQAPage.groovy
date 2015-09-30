package se.inera.webcert.pages

import se.inera.certificate.spec.Browser

class UnhandledQAPage extends AbstractLoggedInPage {

    static url = "/web/dashboard#/unhandled-qa"
    static at = { doneLoading() && $("#unhandled-qa").isDisplayed() }

    static content = {
        unitstatUnhandledQuestionsBadgde(required: false,wait: true) { $("#stat-unitstat-unhandled-question-count") }
        careUnitSelector(required: false) { $("a#wc-care-unit-clinic-selector") }
        careUnitSelectorLink(required: false) { $("a#wc-care-unit-clinic-selector-link") }
        careUnitSelectorNoWait(required: false) { $("div#wc-care-unit-clinic-selector")}
        careUnitModal(required: false, wait: true) { displayed($("a#wc-care-unit-clinic-selector-link")) }
        careUnitModalBody(required: false, wait: true) { displayed($(".modal-body")) }
        unhandledQATable(required: false, wait: true) { displayed($("#qaTable")) }
        unhandledQATable(required: false) { $("#qaTable") }

        noResultsOnUnitInfo { $("#current-list-noResults-unit") }
        noResultsForQueryInfo(wait: true) { displayed($("#current-list-noResults-query")) }
        noResultsForQueryInfo { $("#current-list-noResults-query") }

        advancedFilterBtn { $("#show-advanced-filter-btn") }
        advancedFilterForm { $("#advanced-filter-form") }
        advandecFilterFormFragestallare { $("input", name: "frageStallare") }
        advancedFilterSelectDoctor(required: false) { $("#qp-lakareSelector") }
        advancedFilterVidarebefordrad { $("input", name: "vidarebefordrad") }
        advancedFilterChangeDateFrom(required: false) { $("#filter-changedate-from") }
        advancedFilterChangeDateTo(required: false) { $("#filter-changedate-to") }
        advancedFilterStatus { $("#qp-showStatus") }
        advancedFilterSearchBtn { $("#filter-qa-btn") }
        advancedFilterResetBtn  { $("#reset-search-form") }
        visaAllaFragaBtn(required: false) { $("#select-active-unit-wc-all") }
        vcCentrumVastBtn(required: false) { $("select-active-unit-centrum-vast") }
        fetchMoreBtn { $("#hamtaFler") }
        
        visaFragaBtn(required: false) {internReferens -> $("#showqaBtn-${internReferens}")}

        patientId() {internReferens -> $("#patientId-${internReferens}")}
    }

    void visaAllaFragor() {
        visaAllaFragaBtn.click();
        waitFor {
            doneLoading()
        }
    }

    void showQA(String internReferens) {
        visaFragaBtn(internReferens).click()
        waitFor {
            doneLoading()
        }
    }

    boolean isQAVisible(String internid) {
        visaFragaBtn(internid)?.isDisplayed()
    }

    void showAdvancedFilter() {
        advancedFilterBtn.click()
        waitFor {
            doneLoading()
        }
    }

    void resetAdvancedFilter() {
        advancedFilterResetBtn.click()
        waitFor {
            doneLoading()
        }
    }

    boolean patientIdSyns(String internReferens) {
        patientId(internReferens)?.text() != ""
    }

    void hamtaFler() {
        if (fetchMoreBtn.isDisplayed()) {
            fetchMoreBtn.click()
        }
        waitFor {
            doneLoading()
        }
    }

}
