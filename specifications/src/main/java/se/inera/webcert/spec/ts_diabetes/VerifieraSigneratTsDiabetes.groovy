package se.inera.webcert.spec.ts_diabetes

import org.codehaus.groovy.runtime.StackTraceUtils
import se.inera.certificate.spec.Browser

class VerifieraSigneratTsDiabetes {

    public VerifieraSigneratTsDiabetes() {

    }

    String getCurrentMethodName(){
        def marker = new Throwable()
        return StackTraceUtils.sanitize(marker).stackTrace[1].methodName
    }

    boolean getBooleanResult(field) {
        def result = false
        Browser.drive {
            result = page."$field".isDisplayed()
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

    String observationsperiod() {
        getStringResult(getCurrentMethodName())
    }

    String diabetestyp() {
        getStringResult(getCurrentMethodName())
    }

    String endastKost() {
        getStringResult(getCurrentMethodName())
    }

    String tabletter() {
        getStringResult(getCurrentMethodName())
    }

    String insulin() {
        getStringResult(getCurrentMethodName())
    }

    String insulinBehandlingsperiod() {
        getStringResult(getCurrentMethodName())
    }

    String annanBehandlingBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String kunskapOmAtgarder() {
        getStringResult(getCurrentMethodName())
    }

    String teckenNedsattHjarnfunktion() {
        getStringResult(getCurrentMethodName())
    }

    String saknarFormagaKannaVarningstecken() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomst() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomstBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomstTrafiken() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomstTrafikBeskrivning() {
        getStringResult(getCurrentMethodName())
    }

    String egenkontrollBlodsocker() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomstVakenTid() {
        getStringResult(getCurrentMethodName())
    }

    String allvarligForekomstVakenTidObservationstid() {
        getStringResult(getCurrentMethodName())
    }

    String separatOgonlakarintyg() {
        getStringResult(getCurrentMethodName())
    }

    String synfaltsprovningUtanAnmarkning() {
        getStringResult(getCurrentMethodName())
    }

    String hogerutanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String hogermedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String vansterutanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String vanstermedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String binokulartutanKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String binokulartmedKorrektion() {
        getStringResult(getCurrentMethodName())
    }

    String diplopi() {
        getStringResult(getCurrentMethodName())
    }

    String lamplighetInnehaBehorighet() {
        getStringResult(getCurrentMethodName())
    }

    String kommentar() {
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
