package se.inera.webcert.pages

import se.inera.intyg.common.specifications.page.AbstractPage

class IndexPage extends AbstractPage {

    static at = { $("#indexPage").isDisplayed() }

    static content = {
        loginBtn { $("#loginBtn") }
    }

    def startLogin() {
        loginBtn.click()
    }
}
