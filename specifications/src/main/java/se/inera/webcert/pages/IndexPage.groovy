package se.inera.webcert.pages

import geb.Page

class IndexPage extends Page {

    static at = { $("#indexPage").isDisplayed() }

    static content = {
        loginBtn { $("#loginBtn") }
    }

    def startLogin() {
        loginBtn.click()
    }
}
