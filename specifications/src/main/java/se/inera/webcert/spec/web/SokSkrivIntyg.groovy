package se.inera.webcert.spec.web

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.*
import se.inera.webcert.pages.fk7263.VisaFk7263Page
import se.inera.webcert.pages.ts_bas.VisaTsBasPage
import se.inera.webcert.pages.ts_diabetes.EditeraTsDiabetesPage
import se.inera.webcert.pages.ts_diabetes.VisaTsDiabetesPage
import se.inera.webcert.spec.util.WebcertRestUtils

class SokSkrivIntyg {

    def kopiaintygsid

    // ------- navigation
    def gaTillSokSkrivIntyg() {
        Browser.drive {
            go "/web/dashboard#/create/index"
            waitFor {
                at SokSkrivaIntygPage
            }
        }
    }

    def skickaVisatIntyg() {
        Browser.drive {
            page.send()
            waitFor {
                doneLoading()
            }
        }
    }

    def skickaDetVisadeIntygetAvTyp(String typ) {

        Browser.drive {
            waitFor {
                if (typ == "fk7263") {
                    at se.inera.webcert.pages.fk7263.VisaFk7263Page
                } else if (typ == "ts-bas") {
                    at se.inera.webcert.pages.ts_bas.VisaTsBasPage
                } else if (typ == "ts-diabetes") {
                    at se.inera.webcert.pages.ts_diabetes.VisaTsDiabetesPage
                }
                page.sendWithValidation()
            }
        }
    }

    // ------- pages
    boolean sokSkrivIntygSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                doneLoading()
            }
            result = isAt SokSkrivaIntygPage
        }
        result
    }

    boolean fyllINamnSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                doneLoading()
            }
            result = isAt SokSkrivFyllINamnPage
        }
        result
    }

    boolean valjIntygstypSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                doneLoading()
            }
            result = at SokSkrivValjIntygTypPage
        }
        result
    }

    // ------- behaviour
    void valjPatient(String personNummer) {
        Browser.drive {
            page.angePatient(personNummer)
            waitFor {
                doneLoading()
            }
        }
    }

    void gePatientFornamnEfternamn(String fornamn, String efternamn) {
        Browser.drive {
            page.fornamn = fornamn
            page.efternamn = efternamn
            page.namnFortsattKnapp.click()
            waitFor {
                doneLoading()
            }
        }
    }

    void valjKopieraTidigareIntyg(String intygId) {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            page.kopieraKnapp(intygId).click()
            waitFor {
                doneLoading()
            }
        }
    }

    void valjVisaInteIgenIDialogen() {
        Browser.drive {
            page.kopieraDialogVisaInteIgen.click()
            waitFor {
                doneLoading()
            }
        }
    }


    void valjKopieraIDialogen() {
        Browser.drive {
            page.kopieraDialogKopieraKnapp.click()
            waitFor {
                doneLoading()
            }
        }
    }

    void kopieraTidigareIntyg(String intygId) {
        Browser.drive {
            page.copy(intygId)
        }
    }

    def intygsid

    def kopieraIntygOchGaTillVisaSida(String intygId) {
        Browser.drive {
            page.copy(intygId)
        }
    }

    String intygsid() {
        intygsid
    }

    void oppnaKopieraDialogen() {
        Browser.drive {
            page.kopieraKnapp.click()
            waitFor {
                doneLoading()
            }
        }
    }

    void kopieraVisatIntyg(typ) {
        Browser.drive {
            page.copy()
            waitFor {
                if (typ == "FK7263") {
                    at se.inera.webcert.pages.fk7263.EditeraFk7263Page
                } else if (typ == "ts-bas") {
                    at se.inera.webcert.pages.ts_bas.EditeraTsBasPage
                } else if (typ == "ts-diabetes") {
                    at se.inera.webcert.pages.ts_diabetes.EditeraTsDiabetesPage
                }
            }

            kopiaintygsid = currentUrl.substring(currentUrl.lastIndexOf("/") + 1)
            if (kopiaintygsid.indexOf("?") >= 0) {
                kopiaintygsid = kopiaintygsid.substring(0, kopiaintygsid.indexOf("?"))
            }
        }
    }

    boolean makuleraBekraftelseVisas() {
        boolean result
        Browser.drive {
            result = page.makuleraConfirmationOkButton.isDisplayed()
        }
        result
    }

    void visaIntyg(String intygId) {
        Browser.drive {
            page.show(intygId)
        }
    }

    void valjVardenhet(String careUnit) {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
            page.careUnitSelector.click()

            page.selectCareUnit(careUnit);

        }
    }
    // ------- state
    boolean namnFinnsEjMeddelandeVisas() {
        boolean result
        Browser.drive {
            page.puFelmeddelande.isDisplayed()
        }
        result
    }
	
	String PUTjänstFel() {
		String felmeddelande
		Browser.drive {
			felmeddelande = page.puFelmeddelande.text()
		}
		return felmeddelande
	}
	

    boolean valjIntygstypSynlig() {
        boolean result
        Browser.drive {
            result = page.intygTyp.isDisplayed()
        }
        result
    }

    boolean valjIntygstypEjSynlig() {
        boolean result
        Browser.drive {
            result = !page.intygTypNoWait.isDisplayed()
        }
        result
    }

    boolean sekretessmarkeringTextSynlig() {
        boolean result
        Browser.drive {
            result = page.sekretessmarkering.isDisplayed()
        }
        result
    }

    String patientensNamnAr() {
        String result
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            result = page.patientNamn.text()
        }
        result
    }

    boolean kopieraKnappVisasForIntyg(String intygId) {
        boolean result
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            result = page.kopieraKnapp(intygId).isDisplayed()
        }
        result
    }

    boolean skickaStatusVisas() {
        boolean result
        Browser.drive {
            result = page.certificateIsSentToRecipientMessage; //.isDisplayed()
        }
        result
    }

    boolean skickaStatusVisasMedRattMeddelande(String containsText) {
        boolean result
        Browser.drive {
            result = page.certificateIsOnQueueToITMessage.text().contains(containsText);
        }
        result
    }

    boolean annanEnhetTextVisas() {
        boolean result
        Browser.drive {
            result = page.annanEnhetText
        }
        result
    }

    void makuleraVisatIntyg() {
        Browser.drive {
            page.makulera()
        }
    }

    def visaSokSkrivIntyg() {
        Browser.drive {

        }
    }

    def kanEjMakuleraVisatIntyg() {
        Browser.drive {
            !page.makuleraKnappSyns()
        }
    }

    boolean makuleradStatusVisas() {
        boolean result
        Browser.drive {
            result = page.certificateIsRevokedMessage; //.isDisplayed()
            //return $("#certificate-is-revoked-message-text").isDisplayed()
        }
        result
    }

    // BEGIN these go to the same page but for different classes. should be merged in the future
    boolean visaSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.intygVy.isDisplayed()
        }
        result
    }

    boolean visaIntygSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            result = page.intygLaddat.isDisplayed()
        }
        result
    }
    // END

    boolean visaEditIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at se.inera.webcert.pages.fk7263.EditeraFk7263Page
            }
            intygsid = currentUrl.substring(currentUrl.lastIndexOf("/") + 1)
        }
        intygsid
    }

    boolean visaTsBasSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaTsBasPage
            }
            result = page.intygLaddat.isDisplayed()
        }
        result
    }

    boolean visaTsDiabetesSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaTsDiabetesPage
            }
            result = page.intygLaddat.isDisplayed()
        }
        result
    }

    boolean enhetsvaljareVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.careUnitSelector.isDisplayed()
        }
        result
    }

    boolean felmeddelanderutaVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            result = page.felmeddelandeRuta.isDisplayed()
        }
        result
    }

    void intygstjanstStubOnline() {
        WebcertRestUtils.setIntygTjanstStubInMode("ONLINE")
    }

    void intygstjanstStubOffline() {
        WebcertRestUtils.setIntygTjanstStubInMode("OFFLINE")
    }

    void intygstjanstStubLatency(Long latency = 0L) {
        WebcertRestUtils.setIntygTjanstStubLatency(latency)
    }
//
//        }
//    }

    void skickaVisatIntygTillForsakringskassan() {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            page.send()
        }
    }

    boolean intygSkickatTillForsakringskassan() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            result = page.certificateIsSentToRecipientMessage.isDisplayed()
        }
        result
    }

    boolean intygLagtPaSandKoVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            result = page.certificateIsOnQueueToITMessage.isDisplayed()
        }
        result
    }

    void stallNyFragaTillForsakringskassan() {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            page.stallNyFragaTillForsakringskassan()
            waitFor {
                page.nyFragaTillForsakringskassanFormularVisas()
            }
        }
    }

    boolean nyFragaFormularVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            result = page.nyFragaTillForsakringskassanFormularVisas()
        }
        result
    }

    def fyllOchSkickaFragaTillForsakringskassan() {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                page.fillNyFragaFormular()
            }
        }
    }

    boolean nyFragaSkickadBekraftelseVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                expected == page.nyFragaSkickadTextVisas()
            }
            true
        }
    }

    boolean kopieraKnappHarTextSjukskrivning() {
        def result
        Browser.drive {
            result = page.kopieraKnapp.attr("title").contains("kopia skapas") &&
                    page.kopieraKnapp.attr("title").contains("sjukskrivning")
        }
        return result
    }

    boolean kopieraKnappHarInteTextSjukskrivning() {
        def result
        Browser.drive {
            result = page.kopieraKnapp.attr("title").contains("kopia skapas") &&
                    !page.kopieraKnapp.attr("title").contains("sjukskrivning")
        }
        return result
    }

    // ------- utils
    void ateraktiveraKopieraDialogen() {
        Browser.deleteCookie("wc.dontShowCopyDialog");
    }

    def kopiaintygsid() {
        kopiaintygsid
    }

}
