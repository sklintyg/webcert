package se.inera.webcert.pages

class OmWebcertPage extends AbstractOmWebcertPage {

    static at = { doneLoading() && $("#about-webcert-webcert").isDisplayed() }

}
