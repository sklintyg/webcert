package se.inera.webcert.pages

import geb.Page

class VisaIntygPage extends Page {

    static at = { $("#viewCertAndQA").isDisplayed() }

    static content = {
        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
    }
}
