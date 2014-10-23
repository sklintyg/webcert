package se.inera.webcert.pages

import geb.Page

class OmWebcertIntygPage extends Page {

    static at = { $("#about-webcert-intyg").isDisplayed() }

    static content = {
        webcertLink { $("#about-webcert") }
        supportLink { $("#about-support") }
        intygLink { $("#about-intyg") }
        faqLink { $("#about-faq") }
        cookiesLink { $("#about-cookies") }
    }
}
