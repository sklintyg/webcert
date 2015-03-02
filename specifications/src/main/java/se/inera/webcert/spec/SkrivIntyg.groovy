package se.inera.webcert.spec

import org.openqa.selenium.Keys
import se.inera.webcert.pages.SokSkrivValjIntygTypPage
import se.inera.webcert.pages.SokSkrivaIntygPage

class SkrivIntyg {

    def intygsid

    def skapaNyttIntygsutkastForPatientAvTyp(String patient, String typ) {
        Browser.drive {

            waitFor {
                at SokSkrivaIntygPage
            }

            page.personnummer = patient
            page.personnummerFortsattKnapp.click()

            waitFor {
                at SokSkrivValjIntygTypPage
            }

            if (typ == "FK7263") {
                page.valjIntygstypFk7263();
            } else if (typ == "ts-bas") {
                page.valjIntygstypTsBas();
            } else if (typ == "ts-diabetes") {
                page.valjIntygstypTsDiabetes();
            }

            page.fortsattKnapp.click();

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

    def visaVadSomSaknas(){
        Browser.drive {
            page.visaVadSomSaknasKnapp.click();
        }
    }

    def ingaValideringsfelVisas() {
        def result = false
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

    def valideringsfelIntygBaseratPaVisas() {
        def result = false
        Browser.drive{
            result = page.valideringIntygBaseratPa.isDisplayed()
        }
        result
    }
    def valideringsfelDiagnosVisas() {
        def result = false
        Browser.drive{
            result = page.valideringDiagnos.isDisplayed()
        }
        result
    }
    def valideringsfelFunktionsnedsattningVisas() {
        def result = false
        Browser.drive{
            result = page.valideringFunktionsnedsattning.isDisplayed()
        }
        result
    }
    def valideringsfelAktivitetsbegransningVisas() {
        def result = false
        Browser.drive{
            result = page.valideringAktivitetsbegransning.isDisplayed()
        }
        result
    }
    def valideringsfelSysselsattningVisas() {
        def result = false
        Browser.drive{
            result = page.valideringSysselsattning.isDisplayed()
        }
        result
    }
    def valideringsfelArbetsformagaVisas() {
        def result = false
        Browser.drive{
            result = page.valideringArbetsformaga.isDisplayed()
        }
        result
    }
    def valideringsfelPrognosVisas() {
        def result = false
        Browser.drive{
            result = page.valideringPrognos.isDisplayed()
        }
        result
    }
    def valideringsfelRekommendationerVisas() {
        def result = false
        Browser.drive{
            result = page.valideringRekommendationer.isDisplayed()
        }
        result
    }
    def valideringsfelVardpersonVisas() {
        def result = false
        Browser.drive{
            result = page.valideringVardperson.isDisplayed()
        }
        result
    }

    def sparaUtkast() {
        Browser.drive {
            page.sparaKnapp.click()
        }
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
            waitFor(6) {
                page.intygetEjKomplettMeddelande.isDisplayed()
            }
        }
        true
    }

    boolean verifieraAttSjukskrivningsperiodenAr(int expected) {
        Browser.drive {
            waitFor {
                sleep(500)
                expected == page.arbetsformaga.period.text().toInteger()
            }
        }
        true
    }

    boolean verifieraAttArbetstid25Ar(String expected) {
        Browser.drive {
            waitFor {
                expected == page.arbetsformaga.arbetstid25.text()
            }
        }
        true
    }

    boolean verifieraAttArbetstid50Ar(String expected) {
        Browser.drive {
            waitFor {
                expected == page.arbetsformaga.arbetstid50.text()
            }
        }
        true
    }

    boolean verifieraAttArbetstid75Ar(String expected) {
        Browser.drive {
            waitFor {
                expected == page.arbetsformaga.arbetstid75.text()
            }
        }
        true
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
        }
    }

    boolean datePickerVisas() {
        def result = false
        Browser.drive {
            waitFor(1) {
                result = page.datepicker.isDisplayed()
            }
        }
        result
    }

    boolean datePickerInteVisas() {
        def result
        Browser.drive {
            result = !page.datepicker.isDisplayed()
        }
        result
    }

}