package se.inera.intyg.webcert.specifications.pages

import se.inera.intyg.common.specifications.page.AbstractPage

class PrivatlakarAvtalPage extends AbstractPage {

    static at = { doneLoading() && $("#acceptTermsBtn").isDisplayed() && $("#printTermsBtn").isDisplayed() && $("#logoutTermsBtn").isDisplayed() }

    static content = {
        acceptTermsBtn { $("#acceptTermsBtn") }
        printTermsBtn { $("#printTermsBtn") }
        logoutTermsBtn { $("#logoutTermsBtn") }
    }
}
