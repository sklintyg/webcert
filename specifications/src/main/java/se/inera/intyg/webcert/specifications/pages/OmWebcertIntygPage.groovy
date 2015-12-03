package se.inera.intyg.webcert.specifications.pages

class OmWebcertIntygPage extends AbstractOmWebcertPage {

    static at = { doneLoading() && $("#about-webcert-intyg").isDisplayed() }

}
