package se.inera.webcert.pages

import geb.Page

class OmWebcertCookiesPage extends Page {

    static at = { $("#about-webcert-cookies").isDisplayed() }

    static content = {
        supportLink { $("#about-support") }
        intygLink { $("#about-intyg") }
        faqLink { $("#about-faq") }
        cookiesLink { $("#about-cookies") }
    }
}
