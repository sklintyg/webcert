package se.inera.webcert.spec

import se.inera.webcert.pages.IndexPage
import se.inera.webcert.pages.UnhandledQAPage
import se.inera.webcert.pages.fk7263.ViewCertQAPage
import se.inera.webcert.pages.WelcomePage

class SvaraOchFraga {

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
        return !visasFraga(id)
    }

    boolean enhetsvaljareVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.careUnitSelector.isDisplayed()
        }
        result == expected
    }

    def filtreraFragorOchSvar() {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.advancedFilterSearchBtn.click()
            }
            waitFor {
                page.unhandledQATable.isDisplayed()
            }
        }
    }

    boolean fraganArSkickadTillFkMeddelandeVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.questionIsSentToFkMessage.isDisplayed()
        }
        result == expected
    }


    def fragaMedTextVisasIListanMedOhanteradeFragor(String text) {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.qaUnhandledPanelWithText(text)
            }
        }
    }

    boolean fragaVisasIListanMedOhanteradeFragor(String id, boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.qaUnhandledPanel(id) && page.qaUnhandledPanel(id).isDisplayed()
        }
        result == expected
    }


    boolean fragaVisasIListanMedHanteradeFragor(String id, boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            sleep(1000L)
            result = page.qaHandledPanel(id) && page.qaHandledPanel(id).isDisplayed()
        }
        result == expected
    }

    boolean intygMedFragaSvarSidanVisas() {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
        }
    }

    boolean intygArRattatMeddelandeVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.certificateRevokedMessage.isDisplayed()
        }
        result == expected
    }

    boolean intygArSkickatTillFkMeddelandeVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.certificateIsSentToFKMessage.isDisplayed()

        }
        result == expected
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
                result = page.unhandledQATable.isDisplayed()
            }
        }
        result
    }

    def markeraFragaSomHanterad(String id) {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.unhandledQAList.isDisplayed();
            }
            page.markAsHandledWcOriginBtn(id).click()
            sleep(1000L)
        }
    }

    def valjNyFraga() {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            page.showNewQuestionForm()
        }
    }

    boolean nyFragaFormularVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.newQuestionForm.isDisplayed()
        }
        result == expected
    }

    boolean nyFragaKnappVisas(boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.newQuestionBtn.isDisplayed()

        }
        result == expected
    }

    boolean ohanteradeFragorSidanVisas() {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
        }
    }

    boolean skickaFragaKnappAktiverad(boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.sendQuestionBtn.isEnabled()
        }
        result == expected
    }

    def skickaFragaMedAmne(String fraga, String amne) {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
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
                at ViewCertQAPage
            }
            waitFor {
                page.unhandledQAList.isDisplayed();
            }
            page.addAnswerText(id, svar)
            waitFor {
                page.sendAnswer(id)
            }
            sleep(1000L)
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
                at ViewCertQAPage
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
            go "/m/fk7263/webcert/intyg/${id}#view"
            waitFor {
                at ViewCertQAPage
            }
        }
    }

    def gaTillIntygsvyViaUthoppMedIntygsid(String id) {
        Browser.drive {
            go "/medcert/web/user/certificate/${id}/questions"
            waitFor {
                at ViewCertQAPage
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

    public boolean intygÄrSkickatTillIntygstjänstenMeddelandeVisas(boolean expected) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.certificateIsSentToITMessage.isDisplayed()

        }
        result == expected
    }


    public boolean arFragaHanterad(String internId) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
                page.unhandledQAList.isDisplayed();
            }

            waitFor {
                result = page.qaHandledPanel(internId).isDisplayed()
            }
        }
        return result
    }

    public void markeraFragaSomOhanterad(String internId) {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.qaHandledPanel(internId).isDisplayed()
            }

            page.markAsUnhandledBtn(internId).click()
        }
    }

    public boolean arFragaOhanterad(String internId) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }

            waitFor {
                result = page.qaUnhandledPanel(internId).isDisplayed()
            }
        }
        return result
    }

    boolean arFragaVidarebefordrad(String id, boolean expected = true) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            // TODO:
        }

    }

    public boolean MarkeraObehandladknappFörFrågaVisas(String internId, boolean expectedVisibility) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.qaHandledPanel(internId).isDisplayed()
            }
            result = page.markAsUnhandledBtn(internId).isDisplayed()
        }
        return result == expectedVisibility
    }

    public boolean FrågaMedIdHarFrågenamn(String internId, String namn) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
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

    public boolean FrågaMedIdHarSvarsnamn(String internId, String namn) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
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
                at ViewCertQAPage
            }
            result = page.fkMeddelandeRubrik(id).isDisplayed()
        }
        return result
    }

    boolean visasFkKontakt(String id) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.fkKontakter(id).isDisplayed()
        }
        return result
    }

    boolean visasFkKompletteringar(String id) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.fkKompletteringar(id).isDisplayed()
        }
        return result
    }

    boolean fragaMedIdHarText(String id, String text) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.qaFragetext(id).text().contains(text)
        }
        return result
    }

    boolean fragaMedIdHarSvarstext(String id, String text) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.qaSvarstext(id).getAttribute("value").contains(text)
        }
        return result
    }


    boolean intygFält1Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            if (visatVarde.equalsIgnoreCase("yes")) {
                waitFor {
                    page.field1yes.isDisplayed()
                }
                result = page.field1yes.text().contains(visatVarde)
            } else {
                waitFor {
                    page.field1no.isDisplayed()
                }
                result = page.field1no.text().contains(visatVarde)
            }
        }
        return result
    }

    boolean intygFält2Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field2.isDisplayed()
            }
            result = page.field2.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält3Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field3.isDisplayed()
            }
            result = page.field3.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält4Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field4.isDisplayed()
            }
            result = page.field4.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält4bVisas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field4b.isDisplayed()
            }
            result = page.field4b.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält5Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field5.isDisplayed()
            }
            result = page.field5.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält6aVisas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field6a.isDisplayed()
            }
            result = page.field6a.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält6bVisas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field6b.isDisplayed()
            }
            result = page.field6b.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält7Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field7.isDisplayed()
            }
            result = page.field7.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält8aVisas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field8a.isDisplayed()
            }
            result = page.field8a.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält8bVisas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field8b.isDisplayed()
            }
            result = page.field8b.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält9Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field9.isDisplayed()
            }
            result = page.field9.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält10Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field10.isDisplayed()
            }
            result = page.field10.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält11Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field11.isDisplayed()
            }
            result = page.field11.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält12Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field12.isDisplayed()
            }
            result = page.field12.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält13Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field13.isDisplayed()
            }
            result = page.field13.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFält17Visas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field17.isDisplayed()
            }
            result = page.field17.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFältVarpersonNamnVisas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field_vardperson_namn.isDisplayed()
            }
            result = page.field_vardperson_namn.text().contains(visatVarde)
        }
        return result
    }

    boolean intygFältVarpersonEnhetsnamnVisas(String visatVarde) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.field_vardperson_enhetsnamn.isDisplayed()
            }
            result = page.field_vardperson_enhetsnamn.text().contains(visatVarde)
        }
        return result
    }
}
