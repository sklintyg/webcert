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
        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
        enhetsPostadress(required: false) { $("#clinicInfo\\.postalAddress") }
        enhetsPostnummer(required: false) { $("#clinicInfo\\.postalCode") }
        enhetsPostort(required: false) { $("#clinicInfo\\.postalCity") }
        enhetsTelefonnummer(required: false) { $("#clinicInfo\\.phone") }
        enhetsEpost(required: false) { $("#clinicInfo\\.email") }
        sparaBtn(required: false) { $("#spara-utkast") }
    }
}
