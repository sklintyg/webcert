package se.inera.webcert.pages

import se.inera.certificate.spec.Browser

class VisaFragaSvarPage extends AbstractViewCertPage {

    static at = { doneLoading() && $("#viewQAAndCert").isDisplayed() }

}

