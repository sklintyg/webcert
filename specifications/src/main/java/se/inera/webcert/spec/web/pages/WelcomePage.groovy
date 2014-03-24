package se.inera.webcert.spec.web.pages

import geb.Page

class WelcomePage extends Page {

    static at = { $("#loginForm").isDisplayed() }

    static content = {
        userSelect { $("#jsonSelect") }
        sendCertificateBtn { $("#loginBtn") }
    }

    def loginAs(String id) {
        userSelect = $("#${id}").value();
        sendCertificateBtn.click()
    }
}
