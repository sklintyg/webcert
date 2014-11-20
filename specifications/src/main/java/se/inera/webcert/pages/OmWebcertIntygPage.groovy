package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class OmWebcertIntygPage extends AbstractPage {

    static at = { doneLoading() && $("#about-webcert-intyg").isDisplayed() }

    static content = {
        webcertLink { $("#about-webcert") }
        supportLink { $("#about-support") }
        intygLink { $("#about-intyg") }
        faqLink { $("#about-faq") }
        cookiesLink { $("#about-cookies") }
    }
}
