package se.inera.webcert.pages

import geb.Page

class SokSkrivFyllINamnPage extends Page {

    static at = { $("#sok-skriv-fyll-i-namn").isDisplayed() }

    static content = {
        firstName { $("#firstname") }
        lastName { $("#lastname") }
        namnFortsattKnapp { $("#namnFortsatt") }
    }
}
