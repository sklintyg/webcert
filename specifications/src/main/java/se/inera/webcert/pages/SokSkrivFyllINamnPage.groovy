package se.inera.webcert.pages

import geb.Page

class SokSkrivFyllINamnPage extends Page {

    static at = { $("#sok-skriv-fyll-i-namn").isDisplayed() }

    static content = {
        fornamn { $("#fornamn") }
        efternamn { $("#efternamn") }
        namnFortsattKnapp { $("#namnFortsatt") }
    }
}
