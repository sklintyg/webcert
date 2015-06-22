package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
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

    boolean intygLaddat(boolean expected = true) {
        Browser.drive {
            waitFor {
                if (expected) {
                    at VisaPage
                    page.intygLaddat(expected)
                }
                true
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
    
    def utkastVisasViaIntegrationMedBehorighetsfel(String intygId, boolean expected = true) {
        Browser.drive {
            go "/visa/intyg/" + intygId
            waitFor {
                at EditCertPage
                expected == page.errorPanel.isDisplayed()
            }
        }
        true
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


    boolean skickaKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                page.skickaKnapp.isDisplayed() == expected
            }
        }
        true
    }

    boolean skrivUtKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                page.skrivUtKnapp.isDisplayed() == expected
            }
        }
        true
    }

    boolean kopieraKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                page.kopieraKnapp.isDisplayed() == expected
            }
        }
        true
    }

    boolean makuleraKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                page.makuleraKnapp.isDisplayed() == expected
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

    boolean integrationBorttagetMeddelandeVisas() {
        Browser.drive {
            waitFor {
                at EditCertPage
            }
            waitFor() {
                page.integrationBorttaget.isDisplayed()
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

    boolean inteFranJournalSystemTextVisas() {
        def result = false
        Browser.drive {
            waitFor() {
                result = page.kopieraDialogMsgInteFranJournalSystem.isDisplayed()
            }
        }
        return result
    }

    boolean inteFranJournalSystemTextInteVisas(){
        def result
        Browser.drive {
            waitFor() {
                result = !page.kopieraDialogMsgInteFranJournalSystem.isDisplayed()

            }
        }
        result
    }

    boolean nyttPersonNummerTextVisas() {
        def result = false
        Browser.drive {
            waitFor() {
                result =  page.kopieraDialogMsgNyttPersonId.isDisplayed()
            }
        }
        return result
    }

    boolean nyttPersonNummerTextInteVisas(){
        def result
        Browser.drive {
            waitFor() {
                result = !page.kopieraDialogMsgNyttPersonId.isDisplayed();
            }
        }
        result
    }


    def toggleKopieraDialogen(boolean val){
        if(val){
            Browser.drive {
                waitFor {
                    page.openCopyDialog();
                    page.copyDialog.isDisplayed();
                }
            }
        } else {
            Browser.drive {
                waitFor {
                    page.closeCopyDialog();
                    !page.copyDialog.isDisplayed();
                }
            }
        }
    }

    boolean kopieraDialogenVisas(){
        def result
        Browser.drive {
            waitFor() {
                result = page.copyDialog.isDisplayed();
            }
        }
        result
    }

    boolean forlangningSjukskrivningVisas() {
        def result
        Browser.drive {
            waitFor() {
                result = page.kopieraDialogMsgForlangningSjukskrivning.isDisplayed();
            }
        }
        return result
    }

    boolean forlangningSjukskrivningInteVisas() {
        def result
        Browser.drive {
            waitFor() {
                result = !page.kopieraDialogMsgForlangningSjukskrivning.isDisplayed();
            }
        }
        return result
    }

    def sleepForNSeconds(String time) {
        def n = time as int;
        def originalMilliseconds = System.currentTimeMillis()
        Browser.drive {
            waitFor(n + 1, 0.5) {
                (System.currentTimeMillis() - originalMilliseconds) > (n * 1000)
            }
        }
    }

}
