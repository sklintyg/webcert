package se.inera.webcert.pages

import geb.Browser

class EditeraIntygPage extends AbstractEditCertPage {

    static at = { doneLoading() && $(".edit-form").isDisplayed() }

    static content = {
        enhetsPostadress(required: false) { $("#clinicInfoPostalAddress") }
        enhetsPostnummer(required: false) { $("#clinicInfoPostalCode") }
        enhetsPostort(required: false) { $("#clinicInfoPostalCity") }
        enhetsTelefonnummer(required: false) { $("#clinicInfoPhone") }
        enhetsEpost(required: false) { $("#clinicInfoEmail") }
        visaVadSomSaknasListaNoWait(required: false) { $("#visa-vad-som-saknas-lista") }
    }

    boolean isSignBtnDisplayed(){
        Browser.drive {
            waitFor {
                return signeraBtn.isDisplayed()
            }
        }
    }

}
