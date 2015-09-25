package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class UnsignedIntygPage extends AbstractLoggedInPage {

    static url = "/web/dashboard#/unsigned"
    static at = { doneLoading() && $("#unsigned").isDisplayed() }

    static content = {
        unitstatUnsignedIntygsBadge(required: false) { $("#stat-unitstat-unsigned-certs-count") }
        unsignedIntygTable(required: false) { $("#unsignedCertTable") }
        ingaEjSigneradeIntyg { $("#current-list-noResults-query") }
        logoutLink { $("#logoutLink") }

        advancedFilterBtn(wait: true) { displayed($("#show-advanced-filter-btn")) }
        advancedFilterResetBtn(wait: true) { displayed($("#reset-search-form")) }
        advancedFilterForm(wait: true) { displayed($("#advanced-filter-form")) }
    }

    def showAdvancedFilter() {
        advancedFilterBtn.click()
    }

    def resetAdvancedFilter() {
        advancedFilterResetBtn.click()
    }
}
