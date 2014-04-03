package se.inera.webcert.pages

import geb.Page

class LoginPage extends Page {
    static at = { $("#loginBtn").isDisplayed() }
}
