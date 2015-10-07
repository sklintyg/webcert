package se.inera.webcert.spec.web

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.*
import se.inera.webcert.pages.fk7263.EditeraFk7263Page
import se.inera.webcert.pages.fk7263.VisaFk7263Page

class IntegrationMedJournalsystem {

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
            result = intygLaddat.isDisplayed()
        }
        result
    }

    boolean intygInteLaddat() {
        boolean result
        Thread.sleep(2000)
        Browser.drive {
            result = intygSaknas.isDisplayed()
        }
        result
    }

    def visaUtkastViaIntegrationMedPersonnummer(String intygId, String personnummer) {
        Browser.drive {
            go "/visa/intyg/" + intygId + "?alternatePatientSSn=" + personnummer
            waitFor {
                at EditeraFk7263Page
            }
        }
    }

    def visaUtkastViaIntegrationMedPersonnummerSignerandeLakare(String intygId, String personnummer, String signerandeLakare) {
        Browser.drive {
            go "/visa/intyg/" + intygId + "?alternatePatientSSn=" + personnummer + "&responsibleHospName=" + signerandeLakare
            waitFor {
                at EditeraFk7263Page
            }
        }
    }

    boolean utkastVisasViaIntegrationMedBehorighetsfel(String intygId) {
        boolean result
        Browser.drive {
            go "/visa/intyg/" + intygId
            waitFor {
                at EditeraFk7263Page
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
                at EditeraFk7263Page
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
        boolean result
        Browser.drive {
            waitFor {
                page.signerandeLakare.isDisplayed()
            }
            result = (expected == page.signerandeLakare.text()) && page.signerandeLakare.isDisplayed()
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
	
    void kopieraIntyg() {
        Browser.drive {
            page.copy()
        }
    }

    void stangAvKopieraDialogen() {
        Browser.drive {
            page.kopieraKnapp.click()
            waitFor {
                doneLoading()
            }
            page.kopieraDialogVisaInteIgen.click()
            page.kopieraDialogKopieraKnapp.click()
            page.kopieraDialogAvbrytKnapp.click()
        }
    }

    void aktiveraKopieraDialogen() {
        Browser.deleteCookie("wc.dontShowCopyDialog");
    }

    void kopieraIntygUtanDialog() {
        Browser.drive {
            page.kopieraKnapp.click()
        }
    }

    boolean intygsFelVisas() {
        boolean result
        Browser.drive {
            result = page.intygFel.isDisplayed()
        }
        result
    }

    void visaUtkastViaIntegration(String intygId) {
        Browser.drive {
            go "/visa/intyg/" + intygId
            waitFor {
                at EditeraFk7263Page
            }
        }
    }


    boolean skickaKnappVisas() {
        boolean result
        Browser.drive {
            result = page.skickaKnapp.isDisplayed()
        }
        result
    }

    boolean skrivUtKnappVisas() {
        boolean result
        Browser.drive {
            result = page.skrivUtKnapp.isDisplayed()
        }
        result
    }

    boolean kopieraKnappVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = page.kopieraKnapp.isDisplayed()
            }
        }
        return result;
    }

    boolean kopieraKnappVisasEj() {
        Browser.drive {
            result = page.kopieraKnapp.isDisplayed()
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

    boolean verifieraTillbakaknappBorta() {
        Browser.drive {
            waitFor() {
                !page.tillbakaButton
            }
        }
        true
    }

    boolean raderaUtkastFelmeddelandeVisas() {
        boolean result
        Browser.drive {
            result = page.errorPanel.isDisplayed()
        }
        result
    }

    boolean integrationBorttagetMeddelandeVisas() {
        boolean result
        Browser.drive {
            waitFor {
             at EditeraFk7263Page
            }
            result = page.integrationBorttaget.isDisplayed()
        }
        result
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
            waitFor {
                doneLoading()
            }
            result = page.omWebcertDialog.isDisplayed()
        }
        result
    }

    boolean inteFranJournalSystemTextVisas() {
        boolean result
        Browser.drive {
            waitFor{
                VisaFk7263Page
            }
            result = page.kopieraDialogMsgInteFranJournalSystem.isDisplayed()
        }
        return result
    }

	boolean nyttPersonNummerTextVisas() {
		boolean result
		Browser.drive {
			result = page.kopieraDialogMsgNyttPersonId.isDisplayed()
		}
		return result
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
            waitFor {
                at AbstractViewCertPage
            }
            result =  page.kopieraDialogMsgForlangningSjukskrivning.isDisplayed();  
        }
        
        return result
    }

    def sleepForNSeconds(String time) {
        def n = time as int;
        def originalMilliseconds = System.currentTimeMillis()
        Browser.drive {
            result = page.kopieraDialogMsgForlangningSjukskrivning.isDisplayed();
        }
        result
    }
}
