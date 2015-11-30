package se.inera.webcert.pages

import se.inera.intyg.common.specifications.spec.Browser

class VisaIntygPage extends AbstractViewCertPage {

    static at = { doneLoading() && $("#viewCertAndQA").isDisplayed() }

}

