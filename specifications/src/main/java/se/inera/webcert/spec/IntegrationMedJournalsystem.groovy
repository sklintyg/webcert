package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.*
import se.inera.webcert.pages.fk7263.EditCertPage
import se.inera.webcert.pages.fk7263.VisaFk7263Page

class IntegrationMedJournalsystem {

    boolean exists(content) {
        content
    }

    def visaIntygViaIntegration(String intygId) {
        Browser.drive {
            go "/visa/intyg/" + intygId + "?alternatePatientSSn="
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

    boolean intygLaddat() {
        boolean result;
        Browser.drive {
            waitFor {
                at VisaPage
            }
            waitFor {
                result = page.intygLaddat.isDisplayed();
            }
        }
        return result;
    }

    boolean intygInteLaddat() {
        Browser.drive {
            waitFor {
                at VisaPage
            }
            intygSaknas.isDisplayed()
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

    boolean utkastVisasViaIntegrationMedBehorighetsfel(String intygId) {
        boolean result
        Browser.drive {
            go "/visa/intyg/" + intygId
            waitFor {
                at EditCertPage
                result = page.errorPanel.isDisplayed()
            }

        }
        result
    }

    boolean utkastVisasViaIntegrationUtanBehorighetsfel(String intygId) {
        boolean result
        Browser.drive {
            go "/visa/intyg/" + intygId
            waitFor {
                at EditCertPage
            }
            result = !page.errorPanel.isDisplayed();
        }
        result
    }

    boolean nyttPersonnummerMeddelandeVisas() {
		def result
        Browser.drive {
            result = page.nyttPersonnummer.isDisplayed()
        }
		return result
    }

    boolean signerandeLakareMeddelandeVisas(expected) {
        def result
        Browser.drive {
            waitFor {
                page.signerandeLakare.isDisplayed()
            }
            result = (expected == page.signerandeLakare.text())
        }
        return result
    }

    boolean signeringKraverLakareMeddelandeVisas() {
        def result
        Browser.drive {
            waitFor {
                result = page.signeringKraverLakare.isDisplayed()
            }
        }
        return result
    }

    String patientensNamn() {
		String namnOchPersonnummer
        Browser.drive {
			waitFor{
				page.namnOchPersonnummer.isDisplayed()
			}
			namnOchPersonnummer = page.namnOchPersonnummer.text()
        }
		
       def (namn, personnummer) = namnOchPersonnummer.split( ' - ' )
	   return namn
    }
	
	String patientensPersonnummer() {
		String namnOchPersonnummer
		Browser.drive {
			waitFor{
				page.namnOchPersonnummer.isDisplayed()
			}
			namnOchPersonnummer = page.namnOchPersonnummer.text()
		}
		
	   def (namn, personnummer) = namnOchPersonnummer.split( ' - ' )
	   return personnummer
	}
	
    boolean kopieraIntyg(String intygId) {
        Browser.drive {
            page.copyButton.isDisplayed()
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

    boolean kopieraIntygUtanDialog() {
        Browser.drive {
            page.copyButton.isDisplayed()
            page.copyButton.click()
        }
    }

    boolean intygsFelVisas() {
        boolean result
        Browser.drive {
            result = page.intygFel.isDisplayed()
        }
        result
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


    boolean skickaKnappVisas() {
        boolean result
        Browser.drive {
            result = page.skickaKnapp.isDisplayed()
        }
        result
    }

    boolean skickaKnappVisasEj() {
        boolean result
        Browser.drive {
            result = !page.skickaKnappNoWait.isDisplayed()
        }
        result
    }

    boolean skrivUtKnappVisas() {
        boolean result
        Browser.drive {
            result = (page.skrivUtKnapp?.present && page.skrivUtKnapp.isDisplayed()) || (page.skrivUtKnappEmployer?.present && page.skrivUtKnappEmployer.isDisplayed())
        }
        result
    }

    boolean skrivUtKnappVisasEj() {
        boolean result
        Browser.drive {
            result = !page.skrivUtKnappNoWait.isDisplayed()
        }
        result
    }

    boolean kopieraKnappVisas() {
        boolean result;
        Browser.drive {
            waitFor {
                result = page.kopieraKnapp.isDisplayed()
            }
        }
        return result;
    }

    boolean kopieraKnappVisasEj() {
        boolean result
        Browser.drive {
            result = !page.kopieraKnappNoWait.isDisplayed()
        }
        result
    }

    boolean makuleraKnappVisas() {
        boolean result
        Browser.drive {
            result = page.makuleraKnapp.isDisplayed()
        }
        result
    }

    boolean makuleraKnappVisasEj() {
        boolean result
        Browser.drive {
            result = !page.makuleraKnappNoWait.isDisplayed()
        }
        result
    }

    boolean verifieraTillbakaknappBorta() {
        boolean result
        Browser.drive {
            waitFor() {
                result = !exists(page.tillbakaButton)
            }
        }
        result == true
    }

    boolean raderaUtkastFelmeddelandeVisas() {
        Browser.drive {
            waitFor {
                at EditCertPage
            }
            return page.errorPanel.isDisplayed()
        }
    }

    boolean integrationBorttagetMeddelandeVisas() {
        boolean result;
        Browser.drive {
            waitFor {
                at EditCertPage
            }
            waitFor{
                result = page.integrationBorttaget.isDisplayed();
            }
            return result;
        }
    }

    boolean vidarebefordraEjHanteradFragaEjSynlig() {
        boolean result
        Browser.drive {
            result = !page.vidarebefordraEjHanterad.present
        }
        result
    }

    boolean webcertLogoEjKlickbar() {
        boolean result
        Browser.drive {
            result = !page.webcertLogoLink.present
        }
        result
    }

    boolean huvudmenyEjSynlig() {
        boolean result
        Browser.drive {
            result = !page.huvudmeny.present
        }
        result
    }

    boolean bytVardenhetEjSynlig() {
        boolean result
        Browser.drive {
            result = !page.bytVardenhetLink.present
        }
        result
    }

    boolean loggaUtEjSynlig() {
        boolean result
        Browser.drive {
            result = !page.loggaUtLink
        }
        result
    }


    boolean omWebcertKanOppnasViaLank() {
        boolean result
        Browser.drive {
            page.omWebcertLink.click()
            result = page.omWebcertDialog.isDisplayed()
        }
        result
    }

    boolean inteFranJournalSystemTextVisas() {
        boolean result
        Browser.drive {
            result = page.kopieraDialogMsgInteFranJournalSystem.isDisplayed()
        }
        result
    }

    boolean inteFranJournalSystemTextInteVisas() {
        boolean result
        Browser.drive {
            result = !page.kopieraDialogMsgInteFranJournalSystemNoWait.isDisplayed()
        }
        result
    }

	boolean nyttPersonNummerTextVisas() {
		boolean result
		Browser.drive {
			result = page.kopieraDialogMsgNyttPersonId.isDisplayed()
		}
		result
	}
	
     boolean nyttPersonNummerTextInteVisas() {
		boolean result
        Browser.drive {
            result = !page.kopieraDialogMsgNyttPersonIdNoWait.isDisplayed();
        }
		result
     }


    def toggleKopieraDialogen(boolean val) {
        if (val) {
            Browser.drive {
                page.openCopyDialog();
                page.copyDialog.isDisplayed();
            }
        } else {
            Browser.drive {
                page.closeCopyDialog();
                !page.copyDialog.isDisplayed();
            }
        }
    }

    boolean kopieraDialogenVisas() {
        boolean result
        Browser.drive {
            result = page.copyDialog.isDisplayed();
        }
        result
    }

    boolean forlangningSjukskrivningVisas() {
        def result
        Browser.drive {
            result =  page.kopieraDialogMsgForlangningSjukskrivning();
        }
        result
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
