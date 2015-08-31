package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class PrivatlakarAvtalPage extends AbstractPage {
    static at = {
        $("#acceptTermsBtn").isDisplayed()
        $("#printTermsBtn").isDisplayed()
        $("#logoutTermsBtn").isDisplayed()
    }

    static content = {
        acceptTermsBtn { $("#acceptTermsBtn") }
        printTermsBtn { $("#printTermsBtn") }
        logoutTermsBtn { $("#logoutTermsBtn") }
    }
}
