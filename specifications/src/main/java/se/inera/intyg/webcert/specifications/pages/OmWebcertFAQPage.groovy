package se.inera.webcert.pages

class OmWebcertFAQPage extends AbstractOmWebcertPage {

    static at = { doneLoading() && $("#about-webcert-faq").isDisplayed() }

}
