package se.inera.webcert.spec.web.pages

import geb.Page

class WelcomePage extends Page {

    static at = { $("#loginForm").isDisplayed() }

    static content = {
        userSelect { $("#jsonSelect") }
        //certificateType { $("#certType") }
        sendCertificateBtn { $("#loginBtn") }
    }

    def startLogin() {
        sendCertificateBtn.click()
    }
}
