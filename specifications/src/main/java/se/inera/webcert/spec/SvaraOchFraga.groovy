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

            page.visaAllaFragaBtn().click()

            page.unhandledQATable.isDisplayed()

        }
    }

    def aterstallSokformular() {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.resetAdvancedFilter()
        }
    }

    boolean doljsFraga(String id) {
        try {
            return visasFraga(id, false)
        } finally {
//            sleep(10L)
        }
    }

    boolean enhetsvaljareVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            if (expected) {
                return page.careUnitSelector.isDisplayed()
            } else {
                return !page.careUnitSelectorNoWait.present || !page.careUnitSelectorNoWait.isDisplayed()
            }
        }
    }

    def filtreraFragorOchSvar(boolean expected = true) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            if(expected){
                page.advancedFilterSearchBtn.click()
                page.unhandledQATable.isDisplayed()
            } else {
                page.advancedFilterSearchBtn.click()
                !page.unhandledQATableNoWait.isDisplayed()
            }

        }
    }

    boolean fraganArSkickadTillFkMeddelandeVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            if(expected){
                return page.questionIsSentToFkMessage.isDisplayed()
            } else {
                return !page.questionIsSentToFkMessageNoWait.isDisplayed()
            }

        }
    }

    def stangFkMeddelande() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.closeSentMessage.click()
        }
    }

    def fragaMedTextVisasIListanMedOhanteradeFragor(String text) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            return page.qaUnhandledPanelWithText(text)
        }
    }

    boolean fragaVisasIListanMedOhanteradeFragor(String id, boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            return page.qaUnhandledPanel(id, expected)

        }
    }


    boolean fragaVisasIListanMedHanteradeFragor(String id, boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            return page.qaHandledPanel(id, expected)

        }
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
            return page.certificateRevokedMessage.isDisplayed()

        }
    }

    boolean intygArSkickatTillFkMeddelandeVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            if(expected){

                return page.certificateIsSentToFKMessage.isDisplayed()

            } else {

                return !page.certificateIsSentToFKMessageNoWait.isDisplayed()

            }
        }
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
            page.hamtaFler()
            result = page.unhandledQATable.isDisplayed()
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
            page.unhandledQAList.isDisplayed();
            page.markAsHandledWcOriginBtnClick(id);
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
            if(expected){
                return page.newQuestionForm.isDisplayed()
            } else {
                return !page.newQuestionFormNoWait.isDisplayed()
            }
        }
    }

    boolean nyFragaKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            if(expected){

                return page.newQuestionBtn.isDisplayed()

            } else {
                return !page.newQuestionBtnNoWait.isDisplayed()

            }
        }
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
            if(expected){
                return !page.isButtonDisabled(page.sendQuestionBtn)
            } else {
                return page.isButtonDisabled(page.sendQuestionBtn)
            }

        }
    }

    boolean skickaFragaMedAmne(String fraga, String amne) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.newQuestionText = fraga
            page.newQuestionTopic = amne
            page.sendQuestion()
            !page.newQuestionFormNoWait.isDisplayed()
        }
    }

    def svaraPaFragaMedSvar(String id, String svar) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.unhandledQAList.isDisplayed();
            page.addAnswerText(id, svar)
            page.sendAnswer(id)
        }
    }

    def valjLakareMedNamn(String namn) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.advancedFilterSelectDoctor.isDisplayed()
            page.advancedFilterSelectDoctor = namn

        }
    }

    def valjDatumFran(String datumText) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.advancedFilterChangeDateFrom.isDisplayed()
            page.advancedFilterChangeDateFrom = datumText
        }
    }

    def valjDatumTill(String datumText) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.advancedFilterChangeDateTo.isDisplayed()
            page.advancedFilterChangeDateTo = datumText
        }
    }

    def valjVardenhet(String careUnit) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.selectCareUnit(careUnit);

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
            page.showAdvancedFilter()
            page.advancedFilterForm.isDisplayed()

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

    def visaEjFraga(String id) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            !page.showQANoWait(id)
        }
    }

    def valjFragestallare(String fragestallare) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.advancedFilterForm.isDisplayed()

            page.advandecFilterFormFragestallare = fragestallare;
        }
    }

    boolean visasFraga(String id, boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.unhandledQATable.isDisplayed()
            result = page.isQAVisible(id, expected)
        }
        return result
    }

    boolean visasEjFraga(String id) {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isQAVisible(id, false)
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
            return page.certificateIsSentToITMessage.isDisplayed()

        }
    }

    boolean skrivUtKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            return page.skrivUtBtn.isDisplayed()

        }
    }

    boolean kopieraKnappVisas() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            return page.kopieraBtn.isDisplayed()

        }
    }

    boolean kopieraKnappEjVisas() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            return !page.kopieraBtnNoWait.isDisplayed()

        }
    }

    boolean makuleraKnappVisas() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            return page.makuleraBtn.isDisplayed()

        }
    }

    boolean makuleraKnappVisasEj() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            return page.makuleraBtnNoWait.isDisplayed()

        }
    }

    boolean skickaTillFkKnappVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            if(expected){
                return skickaTillFkBtn.isDisplayed()
            } else {
                return !skickaTillFkBtnNoWait.isDisplayed()
            }

        }
    }

    public boolean arFragaHanterad(String internId, boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.unhandledQAList.isDisplayed();

            result = page.qaHandledPanel(internId, expected)
        }
        return result
    }

    public void markeraFragaSomOhanterad(String internId, boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.qaHandledPanel(internId, expected)

            page.markAsUnhandledBtnClick(internId, expected)
        }
    }

    public boolean arFragaOhanterad(String internId, boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            return page.qaUnhandledPanel(internId, expected)

        }
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
            page.qaHandledPanel(internId).isDisplayed()

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
            page.qaUnhandledPanel(internId).isDisplayed()

            page.frageStallarNamn(internId).isDisplayed()

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
            page.unitstatUnhandledQuestionsBadgde.isDisplayed()

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
            page.besvarareNamn(internId).isDisplayed()

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
            result = page.fkMeddelandeRubrik(id)
        }
        return result
    }

    boolean visasFkKontakt(String id) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkKontakter(id)
        }
        return result
    }

    boolean visasFkKompletteringar(String id) {
        def result = false
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            result = page.fkKompletteringar(id)
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
            return page.intygSaknas.isDisplayed()

        }
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

    def lamnaFragaSvar() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.tillbakaButtonClick()
        }
    }

    boolean lamnaFragaSvarEjHanteradDialogVisas(boolean expected = true) {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
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
        def result = false;
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isQAVisible(id, false)
        }
        return result;
    }

    def klickaPaEjhanteradKnappen() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
            page.ejHanteraButtonClick()

        }
    }

    boolean svarArMed(String id) {
        def result = false;
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isQAVisible(id, true)
        }
        return result;
    }

    def klickaPaTillbakaKnappen() {
        Browser.drive {
            waitFor {
                at VisaFragaSvarPage
            }
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

    boolean infotextIngaFragarPaEnhetVisas(boolean expected = true) {
        def result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            if (expected) {
                result = page.noResultsOnUnitInfo.isDisplayed()

            } else {
                result = !page.noResultsOnUnitInfoNoWait.isDisplayed()
            }
        }
        return result
    }

    boolean infotextIngetSokresultatVisas(boolean expected = true) {
        def result
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            if (expected) {
                result = page.noResultsForQueryInfo.isDisplayed()

            } else {
                result = !page.noResultsForQueryInfoNoWait.isDisplayed()
            }
        }
        return result
    }

    def visasAllaFragorKnappen(boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.visaAllaFragaBtn.isDisplayed()
        }
        return result
    }

    boolean visasEnhetsknappen(String id, boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isCareUnitVisible(id, expected)
        }
        return result
    }

    boolean forEnhetenArSiffran(String id, String expected) {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isNumberPresent(id, expected)
        }
        return result
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

    boolean visasEnhetsvaletIModal(String id, boolean expected = true) {
        def result = false
        Browser.drive {
            page.careUnitModalBody.isDisplayed();
            result = page.isCareUnitModalVisible(id, expected)
        }
        return result
    }

    boolean forEnhetenIModalenArSiffran(String id, String expected) {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.isNumberPresentInModal(id, expected)
        }
        return result
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

}