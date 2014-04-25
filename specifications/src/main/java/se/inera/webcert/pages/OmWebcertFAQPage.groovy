package se.inera.webcert.pages

import geb.Page

class OmWebcertFAQPage extends Page {

    static at = { $("#about-webcert-faq").isDisplayed() }

    static content = {
        supportLink { $("#about-support") }
        intygLink { $("#about-intyg") }
        faqLink { $("#about-faq") }
        cookiesLink { $("#about-cookies") }
    }
}
