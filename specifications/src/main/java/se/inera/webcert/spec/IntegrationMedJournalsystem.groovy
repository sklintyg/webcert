package se.inera.webcert.spec

import se.inera.webcert.pages.*
import se.inera.webcert.pages.fk7263.EditCertPage
import se.inera.webcert.pages.fk7263.VisaFk7263Page

class IntegrationMedJournalsystem {

    boolean exists(content) {
        content
    }

    def loggaInSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor {
                at WelcomePage
            }
            page.loginAs(id)
        }
    }

    def visaIntygViaIntegration(String intygId) {
        Browser.drive {
            go "/visa/intyg/" + intygId
            waitFor {
                at VisaFk7263Page
            }
        }
    }

    def visaIntygViaIntegrationMedPersonnummer(String intygId, String personnummer) {
        Browser.drive {
            go "/visa/intyg/" + intygId + "?alternatePatientSSn=" + personnummer
            waitFor {
                at VisaFk7263Page
            }
        }
    }

    def visaUtkastViaIntegrationMedPersonnummer(String intygId, String personnummer) {
        Browser.drive {
            go "/visa/intyg/" + intygId + "?alternatePatientSSn=" + personnummer
            waitFor {
                at EditCertPage
            }
        }
    }

    def visaUtkastViaIntegrationMedPersonnummerSignerandeLakare(String intygId, String personnummer, String signerandeLakare) {
        Browser.drive {
            go "/visa/intyg/" + intygId + "?alternatePatientSSn=" + personnummer + "&responsibleHospName=" + signerandeLakare
            waitFor {
                at EditCertPage
            }
        }
    }

    def nyttPersonnummerMeddelandeVisas() {
        Browser.drive {
            waitFor {
                page.nyttPersonnummer.isDisplayed()
            }
        }
        true
    }

    def signerandeLakareMeddelandeVisas(expected) {
        Browser.drive {
            waitFor {
                (expected == page.signerandeLakare.text()) && page.signerandeLakare.isDisplayed()
            }
        }
        true
    }

    def verifieraNamnOchPersonnummer(String expected) {
        Browser.drive {
            waitFor {
                expected == page.namnOchPersonnummer.text()
            }
        }
        true
    }

    def kopieraIntyg(String intygId) {
        Browser.drive {
            waitFor {
                page.copyButton.isDisplayed()
            }
            page.copy()
        }
    }

    def stangAvKopieraDialogen() {
        Browser.drive {
            page.copyButton.click()
            waitFor {
                doneLoading()
            }
            page.kopieraDialogVisaInteIgen.click()
            page.kopieraDialogKopieraKnapp.click()
            page.kopieraDialogAvbrytKnapp.click()
        }
    }

    def aktiveraKopieraDialogen() {
        Browser.deleteCookie("wc.dontShowCopyDialog");
    }

    def kopieraIntygUtanDialog() {
        Browser.drive {
            waitFor {
                page.copyButton.isDisplayed()
            }
            page.copyButton.click()
        }
        true
    }

    def intygsFelVisas() {
        Browser.drive {
            waitFor {
                page.intygFel.isDisplayed()
            }
        }
        true
    }

    def intygetSynligt() {
        Browser.drive {
            waitFor {
                page.intygLaddat.isDisplayed()
            }
        }
        true
    }

    def visaUtkastViaIntegration(String intygId) {
        Browser.drive {
            go "/visa/intyg/" + intygId
            waitFor {
                at EditCertPage
            }
        }
        true
    }

    boolean verifieraTillbakaknappBorta() {
        Browser.drive {
            waitFor() {
                !exists(page.tillbakaButton)
            }
        }
        true
    }

    boolean raderaUtkastFelmeddelandeVisas() {
        Browser.drive {
            waitFor {
                at EditCertPage
            }
            waitFor() {
                page.errorPanel.isDisplayed()
            }
        }

        true
    }

    boolean vidarebefordraEjHanteradFragaEjSynlig() {
        Browser.drive {
            waitFor() {
                !exists(page.vidarebefordraEjHanterad)
            }
        }
        true
    }

    boolean webcertLogoEjKlickbar() {
        Browser.drive {
            waitFor() {
                !exists(page.webcertLogoLink)
            }
        }
        true
    }

    boolean huvudmenyEjSynlig() {
        Browser.drive {
            waitFor() {
                !exists(page.huvudmeny)
            }
        }
        true
    }

    boolean bytVardenhetEjSynlig() {
        Browser.drive {
            waitFor() {
                !exists(page.bytVardenhetLink)
            }
        }
        true
    }

    boolean loggaUtEjSynlig() {
        Browser.drive {
            waitFor() {
                !exists(page.loggaUtLink)
            }
        }
        true
    }


    boolean omWebcertKanOppnasViaLank() {
        Browser.drive {
            page.omWebcertLink.click()
            waitFor() {
                page.omWebcertDialog.isDisplayed()
            }
        }
        true
    }
}
