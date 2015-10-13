package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class AbstractOmWebcertPage extends AbstractLoggedInPage {

    static content = {
        webcertLink { $("#about-webcert") }
        supportLink { $("#about-support") }
        intygLink { $("#about-intyg") }
        faqLink { $("#about-faq") }
        cookiesLink { $("#about-cookies") }
    }
}
