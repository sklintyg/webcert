package se.inera.webcert.spec

import se.inera.webcert.spec.util.WebcertRestUtils;
import se.inera.webcert.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static se.inera.webcert.spec.util.WebcertRestUtils.*

import org.apache.commons.io.IOUtils

class SkapaUtkastMedFakeInloggning extends RestClientFixture {

    String intygId
    String intygTyp

    String patientPersonnummer
    String patientFornamn = "Test"
    String patientEfternamn = "Testsson"

    // hosperson
    String hsaId
    String namn

    // Enhet
    String enhetId = "SE4815162344-1A02"
    String enhetnamn = "WebCert-Integration Enhet 1"
    String telefonnummer = "123456789"
    String postadress = "Storgatan 12"
    String postnummer = "12345"
    String postort = "Ankeborg"
    String arbetsplatskod = "arbetsplatskod"
    String epost = "enhet1@webcert.se.invalid"

    // vardgivare
    String vardgivarId = "SE4815162344-1A01"
    String vardgivarnamn = "WebCert-Integration Vårdgivare 1"

    boolean komplett
    String kodverk = "ICD_10_SE"

    public setIntygsId (String value) {
        intygsId = value
    }
    public setIntygTyp(String value) {
        intygTyp = value;
    }
    public setPatientPersonnummer(String value) {
        patientPersonnummer = value;
    }
    public setPatientFornamn(String value) {
        patientFornamn = value;
    }
    public setPatientEfternamn(String value) {
        patientEfternamn = value;
    }
    public setHhsaId(String value) {
        hsaId = value;
    }
    public setNamn(String value) {
        namn = value;
    }
    public setEnhetId(String value) {
        enhetId = value;
    }
    public setKomplett(boolean value ) {
        komplett = value
    }

    def response

    public void execute() {
        WebcertRestUtils.login("user1")
        response = WebcertRestUtils.createNewUtkast(json())
    }

    boolean utkastCreated() {
        return response.success
    }

    private json() {
        '{ "intygType" : "fk7263", "patientPersonnummer" : "19121212-1212", "patientFornamn" : "Test", "patientEfternamn" : "Test", "patientPostadress" : "adres", "patientPostnummer" : "12345", "patientPostort" : "ort" }'
    }

    // Fill utkast and set it up as 'complete'
    public boolean makeComplete() {
        def json
        if ("fk7263" == intygTyp) {
            String diagnos = """"diagnosKodsystem1":"ICD_10_SE","diagnosKod":"S50","diagnosBeskrivning1" : "Kontusion på armbåge", "diagnosKodsystem2":"ICD_10_SE","diagnosKod2" : "Z233",  "diagnosBeskrivning2" : "Vaccination avseende pest", "diagnosKodsystem3":"ICD_10_SE","diagnosKod3" : "Z321", "diagnosBeskrivning3" : "Graviditet bekräftad"""
            if (kodverk == "KSH_97_P") {
                diagnos = """"diagnosKodsystem1":"$kodverk","diagnosKod":"S50","diagnosBeskrivning1" : "Kontusion på armbåge", "diagnosKodsystem2":"$kodverk", "diagnosKod2" : "Z233",  "diagnosBeskrivning2" : "Vaccination avseende pest", "diagnosKodsystem3":"$kodverk", "diagnosKod3" : "Z321", "diagnosBeskrivning3" : "Graviditet bekräftad"""
            }
            json = """{"id":"$id","grundData":{"skapadAv":{"personId":"$hsaId","forskrivarKod":"1234567890","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","telefonnummer":"$telefonnummer","epost":"$epost","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"}}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"Adressen","postnummer":"22222","postort":"Hemma"}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":false,"kontaktMedFk":false,"namnfortydligandeOchAdress":"$patientFornamn $patientEfternamn\\n$enhetnamn","undersokningAvPatienten":"2014-05-06", "annanReferens": "2014-05-07", "annanReferensBeskrivning" : "Mailkontakt med patienten", "diagnosBeskrivning":"Skada underarm", $diagnos", "sjukdomsforlopp":"Trillade och skrapade armen mot trottoaren","funktionsnedsattning":"Kan inte lyfta armen","nuvarandeArbete":true,"nuvarandeArbetsuppgifter":"Armlyftare","nedsattMed100":{"from":"2014-05-06","tom":"2014-07-31"},"prognosBedomning" : "arbetsformagaPrognosGarInteAttBedoma", "arbetsformagaPrognosGarInteAttBedomaBeskrivning" : "Oklart varför", "arbetsformagaPrognos" : "Skadan har förvärrats vid varje tillfälle patienten använt armen. Måste hållas i total stillhet tills läkningsprocessen kommit en bit på väg. Eventuellt kan utredning visa att operation är nödvändig för att läka skadan.", "aktivitetsbegransning" : "aktivitetsbegransning"}"""
        }

        def respUpdate = WebcertRestUtils.updateUtkast(intygId, json)
        def respMakeComplete = WebcertRestUtils.setUtkastKomplett(intygId)
        return respUpdate.success && respMakeComplete.success
    }

    // Return a string representation of the response payload, more specifically: utkastId
    String utkastId() {
        IOUtils.toString(response.data)
    }
}
