package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class OmWebcertCookiesPage extends AbstractPage {

    static at = { doneLoading() && $("#about-webcert-cookies").isDisplayed() }

    static content = {
        webcertLink { $("#about-webcert") }
        termsLink { $("#about-pp-terms") }
        supportLink { $("#about-support") }
        intygLink { $("#about-intyg") }
        faqLink { $("#about-faq") }
        cookiesLink { $("#about-cookies") }
    }
}
