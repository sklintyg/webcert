package se.inera.intyg.webcert.specifications.pages

class OmWebcertCookiesPage extends AbstractOmWebcertPage {

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
