package se.inera.webcert.pages

import se.inera.certificate.spec.Browser

class UnhandledQAPage extends AbstractLoggedInPage {

    static url = "/web/dashboard#/unhandled-qa"
    static at = { doneLoading() && $("#unhandled-qa").isDisplayed() }

    static content = {
        unitstatUnhandledQuestionsBadgde(required: false) { $("#stat-unitstat-unhandled-question-count") }
        unhandledQATable(required: false) { $("#qaTable") }

        noResultsOnUnitInfo { $("#current-list-noResults-unit") }
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

        patientId {internReferens -> $("#patientId-${internReferens}")}
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
