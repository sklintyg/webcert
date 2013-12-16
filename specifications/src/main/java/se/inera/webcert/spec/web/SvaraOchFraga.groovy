package se.inera.webcert.spec.web

import geb.Browser
import se.inera.webcert.spec.web.pages.IndexPage
import se.inera.webcert.spec.web.pages.UnhandledQAPage
import se.inera.webcert.spec.web.pages.WelcomePage
import se.inera.webcert.spec.web.pages.fk7264.ViewCertQAPage
//import se.inera.certificate.web.pages.ArchivedPage

public class SvaraOchFraga {

    public void loggaPåSom(String id) {
        Browser.drive {
            go "/welcome.jsp"

            waitFor{
                at WelcomePage
            }
            page.userSelect=id

            page.startLogin()
        }
    }

    public void GåTillIntygsVyMedIntygsId(String id) {
        Browser.drive {
            go "/m/fk7263/webcert/intyg/" + id + "#view"

            waitFor{
                at ViewCertQAPage
            }
            
        }
    }
    public void loggaInIndex() {
        Browser.drive {

            waitFor{
                at IndexPage
            }


            page.startLogin()
        }
    }

                   
    public boolean enhetsvaljareVisas(boolean expected) {
    def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            result = page.careUnitSelector.isDisplayed()
            
        }
        result == expected
    }

    public boolean obehandladeFragarSidanVisas(){
        Browser.drive {
           waitFor {
               at UnhandledQAPage
           }
        }
    }
    
    public boolean listaMedObehandladeFragarVisas() {
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


    public void visaFraga(String externid) {
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            page.showQA(externid)
        }
    }

    public void intygMedFragaSvarSidanVisas() {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
        }
    }
    public void fragaVisasIBehandladlistan(String internid) {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor{
                page.qaHandledPanel(internid).isDisplayed()
            }
        }
    }
    
    public boolean svaraPaFragaMedSvar(String internid, String svar) {
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.unhandledQAList.isDisplayed();
            }
            page.addAnswerText(internid, svar)

            waitFor {
                page.sendAnswer(internid)
            }
        }
    }
    
    public boolean nyFragaKnappVisas(boolean expected) {
        def result = false
            Browser.drive {
                waitFor {
                    at ViewCertQAPage
                }
                result = page.askQuestionBtn.isDisplayed()
                
            }
            result == expected
        }
    
    public boolean intygÄrRättatmeddelandeVisas(boolean expected) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.certificateRevokedMessage.isDisplayed()
            
        }
        result == expected
    }
    public boolean intygÄrSkickatTillFKmeddelandeVisas(boolean expected) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.certificateIsSentToFKMessage.isDisplayed()
            
        }
        result == expected
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
    public boolean stallFragaMedAmne(String fraga, String amne){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.unhandledQAList.isDisplayed();
            }

            page.initQuestion();

            waitFor {

                page.newQuestionForm.isDisplayed()
            }
            page.selectSubject(amne)
            page.addQuestionText(fraga)

            waitFor {
                result = page.sendQuestion()
            }

            waitFor {

                !page.newQuestionForm.isDisplayed()
            }
            sleep(1000L)
        }
        result
    }

    public void valjVardenhet(String careunit){
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.selectCareUnit(careunit);
            }

        }
    }
    public void markeraFragaSomHanterad(String internId){
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor {
                page.unhandledQAList.isDisplayed();
            }

            page.markAsHandledWcOriginBtn(internId).click()

            sleep(1000L)
        }
    }
    public boolean arFragaHanterad(String internId){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
                page.unhandledQAList.isDisplayed();
            }

            waitFor{
                result = page.qaHandledPanel(internId).isDisplayed()
            }
        }
        return result
    }

    public void markeraFragaSomOhanterad(String internId){
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor{
                page.qaHandledPanel(internId).isDisplayed()
            }

            page.markAsUnhandledBtn(internId).click()
        }
    }

    public boolean arFragaOhanterad(String internId){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }

            waitFor{
                result = page.qaUnhandledPanel(internId).isDisplayed()
            }
        }
        return result
    }
    
    public boolean MarkeraObehandladknappFörFrågaVisas(String internId, boolean expectedVisibility){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor{
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
            waitFor{
                page.qaUnhandledPanel(internId).isDisplayed()
            }
            
            waitFor{
                page.frageStallarNamn(internId).isDisplayed()
            }
            
            result = page.frageStallarNamn(internId).text().contains(namn)
           
        }
        return result
    }
    public boolean TotaltAntalOhanteradeFrågorFörAllaEnheterÄr(String expected) {
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            
            waitFor{
                page.unitstatUnhandledQuestionsBadgde.isDisplayed()
            }
            
            result = page.unitstatUnhandledQuestionsBadgde.text()
           
        }
        return result == expected
    }
    
    
    public boolean FrågaMedIdHarSvarsnamn(String internId, String namn) {
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            waitFor{
                page.besvarareNamn(internId).isDisplayed()
            }
            
            result = page.besvarareNamn(internId).text().contains(namn)
           
        }
        return result
    }
    
    public void visaAvanceratFilter(){
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }

            waitFor{
                page.showAdvancedFilter()
            }
            waitFor{
                page.advancedFilterForm.isDisplayed()
            }
        }
    }
    public void valjFragestallare(String fragestallare){
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor{
                page.advancedFilterForm.isDisplayed()
            }
            if(fragestallare.equalsIgnoreCase("fk"))
            {
                selectFragestallareFK();
            }else if(fragestallare.equalsIgnoreCase("wc"))
            {
                selectFragestallareWC();
            }else {
                selectFragestallareAlla();
            }
        }
    }

    public void valjLakareMedNamn(String namn){
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor{
                page.advancedFilterSelectDoctor.isDisplayed()
            }
            
            waitFor{
                page.advancedFilterSelectDoctor = namn
            }
        }
    }
    
    public void valjSvarSenast(String svarSenast){
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor{
                page.advancedFilterForm.isDisplayed()
            }
            page.replyBy << svarSenast
        }
    }

    public boolean visasFraga(String internid){
        def result = false
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }
            waitFor {
                page.unhandledQATable.isDisplayed()
            }

                result = page.isQAVisible( internid)

        }
        return result
    }

    public boolean doljsFraga(String internid){
        def result = false
        result = visasFraga(internid)
        return !result
    }



    public void filtreraFragorOchSvar(){
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }

            waitFor{
                page.filterBtn.click()
            }

            waitFor{
                page.unhandledQATable.isDisplayed()
            }
        }
    }

    public void aterstallSokformular(){
        Browser.drive {
            waitFor {
                at UnhandledQAPage
            }

            waitFor{
                page.resetCookie()
            }
        }
    }

    public boolean visasFkRubrik(String internid){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }

            result = page.fkMeddelandeRubrik(internid).isDisplayed()
        }
        return result
    }
    public boolean visasFkKontakt(String internid){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }

            result = page.fkKontakter(internid).isDisplayed()
        }
        return result
    }
    public boolean visasFkKompletteringar(String internid){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }

            result = page.fkKompletteringar(internid).isDisplayed()
        }
        return result
    }

    public boolean fragaMedIdHarText(String internid, String text){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.qaFragetext(internid).text().contains(text)
        }
        return result
    }

    public boolean fragaMedIdHarSvarstext(String internid, String text){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            result = page.qaSvarstext(internid).getAttribute("value").contains(text)
        }
        return result
    }

    public boolean intygFält1Visas(String visatVarde){
        def result = false
        Browser.drive {
            waitFor {
                at ViewCertQAPage
            }
            if(visatVarde.equalsIgnoreCase("yes")){
                waitFor {
                    page.field1yes.isDisplayed()
                }
                result = page.field1yes.text().contains(visatVarde)
            } else{
                waitFor {
                    page.field1no.isDisplayed()
                }
                result = page.field1no.text().contains(visatVarde)
            }

        }
        return result
    }
    public boolean intygFält2Visas(String visatVarde){
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
    public boolean intygFält3Visas(String visatVarde){
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
    public boolean intygFält4Visas(String visatVarde){
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
    public boolean intygFält4bVisas(String visatVarde){
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
    public boolean intygFält5Visas(String visatVarde){
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
    public boolean intygFält6aVisas(String visatVarde){
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
    public boolean intygFält6bVisas(String visatVarde){
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
    public boolean intygFält7Visas(String visatVarde){
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
    public boolean intygFält8aVisas(String visatVarde){
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
    public boolean intygFält8bVisas(String visatVarde){
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
    public boolean intygFält9Visas(String visatVarde){
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
    public boolean intygFält10Visas(String visatVarde){
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
    public boolean intygFält11Visas(String visatVarde){
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
    public boolean intygFält12Visas(String visatVarde){
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
    public boolean intygFält13Visas(String visatVarde){
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

    public boolean intygFält17Visas(String visatVarde){
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
    public boolean intygFältVarpersonNamnVisas(String visatVarde){
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
    public boolean intygFältVarpersonEnhetsnamnVisas(String visatVarde){
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
