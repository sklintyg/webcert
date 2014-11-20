package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class OmWebcertFAQPage extends AbstractPage {

    static at = { doneLoading() && $("#about-webcert-faq").isDisplayed() }

    static content = {
        webcertLink { $("#about-webcert") }
        supportLink { $("#about-support") }
        intygLink { $("#about-intyg") }
        faqLink { $("#about-faq") }
        cookiesLink { $("#about-cookies") }
    }
}
