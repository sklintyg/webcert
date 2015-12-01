package se.inera.webcert.pages

import se.inera.intyg.common.specifications.page.AbstractPage

class HeaderPage extends AbstractPage {

    static at = { $("#wcHeader").isDisplayed() }

    static content = {
        unhandledQa(required: false) { $("#menu-unhandled-qa") }
        editUserLink(required: false) { $('#editUserLink') }
        loggedInRole(required: true) { $('#logged-in-role strong') }
    }
}
