package se.inera.webcert.spec.web

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.IndexPage
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.UnhandledQAPage
import se.inera.webcert.pages.VisaFragaSvarPage
import se.inera.webcert.pages.WelcomePage

class SvaraOchFraga {

    void gaTillSvaraOchFraga() {
        Browser.drive {
            go "/web/dashboard#/unhandled-qa"
            waitFor {
                at UnhandledQAPage
            }
        }
    }

    boolean svaraOchFragaSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt UnhandledQAPage
            }
        }
        result
    }

    def gaTillSokSkrivaIntyg() {
        Browser.drive {
            go "/web/dashboard#/create/index"
            waitFor {
                doneLoading()
            }
        }
    }

    boolean sokSkrivaIntygSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt SokSkrivaIntygPage
            }
        }
        result
    }

    void visaAllaFragor() {
        Browser.drive {
            page.visaAllaFragor()
        }
    }

    void aterstallSokformular() {
        Browser.drive {
            page.resetAdvancedFilter()
        }
    }

    boolean enhetsvaljareVisas() {
        boolean result
        Browser.drive {
            result = page.careUnitSelector.isDisplayed()
        }
        result
    }

    void filtreraFragorOchSvar() {
        Browser.drive {
            page.advancedFilterSearchBtn.click()
            waitFor {
                doneLoading()
            }
        }
    }

    boolean fraganArSkickadTillFkMeddelandeVisas() {
        boolean result
        Browser.drive {
            result = page.questionIsSentToFkMessage.isDisplayed()
        }
        result
    }

    void stangFkMeddelande() {
        Browser.drive {
            page.closeSentMessage.click()
            waitFor {
                doneLoading()
            }
        }
    }

    def fragaMedTextVisasIListanMedOhanteradeFragor(String text) {
        boolean result
        Browser.drive {
            result = page.unhandledQAPanelWithText(text)?.isDisplayed()
        }
        result
    }

    boolean fragaVisasIListanMedOhanteradeFragor(String id) {
        boolean result
        Browser.drive {
            result = page.unhandledQAPanel(id)?.isDisplayed()
        }
        result
    }

    boolean fragaVisasIListanMedHanteradeFragor(String id) {
        boolean result
        Browser.drive {
            result = page.handledQAPanel(id)?.isDisplayed()
        }
        result
    }

    boolean intygMedFragaSvarSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt VisaFragaSvarPage
            }
        }
        result
    }

    boolean intygArRattatMeddelandeVisas() {
        boolean result
        Browser.drive {
            result = page.certificateRevokedMessage?.isDisplayed()
        }
        result
    }

    boolean intygArSkickatTillFkMeddelandeVisas() {
        boolean result
        Browser.drive {
            result = page.certificateIsSentToFKMessage?.isDisplayed()
        }
        result
    }

    boolean listaMedOhanteradeFragorVisas() {
        boolean result
        Browser.drive {
            waitFor{
                at UnhandledQAPage
            }
            page.hamtaFler()
            result = page.unhandledQATable?.isDisplayed()
        }
        result
    }

    boolean personnummerSynsForFraga(String internReferens) {
        boolean result
        Browser.drive {
            result = page.patientIdSyns(internReferens)
        }
        result
    }

    void markeraFragaSomHanterad(String id) {
        Browser.drive {
            page.markQuestionAsHandled(id)
        }
    }

    void valjNyFraga() {
        Browser.drive {
            page.showNewQuestionForm()
        }
    }

    boolean nyFragaFormularVisas() {
        boolean result
        Browser.drive {
            result = page.newQuestionForm.isDisplayed()
        }
        result
    }

    boolean nyFragaKnappVisas() {
        boolean result
        Browser.drive {
            result = page.newQuestionBtn.isDisplayed()
        }
        result
    }

    boolean ohanteradeFragorSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt UnhandledQAPage
            }
        }
        result
    }

    boolean skickaFragaKnappAktiverad() {
        boolean result
        Browser.drive {
            result = !page.sendQuestionBtn.isDisabled()
        }
        result
    }

    void skickaFragaMedAmne(String fraga, String amne) {
        Browser.drive {
            page.newQuestionText = fraga
            page.newQuestionTopic = amne
            page.sendQuestion()
        }
    }

    void svaraPaFragaMedSvar(String id, String svar) {
        Browser.drive {
            page.unhandledQAList.isDisplayed()
            page.addAnswerText(id, svar)
            page.sendAnswer(id)
        }
    }

    void valjLakareMedNamn(String namn) {
        Browser.drive {
            page.advancedFilterSelectDoctor = namn

        }
    }

    void valjDatumFran(String datumText) {
        Browser.drive {
            page.advancedFilterChangeDateFrom = datumText
        }
    }

    void valjDatumTill(String datumText) {
        Browser.drive {
            page.advancedFilterChangeDateTo = datumText
        }
    }

    void valjVardenhet(String careUnit) {
        Browser.drive {
            page.selectCareUnit(careUnit)
        }
    }

    void vidarebefordraFraga(String id) {
        Browser.drive {
            page.forwardBtn(id).click()
            waitFor {
                page.doneLoading()
            }
        }
    }

    void visaAvanceratFilter() {
        Browser.drive {
            page.showAdvancedFilter()
        }
    }

    void visaFraga(String id) {
        Browser.drive {
            page.showQA(id)
        }
    }

    void valjFragestallare(String fragestallare) {
        Browser.drive {
            page.advandecFilterFormFragestallare = fragestallare
        }
    }

    boolean fragaVisas(String id) {
        boolean result
        Browser.drive {
            result = page.isQAVisible(id)
        }
        result
    }

    void gaTillIntygsvyMedIntygsid(String id) {
        Browser.drive {
            go "/web/dashboard#/fragasvar/fk7263/${id}"
            waitFor {
                at VisaFragaSvarPage
            }
        }
    }

    void gaTillIntygsvyViaUthoppMedIntygsid(String id) {
        Browser.drive {
            go "/webcert/web/user/certificate/${id}/questions"
            waitFor {
                at VisaFragaSvarPage
            }
        }
    }

    boolean intygArSkickatTillIntygstjanstenMeddelandeVisas() {
        boolean result
        Browser.drive {
            result = page.certificateIsSentToITMessage.isDisplayed()
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
            result = page.kopieraKnapp.isDisplayed()
        }
        result
    }

    boolean makuleraKnappVisas() {
        boolean result
        Browser.drive {
            result = page.makuleraKnapp.isDisplayed()
        }
    }

    boolean skickaTillFkKnappVisas() {
        boolean result
        Browser.drive {
            result = page.skickaKnapp.isDisplayed()
        }
        result
    }

    public boolean arFragaHanterad(String internId) {
        boolean result
        Browser.drive {
            result = page.handledQAPanel(internId).isDisplayed()
        }
        result
    }

    public void markeraFragaSomOhanterad(String internId) {
        Browser.drive {
            page.markAsUnhandled(internId)
        }
    }

    public boolean arFragaOhanterad(String internId) {
        boolean result
        Browser.drive {
            result = page.unhandledQAPanel(internId).isDisplayed()
        }
        result
    }

    boolean arFragaVidarebefordrad(String id) {
        boolean result
        Browser.drive {
            // TODO:
        }
        result
    }

    public boolean MarkeraObehandladknappFörFrågaVisas(String internId) {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.qaHandledPanel(internId).isDisplayed()

            result = page.markAsUnhandledBtn(internId).isDisplayed()
        }
        result
    }

    public boolean FragaMedIdHarFragenamn(String internId, String namn) {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.qaUnhandledPanel(internId).isDisplayed()

            page.frageStallarNamn(internId).isDisplayed()

            result = page.frageStallarNamn(internId).text().contains(namn)
        }
        result
    }

    public String TotaltAntalOhanteradeFrågorFörAllaEnheterÄr() {
        String result = ""
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.unitstatUnhandledQuestionsBadgde.isDisplayed()

            result = page.unitstatUnhandledQuestionsBadgde.text()
        }
        result
    }

    public boolean FragaMedIdHarSvarsnamn(String internId, String namn) {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.besvarareNamn(internId).isDisplayed()

            result = page.besvarareNamn(internId).text().contains(namn)
        }
        result
    }

    boolean visasFkRubrik(String id) {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkMeddelandeRubrik(id)
        }
        result
    }

    boolean visasFkKontakt(String id) {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkKontakter(id)
        }
        result
    }

    boolean visasFkKompletteringar(String id) {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkKompletteringar(id)
        }
        result
    }

    boolean fragaMedIdHarText(String id, String text) {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.qaFragetext(id).text().contains(text)
        }
        result
    }

    boolean fragaMedIdHarSvarstext(String id, String text) {
        boolean result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.qaSvarstext(id).getAttribute("value").contains(text)
        }
        result
    }

    boolean intygSaknasVisas() {
        boolean result
        Browser.drive {
            result = page.intygSaknas.isDisplayed()
        }
        result
    }

    boolean sokSkrivIntygSidanVisas() {
        boolean result
        Browser.drive {
            waitFor {
                result = isAt SokSkrivaIntygPage
            }
        }
        result
    }

    boolean lamnaFragaSvarVarningVisas() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            return page.qaOnlyDialog.isDisplayed()

        }
    }

    def lamnaFragaSvarFortsatt() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.qaOnlyDialogFortsatt.click()

        }
    }

    def lamnaFragaSvarAvbryt() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.qaOnlyDialogCancel.click()
        }
    }

    void lamnaFragaSvar() {
        Browser.drive {
            page.tillbaka()
        }
    }

    boolean lamnaFragaSvarEjHanteradDialogVisas() {
        Browser.drive {
            return page.qaCheckEjHanteradDialog.isDisplayed()
        }
    }

    def klickaPaHanteraKnappen() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.hanteraButtonClick()

        }
    }

    boolean svarArBorta(String id) {
        boolean result
        Browser.drive {
            waitFor{
                at UnhandledQAPage
            }
            result = !page.isQAVisible(id)
        }
        result
    }

    void klickaPaEjhanteradKnappen() {
        Browser.drive {
            page.ejHanteraButtonClick()
        }
    }

    boolean svarArMed(String id) {
        boolean result
        Browser.drive {
            result = page.isQAVisible(id)
        }
        result
    }

    void klickaPaTillbakaKnappen() {
        Browser.drive {
            page.tillbakaButtonClick()
        }
    }

    def kryssaIVisaInteIgen() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.preferenceSkipShowUnhandledCheck()

        }
    }

    def klickaPaHanteraTillbakaKnappen() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.hanteraTillbakaButtonClick()

            // also need to wait for the dialog shim to hide
            page.waitForModalBackdropToHide();
        }
    }

    boolean infotextIngaFragarPaEnhetVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.noResultsOnUnitInfo.isDisplayed()
        }
        result
    }

    boolean infotextIngetSokresultatVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.noResultsForQueryInfo.isDisplayed()
        }
        result
    }

    def visasAllaFragorKnappen(boolean expected = true) {
        boolean result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.visaAllaFragaBtn.isDisplayed()
        }
        result
    }

    boolean visasEnhetsknappen(String id, boolean expected = true) {
        boolean result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isCareUnitVisible(id, expected)
        }
        result
    }

    boolean forEnhetenArSiffran(String id, String expected) {
        boolean result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isNumberPresent(id, expected)
        }
        result
    }

    def klickaUppModal() {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.clickCareUnitModal()
            page.modalIsDisplayed()

        }
    }

    boolean visasEnhetsvaletIModal(String id) {
        boolean result
        Browser.drive {
            page.careUnitModalBody.isDisplayed()
            result = page.careUnitModalBody.isDisplayed() && page.isCareUnitModalVisible(id)
        }
        result
    }

    boolean forEnhetenIModalenArSiffran(String id, String expected) {
        boolean result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isNumberPresentInModal(id, expected)
        }
        result
    }

    def expanderaIModal(String id) {
        Browser.drive {
            page.modalIsDisplayed()
            page.expandEnhetModal(id)
            page.modalIsDisplayed()
        }
    }

    def valjEnhetIModal(String id) {
        Browser.drive {
            page.modalIsDisplayed()

            page.selectCareUnitModal(id)
        }
    }

    boolean vidareBefordraKnappVisas(boolean expected) {
        boolean result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = $("#qaTable button.vidarebefordra-btn").isDisplayed()
        }
    }

    boolean vidarebefordradCheckboxVisas(boolean expected) {
        boolean result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = $("#qaTable input.vidarebefordrad-checkbox").isDisplayed()

        }
        return expected == result
    }

    boolean vardenhetValjareVisas(boolean expected) {
        boolean result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = $("div#wc-care-unit-clinic-selector").isDisplayed()
        }
        return expected == result
    }

    boolean vidarebefordraKnappInnePaFragaVisas(boolean expected) {
        boolean result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            // TODO use GEB page abstraction
            result = $("#unhandled-vidarebefordraEjHanterad").isDisplayed()
        }
        return expected == result
    }


    boolean filterVidarebefordradVisas(boolean expected) {
        boolean result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = $('#filterFormVidarebefordrad').isDisplayed()
        }
        return expected == result
    }

    boolean filterValjLakareVisas(boolean expected) {
        boolean result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = $('#filterFormSigneratAv').isDisplayed()
        }
        return expected == result
    }
}
