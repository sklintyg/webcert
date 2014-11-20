package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class AccessDeniedPage extends AbstractPage {
    static at = { $("#noAuth").isDisplayed() }
}
