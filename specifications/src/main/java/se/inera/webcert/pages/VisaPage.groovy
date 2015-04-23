package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class VisaPage extends AbstractPage {
    static at = { js.doneLoading && $("#viewCertAndQA").isDisplayed() }

    static content = {
        intygSaknas { $('#cert-load-error') }
        intygLaddat { $('#intyg-vy-laddad') }

        skickaKnapp { $("#sendBtn") }
        skickaDialogCheck { $("#patientSamtycke") }
        skickaDialogSkickaKnapp { $("#button1send-dialog") }

        skrivUtKnapp {$("#downloadprint") }
        kopieraKnapp { $("#copyBtn") }
        makuleraKnapp { $("#makuleraBtn") }

        kopieraDialogMsgForlangningSjukskrivning(required:false) { $("#msgForlangningSjukskrivning") }

    }

    boolean intygLaddat(boolean expected) {
        waitFor {
            doneLoading()
            intygLaddat.isDisplayed() == expected
            intygSaknas.isDisplayed() != expected
        }
    }

    /**
     * Generic send, does not validate content of send dialog body text
     * @return
     */
    def send() {
        skickaKnapp.click()
        waitFor {
            doneLoading()
        }
        skickaDialogCheck.click()
        waitFor {
            doneLoading()
        }
        skickaDialogSkickaKnapp.click()
    }
}
