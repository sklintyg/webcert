package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class WelcomePage extends AbstractPage {

    static at = { $("#loginForm").isDisplayed() }

    static content = {
        userSelect { $("#jsonSelect") }
        loginBtn(wait:true, to: [AvtalPage, SokSkrivaIntygPage, UnhandledQAPage, AccessDeniedPage]) { $("#loginBtn") }
    }

    def loginAs(String id) {
        userSelect = $("#${id}").value();
        loginBtn.click()
    }
}
