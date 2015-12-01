package se.inera.intyg.webcert.specifications.pages

class OmWebcertSupportPage extends AbstractOmWebcertPage {

    static at = { doneLoading() && $("#about-webcert-support").isDisplayed() }

}
