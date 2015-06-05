package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class VisaPage extends AbstractPage {
    static at = { doneLoading() && $("#viewCertAndQA").isDisplayed() }

    static content = {
        intygSaknas { $('#cert-load-error') }
        intygLaddat(wait: true) { displayed($('#intyg-vy-laddad')) }
        intygLaddatNoWait { $('#intyg-vy-laddad') }

        skickaDialogCheck(wait: true) { displayed($("#patientSamtycke")) }
        skickaDialogSkickaKnapp(wait: true) { displayed($("#button1send-dialog")) }

        skickaKnapp(wait: true) { displayed($("#sendBtn")) }
        skrivUtKnapp(wait: true) { displayed($("#downloadprint")) }
        kopieraKnapp(wait: true) { displayed($("#copyBtn")) }
        makuleraKnapp(wait: true) { displayed($("#makuleraBtn")) }

        skickaKnappNoWait{$("#sendBtn") }
        skrivUtKnappNoWait{$("#downloadprint") }
        kopieraKnappNoWait{$("#copyBtn") }
        makuleraKnappNoWait{$("#makuleraBtn") }

        kopieraDialogMsgForlangningSjukskrivning(required:false,wait: true) { displayed($("#msgForlangningSjukskrivning")) }
        kopieraDialogMsgForlangningSjukskrivningNoWait(required:false) { $("#msgForlangningSjukskrivning") }

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
