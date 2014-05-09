package se.inera.webcert.pages

import geb.Page

class EditeraIntygPage extends Page {

    static at = { $(".edit-form").isDisplayed() }

    static content = {
        radera { $("#ta-bort-utkast") }
        konfirmeraRadera { $("#confirm-draft-delete-button") }
        signeraBtn(required: false) { $("#signera-utkast-button") }
        konfirmeraSignera { $("#confirm-signera-utkast-button") }
        signRequiresDoctorMessage(required: false) { $("#sign-requires-doctor-message-text") }
        certificateSignedMessage(required: false) { $("#certificate-is-signed-message-text") }
    }
}
