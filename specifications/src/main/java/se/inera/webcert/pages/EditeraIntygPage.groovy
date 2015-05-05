package se.inera.webcert.pages

class EditeraIntygPage extends AbstractEditCertPage {

    static at = { doneLoading() && $(".edit-form").isDisplayed() }

    static content = {
        tillbakaButton(required: false) { $("#tillbakaButton") }
        radera { $("#ta-bort-utkast") }
        skrivUtBtn { $("#skriv-ut-utkast") }
        konfirmeraRadera { $("#confirm-draft-delete-button") }
        signeraBtn(required: false) { $("#signera-utkast-button") }
        signRequiresDoctorMessage(required: false) { $("#sign-requires-doctor-message-text") }
        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
        enhetsPostadress(required: false) { $("#clinicInfoPostalAddress") }
        enhetsPostnummer(required: false) { $("#clinicInfoPostalCode") }
        enhetsPostort(required: false) { $("#clinicInfoPostalCity") }
        enhetsTelefonnummer(required: false) { $("#clinicInfoPhone") }
        enhetsEpost(required: false) { $("#clinicInfoEmail") }
        sparaBtn(required: false) { $("#spara-utkast") }
        intygetSparatMeddelande { $("#intyget-sparat-meddelande") }
        intygetEjKomplettMeddelande { $("#intyget-ej-komplett-meddelande") }
        errorPanel { $("#error-panel") }
        visaVadSomSaknasKnapp { $("#showCompleteButton") }
        visaVadSomSaknasLista(required: false) { $("#visa-vad-som-saknas-lista") }
    }
}
