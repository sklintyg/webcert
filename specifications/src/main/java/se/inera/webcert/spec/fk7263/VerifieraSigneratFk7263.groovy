package se.inera.webcert.spec.fk7263

import org.codehaus.groovy.runtime.StackTraceUtils
import se.inera.webcert.spec.Browser

class VerifieraSigneratFk7263 {

    public VerifieraSigneratFk7263() {

    }

    //| smittskydd | undersokning | undersokningDatum | telefonkontakt | telefonkontaktDatum | journal | journalDatum |
    // other | otherDatum | otherText | diagnos1 | diagnos1Text | diagnos2 | diagnos2Text | diagnos3 | diagnos3Text |
    // diagnosFortydligande | diagnosSamsjuklighet | sjukdomsforlopp | funktionsnedsattning | aktivitetsbegransning |
    // nuvarandearbete | arbetsuppgifter | arbetslos | foraldraledig | tjanstgoringstid | nedsattMed25 |
    // nedsattMed25start | nedsattMed25slut | nedsattMed25beskrivning | nedsattMed50 | nedsattMed50start |
    // nedsattMed50slut | nedsattMed50beskrivning | nedsattMed75 | nedsattMed75start | nedsattMed75slut |
    // nedsattMed75beskrivning | nedsattMed100 | nedsattMed100start | nedsattMed100slut | nedsattBeskrivning | prognos |
    // prognosBeskrivning | atgardSjukvard | atgardAnnan | rekommendationRessatt | rekommendationKontaktAf |
    // rekommendationKontaktForetagshalsovard | rekommendationOvrigt | rekommendationOvrigtBeskrivning |
    // arbetslivsinriktadRehabilitering | kontaktFk | ovrigt | vardenhetPostadress | vardenhetPostnummer |
    // vardenhetPostort | vardenhetTelefonnummer |
    def getCurrentMethodName(){
        def marker = new Throwable()
        return StackTraceUtils.sanitize(marker).stackTrace[1].methodName
    }

    boolean getBooleanResult(field) {
        def result
        Browser.drive {
            result = page."$field".isDisplayed()
        }
        result
    }

    String getStringResult(field) {
        def result
        Browser.drive {
            result = page."$field".text()
        }
        result
    }

    boolean smittskydd() {
        getBooleanResult("field1yes")
    }

    String diagnosKod() {
        getStringResult(getCurrentMethodName())
    }

    String diagnosBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String diagnosKod2() {
        getStringResult(getCurrentMethodName())
    }

    String diagnosBeskrivning2() {
        getStringResult(getCurrentMethodName())
    }

    String diagnosKod3() {
        getStringResult(getCurrentMethodName())
    }

    String diagnosBeskrivning3() {
        getStringResult(getCurrentMethodName())
    }

    boolean samsjuklighet() {
        getBooleanResult(getCurrentMethodName())
    }
}
