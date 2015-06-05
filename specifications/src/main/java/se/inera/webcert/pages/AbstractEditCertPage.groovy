package se.inera.webcert.pages

import geb.Browser
import geb.Module
import se.inera.certificate.page.AbstractPage

abstract class AbstractEditCertPage extends AbstractPage {
    
    def visaVadSomSaknas() {
        visaVadSomSaknasKnapp.click();
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
        Browser.drive{
            return intygetSparatOchKomplettMeddelande.isDisplayed();
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
