package se.inera.webcert.spec

import org.openqa.selenium.Keys

import se.inera.certificate.spec.Browser
import se.inera.webcert.pages.SokSkrivValjIntygTypPage
import se.inera.webcert.pages.SokSkrivaIntygPage

class SkrivIntyg {

    def intygsid

    def skapaNyttIntygsutkastForPatientAvTyp(String patient, String typ) {
        Browser.drive {

            waitFor {
                at SokSkrivaIntygPage
            }

            page.angePatient(patient)

            waitFor {
                at SokSkrivValjIntygTypPage
            }

            page.valjIntygsTyp(typ)
            
            waitFor {
                if (typ == "FK7263") {
                    at se.inera.webcert.pages.fk7263.EditCertPage
                } else if (typ == "ts-bas") {
                    at se.inera.webcert.pages.ts_bas.EditCertPage
                } else if (typ == "ts-diabetes") {
                    at se.inera.webcert.pages.ts_diabetes.EditCertPage
                }
            }
    
            intygsid = currentUrl.substring(currentUrl.lastIndexOf("/") + 1)
        }
    }

    String intygsid() {
        intygsid
    }

    void visaVadSomSaknas(){
        Browser.drive {
            page.visaVadSomSaknas()
        }
    }

    void doljVadSomSaknas(){
        Browser.drive {
            page.doljVadSomSaknas()
        }
    }

    boolean visaVadSomSaknasListaVisas(boolean expected = false) {
        boolean result
        Browser.drive {
            result = page.visaVadSomSaknasLista.isDisplayed() == expected
        }
        result
    }

    boolean ingaValideringsfelVisas() {
        def result
        Browser.drive {
            result = !page.valideringIntygBaseratPa.isDisplayed() &&
                !page.valideringDiagnos.isDisplayed() &&
                !page.valideringFunktionsnedsattning.isDisplayed() &&
                !page.valideringAktivitetsbegransning.isDisplayed() &&
                !page.valideringSysselsattning.isDisplayed() &&
                !page.valideringArbetsformaga.isDisplayed() &&
                !page.valideringPrognos.isDisplayed() &&
                !page.valideringRekommendationer.isDisplayed() &&
                !page.valideringVardperson.isDisplayed()
        }
        result
    }

    boolean valideringsfelIntygBaseratPaVisas() {
        def result
        Browser.drive{
            result = page.valideringIntygBaseratPa.isDisplayed()
        }
        result
    }
    boolean valideringsfelDiagnosVisas() {
        def result
        Browser.drive{
            result = page.valideringDiagnos.isDisplayed()
        }
        result
    }
    boolean valideringsfelFunktionsnedsattningVisas() {
        def result
        Browser.drive{
            result = page.valideringFunktionsnedsattning.isDisplayed()
        }
        result
    }
    boolean valideringsfelAktivitetsbegransningVisas() {
        def result
        Browser.drive{
            result = page.valideringAktivitetsbegransning.isDisplayed()
        }
        result
    }
    boolean valideringsfelSysselsattningVisas() {
        def result
        Browser.drive{
            result = page.valideringSysselsattning.isDisplayed()
        }
        result
    }
    boolean valideringsfelArbetsformagaVisas() {
        def result
        Browser.drive{
            result = page.valideringArbetsformaga.isDisplayed()
        }
        result
    }
    boolean valideringsfelPrognosVisas() {
        def result
        Browser.drive{
            result = page.valideringPrognos.isDisplayed()
        }
        result
    }
    boolean valideringsfelRekommendationerVisas() {
        def result
        Browser.drive{
            result = page.valideringRekommendationer.isDisplayed()
        }
        result
    }
    boolean valideringsfelVardpersonVisas() {
        def result
        Browser.drive{
            result = page.valideringVardperson.isDisplayed()
        }
        result
    }

    void sparaUtkast() {
        // ocassionally the spara button has gone back to disabled becuase the auto save has already happened.
        // we need to check if intygSparatVisas, if it has then we should not try clicking on
        // the spara button.
        Browser.drive {
            if(page.intygetSparatMeddelande != null && page.intygetSparatMeddelande.isDisplayed()){
                println('auto save has happened!')
            } else {
                page.spara()
                println('before auto save so click on the save button')
            }
        }
    }

    def vanta(int sekunder) {
        Browser.drive {
            Thread.sleep(sekunder * 1000)
        }
        true
    }

    boolean intygSparatVisas() {
        Browser.drive {
            waitFor {
                page.intygetSparatMeddelande.isDisplayed()
            }
        }
        true
    }

    boolean intygEjKomplettVisas() {
        Browser.drive {
            assert page.intygetEjKomplettMeddelande.isDisplayed()
        }
        true
    }

    int sjukskrivningsperiod() {
        int result
        Browser.drive {
            result = page.arbetsformaga.period.text().toInteger()
        }
        result
    }

    String arbetstid25() {
        String result
        Browser.drive {
            result = page.arbetsformaga.arbetstid25.text()
        }
        result
    }

    String arbetstid50() {
        String result
        Browser.drive {
            result = page.arbetsformaga.arbetstid50.text()
        }
        result
    }

    String arbetstid75() {
        String result
        Browser.drive {
            result = page.arbetsformaga.arbetstid75.text()
        }
        result
    }

    String diagnos1Kod() {
        def result
        Browser.drive {
            result = page.diagnos.diagnos1.value()
        }
        result
    }

    String diagnos2Kod() {
        def result
        Browser.drive {
            result = page.diagnos.diagnos2.value()
        }
        result
    }

    String diagnos3Kod() {
        def result
        Browser.drive {
            result = page.diagnos.diagnos3.value()
        }
        result
    }

    String diagnos1Text() {
        def result
        Browser.drive {
            result = page.diagnos.diagnos1Text.value()
        }
        result
    }

    String diagnos2Text() {
        def result
        Browser.drive {
            result = page.diagnos.diagnos2Text.value()
        }
        result
    }

    String diagnos3Text() {
        def result
        Browser.drive {
            result = page.diagnos.diagnos3Text.value()
        }
        result
    }

    def enterPaDiagnosKod(){
        Browser.drive {
            page.diagnos.diagnos1 << Keys.ENTER
        }
    }

    def oppnaDatePicker(){
        Browser.drive {
            baserasPa.undersokningDatumToggle.click();
            waitFor {
                page.doneLoading()
            }
        }
    }

    boolean datePickerVisas() {
        def result
        Browser.drive {
            waitFor {
                page.doneLoading()
            }
            result = page.datepicker.isDisplayed()
        }
        result
    }

    String prognos() {
        def result = '';
        Browser.drive {
            result = page.prognos.prognos.value();
        }
        result
    }

    boolean prognosArInteVald() {
        def result;
        Browser.drive {
            result = page.prognos.prognos.value() == null;
        }
        result
    }

    String ressatt() {
        def result = ''
        Browser.drive {
            result = page.rekommendationer.radioGroupResor
        }
        result
    }

    String rehab() {
        def result = ''
        Browser.drive {
            result = page.rekommendationer.radioGroupRehab
        }
        result
    }

    String ressattNej() {
        def result = ''
        Browser.drive {
            result = page.rekommendationer.ressattNej.value();
        }
        result
    }

    String ressattJa() {
        def result = ''
        Browser.drive {
            result = page.rekommendationer.ressattJa.value();
        }
        result
    }

    String rehabJa() {
        def result = ''
        Browser.drive {
            result = page.rekommendationer.rehabYes.value();
        }
        result
    }

    String rehabNej() {
        def result = ''
        Browser.drive {
            result = page.rekommendationer.rehabNo.value();
        }
        result
    }

    boolean rehabNejVisas() {
        def result
        Browser.drive {
            result = page.rekommendationer.rehabNo.isDisplayed();
        }
        result
    }

    def klickaPaTillbakaKnappen() {
        Browser.drive {
            page.tillbaka()
        }
    }

    boolean textForIdInnehallar(String elementId, String expectedText) {
        def result;
        Browser.drive {
            def element = page.elementForId(elementId);
            result = containText(element, expectedText);
        }
        result
    }

    boolean containText(element, expectedText){
        println(element);
        if(element != null && element instanceof ArrayList){
            element = element.get(0);
        }
        def text = '';
        if(element){
            if(element.value()){
                text = element.value();
            } else if(element.text()){
                text = element.text();
            }
        }
        return text.contains(expectedText);
    }

    boolean textForClassInnehallar(String classId, String expectedText) {
        def result;
        Browser.drive {
            def element = page.elementForClass(classId);
            result = containText(element, expectedText);
        }
        result
    }

    /**
     * Returns true if the specified text exists in any of the markup within the specified element id. E.g,
     * note that this looks at all HTML, not just text within the element.
     */
    boolean markupForIdInnehaller(String elementId, String text) {
        def result
        Browser.drive {
            def element = $('#' + elementId)
            result = element.contains(text)
        }
        result
    }

    void klickaPaSmittskyd(boolean val) {
        Browser.drive {
            page.setSmittskydd(val);
        }
    }

    boolean diagnosArSynligt(){
        def result
        Browser.drive {
            result = page.diagnos.isDisplayed()
        }
        result
    }

    /**
     * Clicks the anchor identified as having the supplied string as message key.
     *
     * @param messageKeyForLink
     * @return
     */
    def klickaPaFellank(String messageKeyForLink) {
        Browser.drive {
            def errorLink = $('a').find('span[key="' + messageKeyForLink + '"]');
            errorLink.click()
        }
    }

    /**
     * Checks the full markup of the <body> for the text string "[Missing " and returns
     * false if it is found.
     */
    boolean ingaOversattningsnycklarSaknas() {
        def result
        Browser.drive {
            result = $('body').text().contains("[Missing ")
        }
        !result
    }
}