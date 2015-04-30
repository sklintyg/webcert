package se.inera.webcert.spec.ts_bas

import se.inera.webcert.pages.ts_bas.VisaTsBasPage
import org.codehaus.groovy.runtime.StackTraceUtils
import se.inera.certificate.spec.Browser

class VerifieraSigneratTsBas {

    public VerifieraSigneratTsBas() {

    }

    String getCurrentMethodName(){
        def marker = new Throwable()
        return StackTraceUtils.sanitize(marker).stackTrace[1].methodName
    }

    boolean getBooleanResult(field) {
        boolean result
        Browser.drive {
            result = page."$field".isDisplayed()
        }
        result
    }

    String getStringResult(field) {
        String result = ''
        Browser.drive {
            if (!page."$field".isDisplayed()) {
                result = "notshown"
            } else {
                result = page."$field".text()
            }
        }
        result
    }

    String patientpostadress() {
        getStringResult(getCurrentMethodName())
    }

    String patientpostnummer() {
        getStringResult(getCurrentMethodName())
    }

    String patientpostort() {
        getStringResult(getCurrentMethodName())
    }

    String intygAvser() {
        getStringResult(getCurrentMethodName())
    }

    String identitet() {
        getStringResult(getCurrentMethodName())
    }

    String synfaltsdefekter() {
        getStringResult(getCurrentMethodName())
    }

    String nattblindhet() {
        getStringResult(getCurrentMethodName())
    }

    String diplopi() {
        getStringResult(getCurrentMethodName())
    }

    String nystagmus() {
        getStringResult(getCurrentMethodName())
    }

    String hogerOgautanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String hogerOgamedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String hogerOgakontaktlins() {
        getStringResult(getCurrentMethodName())
    }

    String vansterOgautanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String vansterOgamedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String vansterOgakontaktlins() {
        getStringResult(getCurrentMethodName())
    }

    String binokulartutanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String binokulartmedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String korrektionsglasensStyrka() {
        getStringResult(getCurrentMethodName())
    }

    String horselBalansbalansrubbningar() {
        getStringResult(getCurrentMethodName())
    }

    String horselBalanssvartUppfattaSamtal4Meter() {
        getStringResult(getCurrentMethodName())
    }

    String funktionsnedsattning() {
        getStringResult(getCurrentMethodName())
    }

    String funktionsnedsattningbeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String funktionsnedsattningotillrackligRorelseformaga() {
        getStringResult(getCurrentMethodName())
    }

    String hjartKarlSjukdom() {
        getStringResult(getCurrentMethodName())
    }

    String hjarnskadaEfterTrauma() {
        getStringResult(getCurrentMethodName())
    }

    String riskfaktorerStroke() {
        getStringResult(getCurrentMethodName())
    }

    String beskrivningRiskfaktorer() {
        getStringResult(getCurrentMethodName())
    }

    String harDiabetes() {
        getStringResult(getCurrentMethodName())
    }

    String diabetesTyp() {
        getStringResult(getCurrentMethodName())
    }

    String kost() {
        getStringResult(getCurrentMethodName())
    }

    String tabletter() {
        getStringResult(getCurrentMethodName())
    }

    String insulin() {
        getStringResult(getCurrentMethodName())
    }

    String neurologiskSjukdom() {
        getStringResult(getCurrentMethodName())
    }

    String medvetandestorning() {
        getStringResult(getCurrentMethodName())
    }

    String medvetandestorningbeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String nedsattNjurfunktion() {
        getStringResult(getCurrentMethodName())
    }

    String sviktandeKognitivFunktion() {
        getStringResult(getCurrentMethodName())
    }

    String teckenSomnstorningar() {
        getStringResult(getCurrentMethodName())
    }

    String teckenMissbruk() {
        getStringResult(getCurrentMethodName())
    }

    String foremalForVardinsats() {
        getStringResult(getCurrentMethodName())
    }

    String provtagningBehovs() {
        getStringResult(getCurrentMethodName())
    }

    String lakarordineratLakemedelsbruk() {
        getStringResult(getCurrentMethodName())
    }

    String lakemedelOchDos() {
        getStringResult(getCurrentMethodName())
    }

    String psykiskSjukdom() {
        getStringResult(getCurrentMethodName())
    }

    String psykiskUtvecklingsstorning() {
        getStringResult(getCurrentMethodName())
    }

    String harSyndrom() {
        getStringResult(getCurrentMethodName())
    }

    String stadigvarandeMedicinering() {
        getStringResult(getCurrentMethodName())
    }

    String medicineringbeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String kommentar() {
        getStringResult(getCurrentMethodName())
    }

    String kommentarEjAngivet() {
        getStringResult(getCurrentMethodName())
    }

    String bedomning() {
        getStringResult(getCurrentMethodName())
    }

    String bedomningKanInteTaStallning() {
        getStringResult(getCurrentMethodName())
    }

    String lakareSpecialKompetens() {
        getStringResult(getCurrentMethodName())
    }

    String lakareSpecialKompetensEjAngivet() {
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

    String vardenhet_postadress() {
        getStringResult(getCurrentMethodName())
    }

    String vardenhet_postnummer() {
        getStringResult(getCurrentMethodName())
    }

    String vardenhet_postort() {
        getStringResult(getCurrentMethodName())
    }

    String vardenhet_telefonnummer() {
        getStringResult(getCurrentMethodName())
    }
}
