package se.inera.intyg.webcert.specifications.pages

class SokSkrivFyllINamnPage extends AbstractLoggedInPage {

    static at = { doneLoading() && $("#sok-skriv-fyll-i-namn").isDisplayed() }

    static content = {
        fornamn { $("#fornamn") }
        efternamn { $("#efternamn") }
        namnFortsattKnapp { $("#namnFortsatt") }
    }
}
