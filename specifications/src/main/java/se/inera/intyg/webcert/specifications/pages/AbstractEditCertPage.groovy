/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.specifications.pages

import geb.Module
import geb.Page
import se.inera.intyg.common.specifications.spec.Browser

class AbstractEditCertPage extends AbstractLoggedInPage {

    static at = { doneLoading() && $(".edit-form").isDisplayed() }

    void onLoad(Page previousPage) {
        js.eval("window.onbeforeunload = null;");
    }

    static content = {
        namnOchPersonnummer(required: false) { $("#patientNamnPersonnummer") }

        // Knappar
        tillbakaBtn(required: false) { $("#tillbakaButton") }
        raderaBtn(required: false) { $("#ta-bort-utkast") }
        konfirmeraRaderaBtn(required: false) { $("#confirm-draft-delete-button") }
        skrivUtBtn(required: false) { $("#skriv-ut-utkast") }
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

        vidarebefordraEjHanterad(required: false) { $('#vidarebefordraEjHanterad') }
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
        tillbakaBtn.click();
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

    def tabortUtkast() {
        waitFor {
            raderaBtn.isDisplayed()
        }
        raderaBtn.click()
        waitFor {
            doneLoading()
        }
    }

    def konfirmeraTabortUtkast() {
        waitFor {
            konfirmeraRaderaBtn.isDisplayed()
        }
        konfirmeraRaderaBtn.click()
        waitFor {
            doneLoading()
        }
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
