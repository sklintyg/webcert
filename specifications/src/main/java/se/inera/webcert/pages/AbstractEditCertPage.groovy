package se.inera.webcert.pages

import geb.Module
import se.inera.certificate.spec.Browser

class AbstractEditCertPage extends AbstractLoggedInPage {
    
    static at = { doneLoading() && $(".edit-form").isDisplayed() }

    static content = {
        namnOchPersonnummer { $("#patientNamnPersonnummer") }
        tillbakaButton(required: false) { $("#tillbakaButton") }
        radera { $("#ta-bort-utkast") }
        skrivUtBtn { $("#skriv-ut-utkast") }
        konfirmeraRadera { $("#confirm-draft-delete-button") }
        signeraBtn(required: false, to: AbstractViewCertPage, toWait: true) { $("#signera-utkast-button") }
        signRequiresDoctorMessage(required: false) { $("#sign-requires-doctor-message-text") }
        certificateIsSentToITMessage(required: false) { $("#certificate-is-sent-to-it-message-text") }
        intygetSparatOchKomplettMeddelande(required: false){ $("#intyget-sparat-och-komplett-meddelande") }
        intygetSparatOchEjKomplettMeddelande(required: false){ $("#intyget-sparat-och-ej-komplett-meddelande") }
        errorPanel { $("#error-panel") }
        visaVadSomSaknasKnapp { $("#showCompleteButton") }
        doljVadSomSaknasKnapp { $("#hideCompleteButton") }
        visaVadSomSaknasLista(required: false) { $("#visa-vad-som-saknas-lista") }
        sekretessmarkering { $("#sekretessmarkering") }
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

    static def setAutoSave(val){
        Browser.drive {
            js.setAutoSave(val);
            if(val){
                js.save(true);
            }
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

    void spara(){
        js.save()
    }

    boolean harSparat(){
        return intygetSparatOchKomplettMeddelande.isDisplayed() || intygetSparatOchEjKomplettMeddelande.isDisplayed();
    }
    
    boolean isSignBtnDisplayed(){
        signeraBtn.isDisplayed()
    }

    def setSaving(val){
        println('set saving : ' + val);
        Browser.drive {
            js.setSaving(val)
        }
    }

}

class VardenhetModule extends Module {
    static base = { $("#vardenhetForm") }
    static content = {
        postadress { $("#clinicInfoPostalAddress") }
        postnummer { $("#clinicInfoPostalCode") }
        postort { $("#clinicInfoPostalCity") }
        telefonnummer { $("#clinicInfoPhone") }
        epost { $("#clinicInfoEmail") }
    }
}
