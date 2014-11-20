package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class LoginPage extends AbstractPage {
    static at = { $("#loginBtn").isDisplayed() }
}
