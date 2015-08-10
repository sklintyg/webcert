package se.inera.webcert.pages

import se.inera.certificate.spec.Browser

class VisaIntygPage extends AbstractViewCertPage {

    static at = { doneLoading() && $("#viewCertAndQA").isDisplayed() }

}

