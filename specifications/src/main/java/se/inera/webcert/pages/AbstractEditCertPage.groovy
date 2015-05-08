package se.inera.webcert.pages

import geb.Module
import se.inera.certificate.page.AbstractPage

abstract class AbstractEditCertPage extends AbstractPage {

    def spara() {
        waitFor {
            doneLoading()
        }
        if (sparaKnapp.isEnabled()) {
            sparaKnapp.click()
            waitFor {
                doneLoading()
            }
        }
    }
    
    def visaVadSomSaknas() {
        visaVadSomSaknasKnapp.click();
        waitFor {
            doneLoading()
        }
    }
    
    def doljVadSomSaknas() {
        doljVadSomSaknasKnapp.click();
        waitFor {
            doneLoading()
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
