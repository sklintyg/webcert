package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class SokSkrivFyllINamnPage extends AbstractPage {

    static at = { doneLoading() && $("#sok-skriv-fyll-i-namn").isDisplayed() }

    static content = {
        fornamn { $("#fornamn") }
        efternamn { $("#efternamn") }
        namnFortsattKnapp { $("#namnFortsatt") }
    }
}
