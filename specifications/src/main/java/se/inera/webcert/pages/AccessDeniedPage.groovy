package se.inera.webcert.pages

import geb.Page

class AccessDeniedPage extends Page {
    static at = { $("#noAuth").isDisplayed() }
}
