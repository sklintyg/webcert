package se.inera.intyg.webcert.specifications.pages

import se.inera.intyg.common.specifications.page.AbstractPage

class AccessDeniedPage extends AbstractPage {
    static at = { $("#noAuth").isDisplayed() }
}
