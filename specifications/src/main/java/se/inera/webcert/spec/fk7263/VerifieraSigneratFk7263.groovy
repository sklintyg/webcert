package se.inera.webcert.spec.fk7263

import org.codehaus.groovy.runtime.StackTraceUtils
import se.inera.certificate.spec.Browser

class VerifieraSigneratFk7263 {

    public VerifieraSigneratFk7263() {
    }

    String getCurrentMethodName(){
        def marker = new Throwable()
        return StackTraceUtils.sanitize(marker).stackTrace[1].methodName
    }

    boolean getBooleanResult(field) {
        def result = false
        Browser.drive {
            result = page."$field".isDisplayed() && page."$field" != "Ej angivet"
        }
        result
    }

    String getStringResult(field) {
        def result = ''
        Browser.drive {
            if (!page."$field".isDisplayed()) {
                result = "notshown"
            } else {
                result = page."$field".text()
            }
        }
        result
    }

    boolean smittskydd() {
        getBooleanResult(getCurrentMethodName())
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

    String sjukdomsforlopp() {
        getStringResult(getCurrentMethodName())
    }

    String funktionsnedsattning() {
        getStringResult(getCurrentMethodName())
    }

    boolean baseratPaList() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean undersokningAvPatienten() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean telefonkontaktMedPatienten() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean journaluppgifter() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean annanReferens() {
        getBooleanResult(getCurrentMethodName())
    }

    String annanReferensBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String aktivitetsbegransning() {
        getStringResult(getCurrentMethodName())
    }

    boolean rekommendationKontaktArbetsformedlingen() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean rekommendationKontaktForetagshalsovarden() {
        getBooleanResult(getCurrentMethodName())
    }

    String rekommendationOvrigt() {
        getStringResult(getCurrentMethodName())
    }

    String atgardInomSjukvarden() {
        getStringResult(getCurrentMethodName())
    }

    String annanAtgard() {
        getStringResult(getCurrentMethodName())
    }

    String rehabilitering() {
        def result = ''
        Browser.drive {
            if (page.rehabiliteringAktuell.isDisplayed()) result = "AKTUELL"
            if (page.rehabiliteringEjAktuell.isDisplayed()) result = "EJAKTUELL"
            if (page.rehabiliteringGarInteAttBedoma.isDisplayed()) result = "GARINTEATTBEDOMA"
        }
        result
    }

    String nuvarandeArbetsuppgifter() {
        getStringResult(getCurrentMethodName())
    }

    boolean arbetsloshet() {
        getBooleanResult(getCurrentMethodName())
    }

    boolean foraldrarledighet() {
        getBooleanResult(getCurrentMethodName())
    }

    String nedsattMed25from() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed25tom() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed25Beskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed50from() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed50tom() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed50Beskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed75from() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed75tom() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed75Beskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed100from() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattMed100tom() {
        getStringResult(getCurrentMethodName())
    }

    String prognosBedomning() {
        getStringResult(getCurrentMethodName())
    }

    String arbetsformagaPrognos() {
        getStringResult(getCurrentMethodName())
    }

    String prognos10() {
        def result = ''
        Browser.drive {
            if (page.arbetsformagaPrognosJa.isDisplayed()) result = "JA"
            if (page.arbetsformagaPrognosJaDelvis.isDisplayed()) result = "JADELVIS"
            if (page.arbetsformagaPrognosNej.isDisplayed()) result = "NEJ"
            if (page.arbetsformagaPrognosGarInteAttBedoma.isDisplayed()) result = "GARINTEATTBEDOMA"
        }
        result
    }

    String arbetsformagaPrognosGarInteAttBedomaBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String ressattTillArbeteAktuellt() {
        def result = null
        Browser.drive {
            if (page.ressattTillArbeteAktuellt.isDisplayed()) result = true
            if (page.ressattTillArbeteEjAktuellt.isDisplayed()) result = false
        }
        result
    }

    boolean kontaktMedFk() {
        getBooleanResult(getCurrentMethodName())
    }

    String kommentar() {
        getStringResult(getCurrentMethodName())
    }

    String forskrivarkodOchArbetsplatskod() {
        getStringResult(getCurrentMethodName())
    }

    String signeringsdatum() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_namn() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_enhetsnamn() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_postadress() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_postnummer() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_postort() {
        getStringResult(getCurrentMethodName())
    }

    String vardperson_telefonnummer() {
        getStringResult(getCurrentMethodName())
    }
}
