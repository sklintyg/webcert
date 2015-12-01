package se.inera.webcert.pages

class OmWebcertIntygPage extends AbstractOmWebcertPage {

    static at = { doneLoading() && $("#about-webcert-intyg").isDisplayed() }

}
