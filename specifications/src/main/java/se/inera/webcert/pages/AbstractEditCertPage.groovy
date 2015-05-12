package se.inera.webcert.pages

import geb.Module
import se.inera.certificate.page.AbstractPage

abstract class AbstractEditCertPage extends AbstractPage {

    def spara() {
        vantaTillsUtkastetSparat();
    }
    
    def visaVadSomSaknas() {
        visaVadSomSaknasKnapp.click();
        vantaTillsUtkastetSparat();
    }
    
    def doljVadSomSaknas() {
        doljVadSomSaknasKnapp.click();
        vantaTillsUtkastetSparat();
    }

    def vantaTillsUtkastetSparat() {
        // Autospar kör med max 5 sekunders mellanrum.
        waitFor(5) {
            intygetSparatOchKomplettMeddelande.isDisplayed() || intygetSparatOchEjKomplettMeddelande.isDisplayed()
        }
    }

    def tillbaka() {
        tillbakaButton.click();
        waitFor {
            doneLoading()
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
