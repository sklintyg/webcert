package se.inera.webcert.pages

class OmWebcertCookiesPage extends AbstractOmWebcertPage {

    static at = { doneLoading() && $("#about-webcert-cookies").isDisplayed() }

}
