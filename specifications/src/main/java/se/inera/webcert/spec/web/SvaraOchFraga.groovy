package se.inera.webcert.spec.web
import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.SokSkrivaIntygPage
import se.inera.webcert.pages.UnhandledQAPage
import se.inera.webcert.pages.VisaFragaSvarPage

class SvaraOchFraga {

    void gaTillFragorOchSvar() {
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
            waitFor {
                doneLoading()
            }
            result = page.careUnitSelectorLink.isDisplayed()
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
        def result
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
            waitFor(20.0) {
                result = page.unhandledQAPanelWithText(text)?.isDisplayed()
            }
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
            waitFor(20.0) {
                result = page.handledQAPanel(id)?.isDisplayed()
            }
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
            waitFor {
                result = page.certificateRevokedMessage?.isDisplayed()
            }
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
        def result
        Browser.drive {
            result = page.newQuestionForm?.isDisplayed()
        }
        result
    }

    boolean nyFragaFormularInteVisas() {
        def result

        Browser.drive {
           wait(1000);  // wait 1 second to make sure the little things has rendered
           result = page.newQuestionForm?.isDisplayed()
        }
        result
    }

    boolean nyFragaKnappVisas() {
        def result
        Browser.drive {
            result = page.newQuestionBtn?.isDisplayed()
        }
        result
    }

    boolean ohanteradeFragorSidanVisas() {
        def result
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
        def result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
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
        def result
        Browser.drive {
            waitFor {
                page.certificateIsSentToITMessage.isDisplayed()
            }
            result = page.certificateIsSentToITMessage.isDisplayed()
        }
        result
    }

    boolean skrivUtKnappVisas() {
        def result
        Browser.drive {
            result = page.skrivUtKnappEmployer?.isDisplayed()
        }
        result
    }

    boolean kopieraKnappVisas() {
        def result
        Browser.drive {
            result = page.kopieraKnapp?.isDisplayed()
        }
        result
    }

    boolean makuleraKnappVisas() {
        def result
        Browser.drive {
            result = page.makuleraKnapp?.isDisplayed()
        }
        result
    }

    boolean skickaTillFkKnappVisas() {
        def result
        Browser.drive {
            result = page.skickaKnapp?.isDisplayed()
        }
        result
    }

    public boolean arFragaHanterad(String internId) {
        def result
        Browser.drive {
            waitFor {
                page.handledQAPanel(internId).isDisplayed()
            }
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
        def result
        Browser.drive {
            waitFor {
                page.unhandledQAPanel(internId)?.isDisplayed()
            }
            result = page.unhandledQAPanel(internId).isDisplayed()
        }
        result
    }

    public boolean MarkeraObehandladknappFörFrågaVisas(String internId) {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.qaHandledPanel(internId).isDisplayed()
                page.markAsUnhandledBtn(internId).isDisplayed()
            }
            result = page.markAsUnhandledBtn(internId).isDisplayed()
        }
        result
    }

    public boolean FragaMedIdHarFragenamn(String internId, String namn) {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.qaUnhandledPanel(internId).isDisplayed()
                page.frageStallarNamn(internId).isDisplayed()
            }
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
            waiFor {
                page.unitstatUnhandledQuestionsBadgde.isDisplayed()
            }
            result = page.unitstatUnhandledQuestionsBadgde.text()
        }
        result
    }

    public boolean FragaMedIdHarSvarsnamn(String internId, String namn) {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            waitFor {
                page.besvarareNamn(internId).isDisplayed()
            }
            result = page.besvarareNamn(internId).text().contains(namn)
        }
        result
    }

    boolean visasFkRubrik(String id) {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkMeddelandeRubrik(id)
        }
        result
    }

    boolean visasFkKontakt(String id) {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkKontakter(id)
        }
        result
    }

    boolean visasFkKompletteringar(String id) {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkKompletteringar(id)
        }
        result
    }

    boolean fragaMedIdHarText(String id, String text) {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.qaFragetext(id).text().contains(text)
        }
        result
    }

    boolean fragaMedIdHarSvarstext(String id, String text) {
        def result
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.qaSvarstext(id).getAttribute("value").contains(text)
        }
        result
    }

    boolean intygSaknasVisas() {
        def result
        Browser.drive {
            waitFor {
                result = page.intygSaknas?.isDisplayed()
            }
        }
        result
    }

    boolean sokSkrivIntygSidanVisas() {
        def result
        Browser.drive {
            waitFor {
                result = isAt SokSkrivaIntygPage
            }
        }
        result
    }

    boolean lamnaFragaSvarVarningVisas() {
        def result
        Browser.drive {
            waitFor {
                result = page.qaOnlyDialog?.isDisplayed()
            }
        }
        result
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
        def result
        Browser.drive {
            waitFor {
                result = page.qaCheckEjHanteradDialog.isDisplayed()
            }
        }
        result
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
        def result
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
        def result
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
            // TODO move to page abstraction
            page.hanteraTillbakaButtonClick()

            // also need to wait for the dialog shim to hide
            page.waitForModalBackdropToHide();
        }
    }

    boolean infotextIngaFragarPaEnhetVisas() {
        boolean result
        Browser.drive {
            isAt UnhandledQAPage
            result = page.noResultsOnUnitInfo?.isDisplayed()
        }
        result
    }

    boolean infotextIngetSokresultatVisas() {
        boolean result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.noResultsForQueryInfo?.isDisplayed()
        }
        result
    }

    def visasAllaFragorKnappen() {
        def result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.visaAllaFragaBtn?.isDisplayed()
        }
        result
    }

    boolean visasEnhetsknappen(String id) {
        boolean result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isCareUnitVisible(id)
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
            waitFor {
                page.careUnitModalBody.isDisplayed()

            }
            page.isCareUnitModalVisible(id)
            waitFor {
                result = page.careUnitModalBody?.isDisplayed()
            }
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

    boolean vidareBefordraKnappVisas() {
        def ref = "#qaTable button.vidarebefordra-btn"
        boolean result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = $(ref).isDisplayed()
        }
        result
    }

    boolean vidarebefordradCheckboxVisas() {
        def ref = "#qaTable input.vidarebefordrad-checkbox"
        boolean result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = $(ref).isDisplayed()
        }
        result
    }

    boolean vardenhetValjareVisas() {
        def ref = "div#wc-care-unit-clinic-selector"
        boolean result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = $(ref).isDisplayed()
        }
        result
    }

    boolean vidarebefordraKnappInnePaFragaVisas() {
        boolean result = false
        Browser.drive {
            isAt VisaFragaSvarPage
            result = page.vidarebefordradKnappPaFraga.isDisplayed()
        }
        result
    }

    boolean filterVidarebefordradVisas() {
        boolean result = false
        Browser.drive {
            isAt UnhandledQAPage
            result = page.filterVidarebefordrad?.isDisplayed()
        }
        result
    }

    boolean filterSparatAvVisas() {
        boolean result = false
        Browser.drive {
            isAt UnhandledQAPage
            result = page.filterSparatAv?.isDisplayed()
        }
        result
    }

    boolean filterSigneratAvVisas() {
        boolean result = false
        Browser.drive {
            isAt UnhandledQAPage
            result = page.filterSigneratAv?.isDisplayed()
        }
        result
    }

}
