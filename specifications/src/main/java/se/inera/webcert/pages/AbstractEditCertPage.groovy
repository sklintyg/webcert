package se.inera.webcert.pages

import geb.Module
import se.inera.certificate.page.AbstractPage
import se.inera.certificate.spec.Browser

abstract class AbstractEditCertPage extends AbstractPage {

    boolean spara() {
        Browser.drive {
            if(!AbstractPage.isButtonDisabled(sparaKnapp)){
                sparaKnapp.click()
            } else {
                // utkast är redan sparat genom autospar
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
