package se.inera.intyg.webcert.specifications.pages

class OmWebcertFAQPage extends AbstractOmWebcertPage {

    static at = { doneLoading() && $("#about-webcert-faq").isDisplayed() }

}
