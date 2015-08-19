package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class PrivatlakarAvtalPage extends AbstractPage {
    static at = {
        $("#acceptTermsBtn").isDisplayed()
        $("#declineTermsBtn").isDisplayed()
    }

    static content = {
        acceptTermsBtn { $("#acceptTermsBtn") }
        declineTermsBtn { $("#declineTermsBtn") }
    }
}
