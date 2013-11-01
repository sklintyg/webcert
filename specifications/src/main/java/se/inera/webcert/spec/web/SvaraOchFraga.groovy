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

}
