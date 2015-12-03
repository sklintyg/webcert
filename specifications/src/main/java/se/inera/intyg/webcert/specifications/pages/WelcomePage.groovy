package se.inera.intyg.webcert.specifications.pages

import se.inera.intyg.common.specifications.page.AbstractPage

class WelcomePage extends AbstractPage {

    static at = { $("#loginForm").isDisplayed() }

    static content = {
        userSelect { $("#jsonSelect") }
        loginBtn(wait: true, to: [SokSkrivaIntygPage, UnhandledQAPage, AccessDeniedPage, AvtalPage]) { $("#loginBtn") }
    }

    def loginAs(String id) {
        userSelect = $("#${id}").value();
        loginBtn.click()
    }
}
