package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class UnsignedIntygPage extends AbstractLoggedInPage {

    static url = "/web/dashboard#/unsigned"
    static at = { doneLoading() && $("#unsigned").isDisplayed() }

    static content = {
        unitstatUnsignedIntygsBadge(required: false) { $("#stat-unitstat-unsigned-certs-count") }
        unsignedIntygTable(required: false) { $("#unsignedCertTable") }
        ingaEjSigneradeIntyg { $("#current-list-noResults-query") }
    }
}
