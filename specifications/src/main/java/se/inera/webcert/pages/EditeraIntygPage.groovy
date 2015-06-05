package se.inera.webcert.pages

import geb.Browser

class EditeraIntygPage extends AbstractEditCertPage {

    static at = { doneLoading() && $(".edit-form").isDisplayed() }

    static content = {
        tillbakaButton(required: false,wait: true) { displayed($("#tillbakaButton")) }
        radera(wait: true) { displayed($("#ta-bort-utkast")) }
        skrivUtBtn(wait: true) { displayed($("#skriv-ut-utkast")) }
        konfirmeraRadera(wait: true) { displayed($("#confirm-draft-delete-button")) }
        signeraBtn(required: false,wait: true) { displayed($("#signera-utkast-button"))}
        signeraBtnNoWait(required: false) { $("#signera-utkast-button")}
        signRequiresDoctorMessage(required: false,wait: true) { displayed($("#sign-requires-doctor-message-text")) }
        certificateIsSentToITMessage(required: false,wait: true) { displayed($("#certificate-is-sent-to-it-message-text")) }
        enhetsPostadress(required: false,wait: true) { displayed($("#clinicInfoPostalAddress")) }
        enhetsPostnummer(required: false,wait: true) { displayed($("#clinicInfoPostalCode")) }
        enhetsPostort(required: false,wait: true) { displayed($("#clinicInfoPostalCity")) }
        enhetsTelefonnummer(required: false,wait: true) { displayed($("#clinicInfoPhone")) }
        enhetsEpost(required: false,wait: true) { displayed($("#clinicInfoEmail")) }
        intygetSparatOchKomplettMeddelande(wait: true) { displayed($("#intyget-sparat-och-komplett-meddelande")) }
        intygetSparatOchEjKomplettMeddelande(wait: 20) { displayed($("#intyget-sparat-och-ej-komplett-meddelande")) }
        errorPanel(wait: true) { displayed($("#error-panel")) }
        visaVadSomSaknasKnapp(wait: true) { displayed($("#showCompleteButton")) }
        visaVadSomSaknasLista(wait: true) { displayed($("#visa-vad-som-saknas-lista")) }
        visaVadSomSaknasListaNoWait{$("#visa-vad-som-saknas-lista")}
        println('EditIntygPage content defined');
    }

    boolean isSignBtnDisplayed(){
        Browser.drive {
            waitFor {
                return signeraBtn.isDisplayed()
            }
        }
    }

}
