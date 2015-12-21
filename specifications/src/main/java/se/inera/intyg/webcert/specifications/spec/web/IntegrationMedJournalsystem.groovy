/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.specifications.spec.web
import se.inera.intyg.common.specifications.spec.Browser
import se.inera.intyg.webcert.specifications.pages.fk7263.EditeraFk7263Page
import se.inera.intyg.webcert.specifications.pages.fk7263.VisaFk7263Page
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class IntegrationMedJournalsystem extends ExceptionHandlingFixture {

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
        def result;
        Browser.drive {
            waitFor {
                result = page.intygLaddat?.isDisplayed()
            }
        }
        result
    }

    boolean intygInteLaddat() {
        def result
        Browser.drive {
            waitFor{
                result = page.intygSaknas?.isDisplayed()
            }
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
        def result
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
        def result
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
        def result
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
        def result
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
        def result
        Browser.drive {
            result = page.skickaKnapp?.isDisplayed()
        }
        result
    }

    boolean skrivUtKnappVisas() {
        def result
        Browser.drive {
            result = (page.skrivUtKnapp?.present && page.skrivUtKnapp.isDisplayed()) || (page.skrivUtKnappEmployer?.present && page.skrivUtKnappEmployer.isDisplayed())
        }
        result
    }

    boolean kopieraKnappVisas() {
        def result
        Browser.drive {
            waitFor {
                result = page.kopieraKnapp?.isDisplayed()
            }
        }
        return result;
    }

    boolean makuleraKnappVisas() {
        def result
        Browser.drive {
            result = page.makuleraKnapp?.isDisplayed()
        }
        result
    }

    boolean verifieraTillbakaknappSynlig() {
        def result
        Browser.drive {
            result = page.tillbakaBtn?.isDisplayed()
        }
        result
    }

    boolean verifieraVidarebefordraUtkastSynlig() {
        def result
        Browser.drive {
            result = page.vidarebefordraEjHanterad?.isDisplayed()
        }
        result
    }

    boolean verifieraSigneraKnappSynlig() {
        def result
        Browser.drive {
            result = page.signeraBtn?.isDisplayed()
        }
        result
    }

    boolean raderaUtkastFelmeddelandeVisas() {
        def result
        Browser.drive {
            waitFor {
                result = page.errorPanel?.isDisplayed()
            }
        }
        result
    }

    boolean integrationBorttagetMeddelandeVisas() {
        def result
        Browser.drive {
            waitFor {
                at EditeraFk7263Page
            }
            waitFor {
                result = page.integrationBorttaget?.isDisplayed()
            }
        }
        result
    }

    boolean vidarebefordraEjHanteradFragaSynlig() {
        def result
        Browser.drive {
            result = page.vidarebefordraEjHanterad?.present
        }
        result
    }

    boolean webcertLogoKlickbar() {
        def result
        Browser.drive {
            result = page.webcertLogoLink?.present
        }
        result
    }

    boolean huvudmenySynlig() {
        def result
        Browser.drive {
            result = page.huvudmeny?.present
        }
        result
    }

    boolean bytVardenhetSynlig() {
        def result
        Browser.drive {
            result = page.bytVardenhetLink?.present
        }
        result
    }

    boolean loggaUtSynlig() {
        def result
        Browser.drive {
            result = page.loggaUtLink
        }
        result
    }

    boolean omWebcertKanOppnasViaLank() {
        def result
        Browser.drive {
            page.omWebcertLink.click()
            waitFor {
                doneLoading()
            }
            result = page.omWebcertDialog.isDisplayed()
        }
        result
    }

    boolean textInteFranJournalsystemVisas() {
        def result
        Browser.drive {
            waitFor{
                VisaFk7263Page
            }
            result = page.kopieraDialogMsgInteFranJournalSystem?.isDisplayed()
        }
        result
    }

	boolean textNyttPersonnummerVisas() {
		def result
		Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
			result = page.kopieraDialogMsgNyttPersonId?.isDisplayed()
		}
		result
	}

    boolean kopieraDialogenVisas() {
        def result
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                page.openCopyDialog();
                result = page.copyDialog?.isDisplayed();
            }
        }
        result
    }

    boolean forlangningSjukskrivningVisas() {
        def result
        Browser.drive {
            result =  page.kopieraDialogMsgForlangningSjukskrivning?.isDisplayed();
        }
        result
    }

}
