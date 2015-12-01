package se.inera.intyg.webcert.specifications.pages

class UnsignedIntygPage extends AbstractLoggedInPage {

    static url = "/web/dashboard#/unsigned"
    static at = { doneLoading() && $("#unsigned").isDisplayed() }

    static content = {
        unitstatUnsignedIntygsBadge(required: false) { $("#stat-unitstat-unsigned-certs-count") }
        unsignedIntygTable(required: false) { $("#unsignedCertTable") }
        ingaEjSigneradeIntyg { $("#current-list-noResults-query") }

        advancedFilterBtn(wait: true) { $("#show-advanced-filter-btn") }
        advancedFilterResetBtn(wait: true) { $("#reset-search-form") }
        advancedFilterForm(wait: true) { $("#advanced-filter-form") }

        filterVidarebefordrad(required: false) { $("#filterFormVidarebefordrad") }
        filterSparatAv(required: false) { $("#filterFormSparatAv") }
        filterSigneratAv(required: false) { $("#filterFormSigneratAv") }
    }

    def showAdvancedFilter() {
        advancedFilterBtn.click()
    }

    def resetAdvancedFilter() {
        advancedFilterResetBtn.click()
    }
}
