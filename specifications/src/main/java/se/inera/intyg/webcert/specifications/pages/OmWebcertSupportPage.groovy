package se.inera.webcert.pages

class OmWebcertSupportPage extends AbstractOmWebcertPage {

    static at = { doneLoading() && $("#about-webcert-support").isDisplayed() }

}
