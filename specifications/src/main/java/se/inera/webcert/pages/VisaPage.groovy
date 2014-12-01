package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class VisaPage extends AbstractPage {
    static at = { js.doneLoading && $("#viewCertAndQA").isDisplayed() }

    static content = {
        intygSaknas { $('#cert-load-error') }
        intygLaddat { $('#intyg-vy-laddad') }
    }

    boolean intygLaddat(boolean expected) {
        waitFor {
            doneLoading()
            intygLaddat.isDisplayed() == expected
            intygSaknas.isDisplayed() != expected
        }
    }
}
