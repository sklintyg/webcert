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

    def valjPatient(String personNummer) {
        Browser.drive {
            page.personnummer = personNummer
            page.personnummerFortsattKnapp.click()
        }
    }

    boolean fyllINamnSidanVisas() {
        Browser.drive {
            waitFor {
                at SokSkrivFyllINamnPage
            }
        }
    }

    def namnFinnsEjMeddelandeVisas() {
        Browser.drive {
            waitFor {
                page.puFelmeddelande.isDisplayed()
            }
        }
    }

    def gePatientFornamnEfternamn(String fornamn, String efternamn) {
        Browser.drive {
            page.fornamn = fornamn
            page.efternamn = efternamn
            page.namnFortsattKnapp.click()
        }
    }

    boolean valjIntygstypSidanVisas() {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
        }
    }

    boolean patientensNamnAr(String expected) {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            waitFor {
                expected == page.patientNamn.text()
            }
        }
        true
    }

    boolean kopieraKnappVisasForIntyg(boolean expected = true, String intygId) {
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            waitFor {
                expected == page.copyBtn(intygId).isDisplayed()
            }
        }
        true
    }

    def ateraktiveraKopieraDialogen() {
        Browser.deleteCookie("wc.dontShowCopyDialog");
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
            waitFor {
                page.copyBtn(intygId).isDisplayed()
            }
            println('page ' + page);
            page.copy(intygId)
        }
    }

    def intygsid

    def kopieraIntygOchGaTillVisaSida(String intygId) {
        Browser.drive {
            waitFor {
                page.copyBtn(intygId).isDisplayed()
            }
            page.copy(intygId)

        }
    }

    String intygsid() {
        intygsid
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

    boolean skickaStatusVisas() {
        Browser.drive {
            waitFor {
                page.certificateIsSentToRecipientMessage.isDisplayed()
            }
        }
        true
    }

    boolean skickaStatusVisasMedRattMeddelande(boolean expected = true, String containsText) {

        Browser.drive {

            waitFor {
                expected = page.certificateIsSentToRecipientMessage.text().contains(containsText)
            }
        }
        expected
    }

    def oppnaKopieraDialogen() {
        Browser.drive {
            page.copyButton.click()
        }
    }

    boolean annanEnhetTextVisas() {
        Browser.drive {
            waitFor {
                page.annanEnhetText.isDisplayed()
            }
        }
        true
    }

    def kopiaintygsid() {
        kopiaintygsid
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

    def makuleraVisatIntyg() {
        Browser.drive {
            page.makulera()
        }
    }

    def visaSokSkrivIntyg() {
        Browser.drive {

        }
    }

    boolean redigeraUtkastSidanVisas() {
        Browser.drive {
            waitFor {
                at se.inera.webcert.pages.fk7263.EditCertPage
            }
        }
        true
    }

    def kanEjMakuleraVisatIntyg() {
        Browser.drive {
            page.kanInteMakulera()
        }
        true
    }

    def makuleraBekraftelseVisas() {
        Browser.drive {
            waitFor {
                page.makuleraConfirmationOkButton.isDisplayed()
            }
            makuleraConfirmationOkButton.click()
        }
    }

    boolean makuleradStatusVisas() {
        Browser.drive {
            waitFor {
                page.certificateIsRevokedMessage.isDisplayed()
            }
        }
        true
    }

    def visaIntyg(String intygId) {

        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            waitFor {
                page.intygLista.isDisplayed()
            }

            page.show(intygId)
        }
    }

    // BEGIN these go to the same page but for different classes. should be merged in the future
    boolean visaSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }

            waitFor {
                page.intygVy.isDisplayed()
            }
        }
    }

    boolean visaIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }

            waitFor {
                page.intygLaddat.isDisplayed()
            }
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

            waitFor {
                page.intygLaddat.isDisplayed()
            }
        }
    }

    boolean visaTsDiabetesSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaTsDiabetesPage
            }

            waitFor {
                page.intygLaddat.isDisplayed()
            }
        }
    }

    public void loggaInIndex() {
        Browser.drive {
            waitFor {
                at IndexPage
            }
            page.startLogin()
        }
    }

    boolean enhetsvaljareVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
            waitFor {
                expected == page.careUnitSelector.isDisplayed()
            }
        }
        true
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

    def valjVardenhet(String careUnit) {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
            waitFor {
                page.careUnitSelector.click()
            }
            waitFor {
                page.selectCareUnit(careUnit);
            }
        }
    }

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
            waitFor {
                result = page.kopieraKnapp.attr("title").contains("kopia skapas") &&
                        page.kopieraKnapp.attr("title").contains("sjukskrivning")
            }
        }
        return result
    }

    boolean kopieraKnappHarInteTextSjukskrivning() {
        def result
        Browser.drive {
            waitFor {
                result = page.kopieraKnapp.attr("title").contains("kopia skapas") &&
                        !page.kopieraKnapp.attr("title").contains("sjukskrivning")
            }
        }
        return result
    }
}
