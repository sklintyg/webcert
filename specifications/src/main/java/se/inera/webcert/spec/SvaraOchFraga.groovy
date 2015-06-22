package se.inera.webcert.spec

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.IndexPage
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.UnhandledQAPage
import se.inera.webcert.pages.VisaFragaSvarPage
import se.inera.webcert.pages.WelcomePage

class SvaraOchFraga {

    def gaTillSvaraOchFraga(boolean wait = true) {
        Browser.drive {
            go "/web/dashboard#/unhandled-qa"
            if (wait) {
                waitFor {
                    at UnhandledQAPage
                }
            }
        }
    }

    boolean svaraOchFragaSidanVisas() {
        Browser.drive {
            at UnhandledQAPage
        }
    }

    def gaTillSokSkrivaIntyg() {
        Browser.drive {
            go "/web/dashboard#/create/index"
        }
        Thread.sleep(300);
    }

    boolean sokSkrivaIntygSidanVisas() {
        Browser.drive {
            at SokSkrivaIntygPage
        }
    }

    def visaAllaFragor() {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }

            waitFor {
                page.visaAllaFragaBtn().click()
            }

            sleep(10L)

            waitFor{
                page.unhandledQATable.isDisplayed()
            }

        }
    }

    def aterstallSokformular() {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.resetAdvancedFilter()
            }
        }
    }

    boolean doljsFraga(String id) {
        try {
            return !visasFraga(id)
        } finally {
            sleep(10L)
        }
    }

    boolean enhetsvaljareVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                expected == page.careUnitSelector.isDisplayed()
            }
        }
        true
    }

    def filtreraFragorOchSvar(boolean waitForTableToBeDisplayed = true) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.advancedFilterSearchBtn.click()
            }
            if (waitForTableToBeDisplayed) {
                waitFor {
                    page.unhandledQATable.isDisplayed()
                }
            }
        }
    }

    boolean fraganArSkickadTillFkMeddelandeVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.questionIsSentToFkMessage.isDisplayed()
        }
        result == expected
    }

    def stangFkMeddelande() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.closeSentMessage.click()
            }
        }
    }

    def fragaMedTextVisasIListanMedOhanteradeFragor(String text) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.qaUnhandledPanelWithText(text)
            }
        }
    }

    boolean fragaVisasIListanMedOhanteradeFragor(String id, boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
              expected == page.qaUnhandledPanel(id).isDisplayed()
            }
        }
        true
    }


    boolean fragaVisasIListanMedHanteradeFragor(String id, boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.qaHandledPanel(id).isDisplayed()
            }
        }
        true
    }

    boolean intygMedFragaSvarSidanVisas() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
        }
    }

    boolean intygArRattatMeddelandeVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.certificateRevokedMessage.isDisplayed()
            }
        }
        true
    }

    boolean intygArSkickatTillFkMeddelandeVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.certificateIsSentToFKMessage.isDisplayed()
            }
        }
        true
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

    boolean listaMedOhanteradeFragorVisas() {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.hamtaFler()
            }
            waitFor {
                result = page.unhandledQATable.isDisplayed()
            }
        }
        result
    }

    boolean personnummerSynsForFraga(String internReferens) {
        def result = false

        Browser.drive {
            result = page.patientIdSyns(internReferens)
        }
        result
    }

    def markeraFragaSomHanterad(String id) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.unhandledQAList.isDisplayed();
            }
            page.markAsHandledWcOriginBtn(id).click()
        }
    }

    def valjNyFraga() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.showNewQuestionForm()
        }
    }

    boolean nyFragaFormularVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.newQuestionForm.isDisplayed()
            }
        }
        true
    }

    boolean nyFragaKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.newQuestionBtn.isDisplayed()
            }
        }
        true
    }

    boolean ohanteradeFragorSidanVisas() {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
        }
    }

    boolean skickaFragaKnappAktiverad(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.sendQuestionBtn.isEnabled()
            }
        }
        true
    }

    def skickaFragaMedAmne(String fraga, String amne) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.newQuestionText = fraga
            page.newQuestionTopic = amne
            page.sendQuestion()
            waitFor {
                !page.newQuestionForm.isDisplayed()
            }
        }
    }

    def svaraPaFragaMedSvar(String id, String svar) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.unhandledQAList.isDisplayed();
            }
            page.addAnswerText(id, svar)
            waitFor {
                page.sendAnswer(id)
            }
        }
    }

    def valjLakareMedNamn(String namn) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.advancedFilterSelectDoctor.isDisplayed()
            }
            waitFor {
                page.advancedFilterSelectDoctor = namn
            }
        }
    }

    def valjDatumFran(String datumText) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.advancedFilterChangeDateFrom.isDisplayed()
            }
            page.advancedFilterChangeDateFrom = datumText
        }
    }

    def valjDatumTill(String datumText) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.advancedFilterChangeDateTo.isDisplayed()
            }
            page.advancedFilterChangeDateTo = datumText
        }
    }

    def valjVardenhet(String careUnit) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.selectCareUnit(careUnit);
            }
        }
    }

    def vidarebefordraFraga(String id) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.forwardBtn(id).click()
        }
    }

    def visaAvanceratFilter() {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.showAdvancedFilter()
            }
            waitFor {
                page.advancedFilterForm.isDisplayed()
            }
        }
    }

    def visaFraga(String id) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.showQA(id)
        }
    }

    def valjFragestallare(String fragestallare) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.advancedFilterForm.isDisplayed()
            }
            page.advandecFilterFormFragestallare = fragestallare;
        }
    }

    boolean visasFraga(String id) {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.unhandledQATable.isDisplayed()
            }
            result = page.isQAVisible(id)
        }
        return result
    }

    def gaTillIntygsvyMedIntygsid(String id) {
        Browser.drive {
            go "/web/dashboard#/fragasvar/fk7263/${id}"
            waitFor {
                at VisaFragaSvarPage
            }
        }
    }

    def gaTillIntygsvyViaUthoppMedIntygsid(String id) {
        Browser.drive {
            go "/webcert/web/user/certificate/${id}/questions"
            waitFor {
                at VisaFragaSvarPage
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

    public boolean intygArSkickatTillIntygstjanstenMeddelandeVisas(boolean expected) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.certificateIsSentToITMessage.isDisplayed()
            }
        }
        true
    }

    boolean skrivUtKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.skrivUtBtn.isDisplayed()
            }
        }
        true
    }

    boolean kopieraKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.kopieraBtn.isDisplayed()
            }
        }
        true
    }

    boolean makuleraKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.makuleraBtn.isDisplayed()
            }
        }
        true
    }

    boolean skickaTillFkKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.skickaTillFkBtn.isDisplayed()
            }
        }
        true
    }

    public boolean arFragaHanterad(String internId) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
                page.unhandledQAList.isDisplayed();
            }
            waitFor {
                page.qaHandledPanel(internId)
            }
            result = page.qaHandledPanel(internId).isDisplayed()
        }
        return result
    }

    public void markeraFragaSomOhanterad(String internId) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.qaHandledPanel(internId).isDisplayed()
            }

            page.markAsUnhandledBtn(internId).click()
        }
    }

    public boolean arFragaOhanterad(String internId) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.qaUnhandledPanel(internId).isDisplayed()
            }
        }
        return true
    }

    boolean arFragaVidarebefordrad(String id, boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            // TODO:
        }

    }

    public boolean MarkeraObehandladknappFörFrågaVisas(String internId, boolean expectedVisibility) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.qaHandledPanel(internId).isDisplayed()
            }
            result = page.markAsUnhandledBtn(internId).isDisplayed()
        }
        return result == expectedVisibility
    }

    public boolean FragaMedIdHarFragenamn(String internId, String namn) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.qaUnhandledPanel(internId).isDisplayed()
            }
            waitFor {
                page.frageStallarNamn(internId).isDisplayed()
            }
            result = page.frageStallarNamn(internId).text().contains(namn)
        }
        return result
    }

    public String TotaltAntalOhanteradeFrågorFörAllaEnheterÄr() {
        def result = ""
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.unitstatUnhandledQuestionsBadgde.isDisplayed()
            }
            result = page.unitstatUnhandledQuestionsBadgde.text()
        }
        return result
    }

    public boolean FragaMedIdHarSvarsnamn(String internId, String namn) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.besvarareNamn(internId).isDisplayed()
            }
            result = page.besvarareNamn(internId).text().contains(namn)
        }
        return result
    }

    boolean visasFkRubrik(String id) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkMeddelandeRubrik(id).isDisplayed()
        }
        return result
    }

    boolean visasFkKontakt(String id) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkKontakter(id).isDisplayed()
        }
        return result
    }

    boolean visasFkKompletteringar(String id) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkKompletteringar(id).isDisplayed()
        }
        return result
    }

    boolean fragaMedIdHarText(String id, String text) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.qaFragetext(id).text().contains(text)
        }
        return result
    }

    boolean fragaMedIdHarSvarstext(String id, String text) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.qaSvarstext(id).getAttribute("value").contains(text)
        }
        return result
    }

    boolean intygSaknasVisas() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.intygSaknas.isDisplayed()
            }
        }
        return true
    }

    boolean sokSkrivIntygSidanVisas() {
        Browser.drive {
            waitFor {
                at SokSkrivaIntygPage
            }
        }
    }

    boolean lamnaFragaSvarVarningVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.qaOnlyDialog.isDisplayed()
            }
        }
        true
    }

    def lamnaFragaSvarFortsatt() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.qaOnlyDialogFortsatt.click()
            }
        }
    }

    def lamnaFragaSvarAvbryt() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.qaOnlyDialogCancel.click()
            }
        }
    }

    def lamnaFragaSvar() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.tillbakaButtonClick()
            }
            Thread.sleep(300);
        }
    }

    boolean lamnaFragaSvarEjHanteradDialogVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                expected == page.qaCheckEjHanteradDialog.isDisplayed()
            }
        }
        true
    }

    def klickaPaHanteraKnappen() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.hanteraButtonClick()
            }
        }
    }

    boolean svarArBorta(String id) {
        def result = false;
        Browser.drive {

            waitFor {
                // setting the questions requires an ajax call, we need to wait for a return before moving on.
                doneLoading()
            }

            waitFor {
                at UnhandledQAPage
            }

            result = page.isQAVisible(id)
        }
        return !result;
    }

    def klickaPaEjhanteradKnappen() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.ejHanteraButtonClick()
            }
        }
    }

    boolean svarArMed(String id) {
        def result = false;
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isQAVisible(id)
        }
        return result;
    }

    def klickaPaTillbakaKnappen() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.tillbakaButtonClick()
            }
        }
    }

    def kryssaIVisaInteIgen(){
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.preferenceSkipShowUnhandledCheck()
            }
        }
    }

    def klickaPaHanteraTillbakaKnappen() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.hanteraTillbakaButtonClick()
            }
        }
    }

    boolean infotextIngaFragarPaEnhetVisas(boolean expected = true) {
        def result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            if (expected) {
                waitFor {
                    result = page.noResultsOnUnitInfo.isDisplayed()
                }
            } else {
                result = page.noResultsOnUnitInfo.isDisplayed()
            }
        }
        return result == expected
    }

    boolean infotextIngetSokresultatVisas(boolean expected = true) {
        def result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            if (expected) {
                waitFor {
                    result = page.noResultsForQueryInfo.isDisplayed()
                }
            } else {
                result = page.noResultsForQueryInfo.isDisplayed()
            }
        }
        return result == expected
    }


}
