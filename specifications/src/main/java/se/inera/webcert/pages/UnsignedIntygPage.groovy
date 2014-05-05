package se.inera.webcert.pages

import geb.Page

class UnsignedIntygPage extends Page {

    static at = { $("#unsigned").isDisplayed() }

    static content = {
        unitstatUnsignedIntygsBadge(required: false) { $("#stat-unitstat-unsigned-certs-count") }
        unsignedIntygTable(required: false) { $("#unsignedCertTable") }
        ingaEjSigneradeIntyg { $("#current-list-noResults-query") }
        logoutLink { $("#logoutLink") }
    }
}
