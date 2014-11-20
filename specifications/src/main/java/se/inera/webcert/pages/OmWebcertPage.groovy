package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class OmWebcertPage extends AbstractPage {

    static at = { doneLoading() && $("#about-webcert-webcert").isDisplayed() }

    static content = {
        webcertLink { $("#about-webcert") }
        supportLink { $("#about-support") }
        intygLink { $("#about-intyg") }
        faqLink { $("#about-faq") }
        cookiesLink { $("#about-cookies") }
    }
}
