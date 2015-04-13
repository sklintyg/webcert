package se.inera.webcert.pages

import se.inera.certificate.page.AbstractPage

class SokSkrivValjIntygTypPage extends AbstractPage {

    static at = { doneLoading() && $("#valj-intyg-typ").isDisplayed() }

    static content = {
        patientNamn { $("#patientNamn") }
        intygtypFortsattKnapp { $("#intygTypeFortsatt") }
        intygTyp { $("#intygType") }
        intygLista { $("#intygLista") }
        kopieraDialogVisaInteIgen { $("#dontShowAgain") }
        kopieraDialogKopieraKnapp { $("#button1copy-dialog") }
        fortsattKnapp { $("#intygTypeFortsatt") }
        felmeddelandeRuta { $("#current-list-noResults-error")}
    }

    def copyBtn(String intygId) {
        $("#copyBtn-${intygId}")
    }

    def copy(String intygId) {
        copyBtn(intygId).click()
        waitFor {
            doneLoading()
        }
        kopieraDialogKopieraKnapp.click()
    }

    def show(String intygId) {
        $("#showBtn-${intygId}").click()
    }

    def valjIntygstypFk7263() {
        intygTyp.value("1")
    }
    def valjIntygstypTsBas() {
        intygTyp.value("2")
    }
    def valjIntygstypTsDiabetes() {
        intygTyp.value("3")
    }
    
    def valjIntygsTyp(String typ) {
        if (typ == "FK7263") {
            valjIntygstypFk7263();
        } else if (typ == "ts-bas") {
            valjIntygstypTsBas();
        } else if (typ == "ts-diabetes") {
            valjIntygstypTsDiabetes();
        }
        fortsattKnapp.click();
        waitFor {
            doneLoading()
        }
    }

}
