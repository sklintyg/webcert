package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class IndexPage extends AbstractPage {

    static at = { $("#indexPage").isDisplayed() }

    static content = {
        loginBtn { $("#loginBtn") }
    }

    def startLogin() {
        loginBtn.click()
    }
}
