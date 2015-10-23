package se.inera.webcert.pages

import geb.Module
import se.inera.certificate.spec.Browser

class AbstractEditCertPage extends AbstractLoggedInPage {
    
    static at = { doneLoading() && $(".edit-form").isDisplayed() }

    static content = {
        namnOchPersonnummer(required: false) { $("#patientNamnPersonnummer") }
        tillbakaButton(required: false) { $("#tillbakaButton") }
        radera(required: false) { $("#ta-bort-utkast") }
        skrivUtBtn(required: false) { $("#skriv-ut-utkast") }
        konfirmeraRadera(required: false) { $("#confirm-draft-delete-button") }
        signeraBtn(required: false ) { $("#signera-utkast-button") }
        signRequiresDoctorMessage(required: false) { $("#sign-requires-doctor-message-text") }
        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }

        intygetSparatOchKomplettMeddelande(required: false){ $("#intyget-sparat-och-komplett-meddelande") }
        intygetSparatOchEjKomplettMeddelande(required: false){ $("#intyget-sparat-och-ej-komplett-meddelande") }

        errorPanel(required: false) { $("#error-panel") }
        visaVadSomSaknasKnapp(required: false) { $("#showCompleteButton") }
        doljVadSomSaknasKnapp(required: false) { $("#hideCompleteButton") }
        visaVadSomSaknasLista(required: false) { $("#visa-vad-som-saknas-lista") }
        sekretessmarkering(required: false) { $("#sekretessmarkering") }
        vardenhet { module VardenhetModule }
    }

    static boolean doneSaving() {
        boolean result
        Browser.drive {
            waitFor {
                // if saving then we're in a asynch request and we need to wait until it's false.
                result = js.saving;
                return !result;
            }
        }
        result
    }

    def setAutoSave(val){
        Browser.drive {
            js.setAutoSave(val);
            if(val){
                js.save(true);
            }
        }
    }

    void spara(){
        Browser.drive {
            js.save()
        }
    }

    def setSaving(val){
        println('set saving : ' + val);
        Browser.drive {
            js.setSaving(val)
        }
    }

    def visaVadSomSaknas() {
        visaVadSomSaknasKnapp.click();
        waitFor {
            visaVadSomSaknasLista.isDisplayed();
        }
    }

    def doljVadSomSaknas() {
        doljVadSomSaknasKnapp.click();
    }

    def tillbaka() {
        tillbakaButton.click();
        waitFor {
            doneLoading()
        }
    }

    boolean harSparat(){
        return intygetSparatOchKomplettMeddelande.isDisplayed() || intygetSparatOchEjKomplettMeddelande.isDisplayed();
    }
    
    boolean isSignBtnDisplayed(){
        signeraBtn.isDisplayed()
    }

}

class VardenhetModule extends Module {
    static base = { $("#vardenhetForm") }
    static content = {
        postadress(required: false) { $("#clinicInfoPostalAddress") }
        postnummer(required: false) { $("#clinicInfoPostalCode") }
        postort(required: false) { $("#clinicInfoPostalCity") }
        telefonnummer(required: false) { $("#clinicInfoPhone") }
        epost(required: false) { $("#clinicInfoEmail") }
    }
}
