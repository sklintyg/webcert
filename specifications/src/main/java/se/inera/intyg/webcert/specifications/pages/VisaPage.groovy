package se.inera.intyg.webcert.specifications.pages

import se.inera.intyg.common.specifications.page.AbstractPage

class VisaPage extends AbstractPage {

    static at = { doneLoading() && $("#viewCertAndQA").isDisplayed() }

    static content = {
        intygSaknas { $('#cert-load-error') }
        intygLaddat(required: false) { $('#intyg-vy-laddad') }
        intygLaddatNoWait { $('#intyg-vy-laddad') }

        skickaDialogCheck(required: false) { $("#patientSamtycke") }
        skickaDialogSkickaKnapp(required: false) { $("#button1send-dialog") }

        skickaKnapp(required: false) { $("#sendBtn") }
        skrivUtKnapp(required: false) { $("#downloadprint") }
        skrivUtKnappEmployer(required: false){ $("#downloadprintemployer") }
        kopieraKnapp(required: false) { $("#copyBtn") }
        makuleraKnapp(required: false) { $("#makuleraBtn") }
        sekretessmarkering { $("#sekretessmarkering") }

        skickaKnappNoWait { $("#sendBtn") }
        skrivUtKnappNoWait { $("#downloadprint") }
        kopieraKnappNoWait(required: false) { $("#copyBtn") }
        makuleraKnappNoWait { $("#makuleraBtn") }

        kopieraDialogMsgForlangningSjukskrivning(required:false, wait: true) { $("#msgForlangningSjukskrivning") }
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
