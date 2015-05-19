package se.inera.webcert.pages

import geb.Module
import se.inera.certificate.page.AbstractPage

abstract class AbstractEditCertPage extends AbstractPage {

    def spara() {
        println("------ spara!")
        waitFor {
            doneLoading()
        }
        if (sparaKnapp.isEnabled()) {
            println("about to click spara!")
            try {
                sparaKnapp.click()
            } catch(all){
                println('auto save happened real fast, button disabled and hence unclickable!')
            }
            waitFor {
                doneLoading()
            }
        } else {
            println("auto save happened! spara button was disabled...")
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
