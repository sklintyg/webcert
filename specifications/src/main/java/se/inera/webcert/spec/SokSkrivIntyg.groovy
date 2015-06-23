package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.*
import se.inera.webcert.pages.fk7263.VisaFk7263Page
import se.inera.webcert.pages.ts_bas.VisaTsBasPage
import se.inera.webcert.pages.ts_diabetes.EditCertPage
import se.inera.webcert.pages.ts_diabetes.VisaTsDiabetesPage
import se.inera.webcert.spec.util.WebcertRestUtils

class SokSkrivIntyg {

    def kopiaintygsid

    // ------- navigation
    def loggaInSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor {
                at WelcomePage
            }
            page.loginAs(id)
        }
    }

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
        sokSkrivIntygSidanVisasSaSmaningom()
    }

    boolean sokSkrivIntygSidanVisasSaSmaningom() {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
        }
    }

    boolean fyllINamnSidanVisas() {
        Browser.drive {
            waitFor {
                at SokSkrivFyllINamnPage
            }
        }
    }

    boolean valjIntygstypSidanVisas() {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
        }
    }

    // ------- behaviour
    def valjPatient(String personNummer) {
        Browser.drive {
            page.angePatient(personNummer)
        }
    }

    def gePatientFornamnEfternamn(String fornamn, String efternamn) {
        Browser.drive {
            page.fornamn = fornamn
            page.efternamn = efternamn
            page.namnFortsattKnapp.click()
        }
    }

    def valjKopieraTidigareIntyg(String intygId) {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            page.copyBtn(intygId).click()
            waitFor {
                doneLoading()
            }
        }
    }

    def valjVisaInteIgenIDialogen() {
        Browser.drive {
            page.kopieraDialogVisaInteIgen.click()
        }
    }


    def valjKopieraIDialogen() {
        Browser.drive {
            page.kopieraDialogKopieraKnapp.click()
        }
    }

    def kopieraTidigareIntyg(String intygId) {
        Browser.drive {
            page.copyBtn(intygId).isDisplayed()
            println('page ' + page);
            page.copy(intygId)
        }
    }

    def intygsid

    def kopieraIntygOchGaTillVisaSida(String intygId) {
        Browser.drive {
            page.copyBtn(intygId).isDisplayed()
            page.copy(intygId)

        }
    }

    String intygsid() {
        intygsid
    }

    def oppnaKopieraDialogen() {
        Browser.drive {
            page.copyButton.click()
        }
    }

    def kopieraVisatIntyg(typ) {
        Browser.drive {
            page.copy()
            waitFor {
                if (typ == "FK7263") {
                    at se.inera.webcert.pages.fk7263.EditCertPage
                } else if (typ == "ts-bas") {
                    at se.inera.webcert.pages.ts_bas.EditCertPage
                } else if (typ == "ts-diabetes") {
                    at se.inera.webcert.pages.ts_diabetes.EditCertPage
                }
            }

            kopiaintygsid = currentUrl.substring(currentUrl.lastIndexOf("/") + 1)
            if (kopiaintygsid.indexOf("?") >= 0) {
                kopiaintygsid = kopiaintygsid.substring(0, kopiaintygsid.indexOf("?"))
            }
        }
    }

    def makuleraBekraftelseVisas() {
        Browser.drive {
            page.makuleraConfirmationOkButton.isDisplayed()
        }
    }

    def visaIntyg(String intygId) {
        Browser.drive {
            page.show(intygId)
        }
    }

    public void loggaInIndex() {
        Browser.drive {
            waitFor {
                at IndexPage
                expected = page.certificateIsOnQueueToITMessage.text().contains(containsText)
            }
            page.startLogin()
        }
    }

    def valjVardenhet(String careUnit) {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
            page.careUnitSelector.click()

            page.selectCareUnit(careUnit);

        }
    }
    // ------- state
    def namnFinnsEjMeddelandeVisas() {
        Browser.drive {
            page.puFelmeddelande.isDisplayed()
        }
    }
	
	public String PUTjänstFel() {
		String felmeddelande
		Browser.drive {
			felmeddelande = page.puFelmeddelande.text()
		}
		return  felmeddelande
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

    boolean kopieraKnappVisasForIntyg(boolean expected = true, String intygId) {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            return page.copyBtn(intygId).isDisplayed()
        }
    }

    boolean skickaStatusVisas() {
        Browser.drive {
            return page.certificateIsSentToRecipientMessage; //.isDisplayed()
        }
    }

    boolean skickaStatusVisasMedRattMeddelande(boolean expected = true, String containsText) {
        Browser.drive {
            return page.certificateIsOnQueueToITMessage.text().contains(containsText);
        }
    }

    boolean annanEnhetTextVisas() {

        Browser.drive {
            return page.annanEnhetText
        }
    }

    def makuleraVisatIntyg() {
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
            page.kanInteMakulera()
        }
    }

    boolean makuleradStatusVisas() {
        Browser.drive {
            return page.certificateIsRevokedMessage; //.isDisplayed()
            //return $("#certificate-is-revoked-message-text").isDisplayed()
        }
    }

    // BEGIN these go to the same page but for different classes. should be merged in the future
    boolean visaSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }

            page.intygVy.isDisplayed()
        }
    }

    boolean visaIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }

            page.intygLaddat.isDisplayed()
        }
    }
    // END

    boolean visaEditIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at se.inera.webcert.pages.fk7263.EditCertPage
            }
            intygsid = currentUrl.substring(currentUrl.lastIndexOf("/") + 1)
        }
    }

    boolean visaTsBasSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaTsBasPage
            }

            page.intygLaddat.isDisplayed()
        }
    }

    boolean visaTsDiabetesSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaTsDiabetesPage
            }

            page.intygLaddat.isDisplayed()
        }
    }

    boolean enhetsvaljareVisas() {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            return page.careUnitSelector.isDisplayed()
        }
    }

    boolean felmeddelanderutaVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            waitFor {
                expected == page.felmeddelandeRuta.isDisplayed()
            }
        }
    }

    def intygstjanstStubOnline() {
        WebcertRestUtils.setIntygTjanstStubInMode("ONLINE")
    }

    def intygstjanstStubOffline() {
        WebcertRestUtils.setIntygTjanstStubInMode("OFFLINE")
    }

    def intygstjanstStubLatency(Long latency = 0L) {
        WebcertRestUtils.setIntygTjanstStubLatency(latency)
    }
//
//        }
//    }

    def skickaVisatIntygTillForsakringskassan() {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                page.send()
            }
        }
    }

    boolean intygSkickatTillForsakringskassan(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                expected == page.certificateIsSentToRecipientMessage.isDisplayed()
            }
        }
        true
    }

    boolean intygLagtPaSandKoVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                expected == page.certificateIsOnQueueToITMessage.isDisplayed()
            }
        }
    }

    def stallNyFragaTillForsakringskassan() {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                page.stallNyFragaTillForsakringskassan()
            }
            waitFor {
                page.nyFragaTillForsakringskassanFormularVisas()
            }
        }
    }

    boolean nyFragaFormularVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                expected == page.nyFragaTillForsakringskassanFormularVisas()
            }
        }
        true
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
    def ateraktiveraKopieraDialogen() {
        Browser.deleteCookie("wc.dontShowCopyDialog");
    }

    def kopiaintygsid() {
        kopiaintygsid
    }

}
