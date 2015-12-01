package se.inera.intyg.webcert.specifications.pages

class OmWebcertPage extends AbstractOmWebcertPage {

    static at = { doneLoading() && $("#about-webcert-webcert").isDisplayed() }

    static content = {
        webcertLink { $("#about-webcert") }
        supportLink { $("#about-support") }
        intygLink { $("#about-intyg") }
        faqLink { $("#about-faq") }
        cookiesLink { $("#about-cookies") }
        ppTermsLink(required: false) { $('#about-pp-terms') }
    }
}
