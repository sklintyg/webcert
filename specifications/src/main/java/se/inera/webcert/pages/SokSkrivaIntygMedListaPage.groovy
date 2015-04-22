package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class SokSkrivaIntygMedListaPage extends AbstractPage {

    static at = { doneLoading() && $("#valj-intyg-typ").isDisplayed() }

    static content = {

        valjIntygTyp { $("#valj-intyg-typ")}
    }
}
