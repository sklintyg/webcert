package se.inera.webcert.pages

import geb.Browser
import geb.Page
import se.inera.intyg.common.specifications.page.AbstractPage

class AvtalPage extends AbstractPage {
    static at = { $(".modal-dialog").isDisplayed() }

    static content = {
        termsBody(required:false) {$(".modal-dialog")}
    }
}
