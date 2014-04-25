package se.inera.webcert.pages

import geb.Page

class OmWebcertSupportPage extends Page {

    static at = { $("#loginForm").isDisplayed() }

    static content = {
        userSelect { $("#jsonSelect") }
        loginBtn { $("#loginBtn") }
    }

    def loginAs(String id) {
        userSelect = $("#${id}").value();
        loginBtn.click()
    }
}
