/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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
import se.inera.intyg.webcert.specifications.pages.*
import se.inera.intyg.webcert.specifications.pages.fk7263.EditeraFk7263Page
import se.inera.intyg.webcert.specifications.pages.fk7263.VisaFk7263Page
import se.inera.intyg.webcert.specifications.pages.ts_bas.EditeraTsBasPage
import se.inera.intyg.webcert.specifications.pages.ts_bas.VisaTsBasPage
import se.inera.intyg.webcert.specifications.pages.ts_diabetes.EditeraTsDiabetesPage
import se.inera.intyg.webcert.specifications.pages.ts_diabetes.VisaTsDiabetesPage
import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils
import se.inera.intyg.webcert.specifications.spec.util.screenshot.ExceptionHandlingFixture

class SokSkrivIntyg extends ExceptionHandlingFixture {

    def kopiaintygsid
    def intygsid


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
                    at se.inera.intyg.webcert.specifications.pages.fk7263.VisaFk7263Page
                } else if (typ == "ts-bas") {
                    at se.inera.intyg.webcert.specifications.pages.ts_bas.VisaTsBasPage
                } else if (typ == "ts-diabetes") {
                    at se.inera.intyg.webcert.specifications.pages.ts_diabetes.VisaTsDiabetesPage
                }
            }
            page.sendWithValidation()
        }
    }

    // ------- pages

    boolean sokSkrivIntygSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt SokSkrivaIntygPage
            }
        }
        result
    }

    boolean fyllINamnSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt SokSkrivFyllINamnPage
            }
        }
        result
    }

    boolean valjIntygstypSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt SokSkrivValjIntygTypPage
            }
        }
        return result
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
            // also need to wait for the dialog shim to hide
            page.waitForModalBackdropToHide();
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

    def kopieraIntygOchGaTillVisaSida(String intygId) {
        Browser.drive {
            page.copy(intygId)
        }
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
                    at EditeraFk7263Page
                } else if (typ == "ts-bas") {
                    at EditeraTsBasPage
                } else if (typ == "ts-diabetes") {
                    at EditeraTsDiabetesPage
                }
            }

            kopiaintygsid = currentUrl.substring(currentUrl.lastIndexOf("/") + 1)
            if (kopiaintygsid.indexOf("?") >= 0) {
                kopiaintygsid = kopiaintygsid.substring(0, kopiaintygsid.indexOf("?"))
            }
        }
    }

    def bekraftaMakulera(){
        Browser.drive {
            page.bekraftaMakulera();
        }
    }

    boolean makuleraBekraftelseVisas() {
        boolean result
        Browser.drive {
            waitFor {
                page.makuleraConfirmationOkButton.isDisplayed()
            }
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
            waitFor {
                page.puFelmeddelande.isDisplayed()
                result = page.puFelmeddelande.isDisplayed()
            }
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
            result = page.intygTyp?.isDisplayed()
        }
        result
    }

    boolean sekretessmarkeringTextSynlig() {
        boolean result
        Browser.drive {
            waitFor {
                page.sekretessmarkering.isDisplayed()
                result = page.sekretessmarkering.isDisplayed()
            }
        }
        result
    }

    String patientensNamnAr() {
        String result
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
                result = page.patientNamn.text()
            }
        }
        result
    }

    boolean kopieraKnappVisasForIntyg(String intygId) {
        def result
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            result = page.kopieraKnapp(intygId)?.isDisplayed()
        }
        result
    }

    boolean skickaStatusVisas() {
        boolean result
        Browser.drive {
            result = page.certificateIsSentToRecipientMessage?.isDisplayed()
        }
        result
    }

    boolean meddelandeIntygInteSkickatVisas() {
        boolean result
        Browser.drive {
            waitFor {
                page.certificateIsNotSentToFkMessage.isDisplayed()
            }
            result = page.certificateIsNotSentToFkMessage.isDisplayed();
        }
        result
    }

    boolean meddelandeForStatusIntygInteSkickatVisas(String containsText) {
        boolean result
        Browser.drive {
            waitFor {
                page.certificateIsSentToITMessage.isDisplayed()
            }
            result = page.certificateIsSentToITMessage.text().contains(containsText);
        }
        result
    }

    boolean meddelandeForStatusIntygSkickatVisas(String containsText) {
        boolean result
        Browser.drive {
            waitFor {
                page.certificateIsOnQueueToITMessage.isDisplayed()
            }
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

    def kanEjMakuleraVisatIntyg() {
        Browser.drive {
            !page.makuleraKnappSyns()
        }
    }

    boolean makuleradStatusVisas() {
        boolean result
        Browser.drive {
            result = page.makuleraStatusSyns();
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
                isAt VisaFk7263Page
                result = page.intygLaddat.isDisplayed()
            }

        }
        return result;
    }
    // END

    boolean visaEditIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at se.inera.intyg.webcert.specifications.pages.fk7263.EditeraFk7263Page
            }
            intygsid = currentUrl.substring(currentUrl.lastIndexOf("/") + 1)
        }
        intygsid
    }

    boolean visaTsBasSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                isAt VisaTsBasPage
                result = page.intygLaddat.isDisplayed()
            }
        }
        result
    }

    boolean visaTsDiabetesSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                isAt VisaTsDiabetesPage
                result = page.intygLaddat.isDisplayed()
            }
        }
        result
    }

    boolean enhetsvaljareVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.careUnitSelector.isDisplayed()
                result = page.careUnitSelector.isDisplayed()
            }
        }
        result
    }

    boolean felmeddelanderutaVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            waitFor {
                page.felmeddelandeRuta.isDisplayed()
                result = page.felmeddelandeRuta.isDisplayed()
            }
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
            waitFor {
                page.certificateIsSentToRecipientMessage.isDisplayed()
                result = page.certificateIsSentToRecipientMessage.isDisplayed()
            }
        }
        result
    }

    boolean intygLagtPaSandKoVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                page.certificateIsOnQueueToITMessage.isDisplayed()
                result = page.certificateIsOnQueueToITMessage.isDisplayed()
            }
        }
        result
    }

    void stallNyFragaTillForsakringskassan() {
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

    boolean nyFragaFormularVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                page.nyFragaTillForsakringskassanFormularVisas()
                result = page.nyFragaTillForsakringskassanFormularVisas()
            }
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
        def result
        Browser.drive {
            waitFor {
                at VisaFk7263Page
            }
            waitFor {
                page.nyFragaSkickadTextVisas()
                result = expected == page.nyFragaSkickadTextVisas()
            }
            result
        }
    }

    String kopieraKnappHarText() {
        def text
        Browser.drive {
            text = page.kopieraKnapp.attr("title")
        }
        return text
    }

    // ------- utils

    void ateraktiveraKopieraDialogen() {
        Browser.deleteCookie("wc.dontShowCopyDialog");
    }

    def kopiaintygsid() {
        kopiaintygsid
    }

    String intygsid() {
        intygsid
    }

    boolean anvandareKanForfattaIntygPa(String intygsTypText) {
        def result = false
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            page.waitForModalBackdropToHide();

            waitFor {
                // TODO use better page abstraction
                page.intygTyp.isDisplayed()
            }
            result = $("#intygType option[label=\"$intygsTypText\"]").size() > 0
        }
        result
    }

//    boolean attAnvandareKanForfattaIntygPa(String intygsTypText) {
//        def result
//        Browser.drive {
//            waitFor {
//                at SokSkrivValjIntygTypPage
//            }
//            page.waitForModalBackdropToHide();
//
//            waitFor {
//                // TODO use better page abstraction
//                result = $("#intygType option[label=\"$intygsTypText\"]").size() == 0
//            }
//        }
//        return result
//    }

    boolean intygAvTypVisasInteIListanAvTidigareIntyg(String intygsTyp) {
        boolean result = false
        Browser.drive {
            waitFor {
                at SokSkrivValjIntygTypPage
            }
            page.waitForModalBackdropToHide();

            waitFor {
                // TODO use better page abstraction
                result = $("#prevCertTable table tr td span[key=\"certificatetypes\\.$intygsTyp\\.typename\"]").size() == 0
            }
        }
        return result
    }
}
