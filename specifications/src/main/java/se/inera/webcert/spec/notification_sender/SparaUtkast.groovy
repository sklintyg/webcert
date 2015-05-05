package se.inera.webcert.spec.notification_sender

import se.inera.webcert.spec.util.WebcertRestUtils;
import se.inera.webcert.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static se.inera.webcert.spec.util.WebcertRestUtils.*

import org.apache.commons.io.IOUtils

class SparaUtkast {

    String intygId
    String intygTyp

    String patientPersonnummer
    String patientFornamn = "Test"
    String patientEfternamn = "Testsson"

    // hosperson
    String hsaId
    String namn = "enLäkare"

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

    // Diagnos
    String diagnosKod = "S50"
    String diagnosBeskrivning1 = "Kontusion på armbåge"
    String diagnosBeskrivning = "Skada underarm"
    
    String nedsattMed100 = null

    boolean komplett = false
    boolean basic = false
    boolean useSameLogin = false;
    int version = 1;
    String kodverk = "ICD_10_SE"

    def response
    def json

    public void execute() {

        if(basic){
            makeBasic()
        } else {
            WebcertRestUtils.login()
            if (komplett) {
                makeComplete()
            } else {
                makeIncomplete()
            }
        }
        println("usesame login : " + useSameLogin);
        response = WebcertRestUtils.saveUtkast(intygTyp, intygId, version, json, useSameLogin)
    }

    public boolean utkastSparat() {
        response.success
    }

    // Fill utkast and set it up as 'complete'
    def makeComplete() {
        if ("fk7263" == intygTyp) {
            String diagnos = """"diagnosKodsystem1":"$kodverk","diagnosKod":"$diagnosKod","diagnosBeskrivning1":"$diagnosBeskrivning1","diagnosBeskrivning":"$diagnosBeskrivning"""
            String nedsatt = nedsattMed100 != null ? "${nedsattMed100}," : ""
            json = """{"id":"$intygId","grundData":{"skapadAv":{"personId":"$hsaId","forskrivarKod":"1234567890","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","telefonnummer":"$telefonnummer","epost":"$epost","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"}}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"Adressen","postnummer":"22222","postort":"Hemma"}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":false,"kontaktMedFk":false,"namnfortydligandeOchAdress":"$patientFornamn $patientEfternamn\\n$enhetnamn","undersokningAvPatienten":"2014-05-06", "annanReferens": "2014-05-07", "annanReferensBeskrivning" : "Mailkontakt med patienten", $diagnos", "sjukdomsforlopp":"Trillade och skrapade armen mot trottoaren","funktionsnedsattning":"Kan inte lyfta armen","nuvarandeArbete":true,"nuvarandeArbetsuppgifter":"Armlyftare",${nedsatt}"prognosBedomning" : "arbetsformagaPrognosGarInteAttBedoma", "arbetsformagaPrognosGarInteAttBedomaBeskrivning" : "Oklart varför", "arbetsformagaPrognos" : "Skadan har förvärrats vid varje tillfälle patienten använt armen. Måste hållas i total stillhet tills läkningsprocessen kommit en bit på väg. Eventuellt kan utredning visa att operation är nödvändig för att läka skadan.", "aktivitetsbegransning" : "aktivitetsbegransning"}"""
        }
    }

    def makeIncomplete() {
        if ("fk7263" == intygTyp) {
            json = """{"id":"$intygId","grundData":{"skapadAv":{"personId":"$hsaId","forskrivarKod":"1234567890","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","telefonnummer":"$telefonnummer","epost":"$epost","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"}}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"Adressen","postnummer":"22222","postort":"Hemma"}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":false,"kontaktMedFk":false,"namnfortydligandeOchAdress":"$patientFornamn $patientEfternamn\\n$enhetnamn","undersokningAvPatienten":"2014-05-06", "annanReferens": "2014-05-07", "annanReferensBeskrivning" : "Mailkontakt med patienten", "sjukdomsforlopp":"Trillade och skrapade armen mot trottoaren","funktionsnedsattning":"Kan inte lyfta armen","nuvarandeArbete":true,"nuvarandeArbetsuppgifter":"Armlyftare","prognosBedomning" : "arbetsformagaPrognosGarInteAttBedoma", "arbetsformagaPrognosGarInteAttBedomaBeskrivning" : "Oklart varför", "arbetsformagaPrognos" : "Skadan har förvärrats vid varje tillfälle patienten använt armen. Måste hållas i total stillhet tills läkningsprocessen kommit en bit på väg. Eventuellt kan utredning visa att operation är nödvändig för att läka skadan.", "aktivitetsbegransning" : "aktivitetsbegransning"}"""
        }
    }
    
    def makeBasic(){
        if("fk7263" == intygTyp){
            json = """{"id":"$intygId","grundData":{"skapadAv":{"personId":"$hsaId","fullstandigtNamn":"$namn","forskrivarKod":"2481632","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","telefonnummer":"$telefonnummer","epost":"enhet1@webcert.invalid.se","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"},"arbetsplatsKod":"0000000"}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"Svensson, Storgatan 1, PL 1234","postnummer":"12345","postort":"Smamala","samordningsNummer":false}},"avstangningSmittskydd":true,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"rekommendationOvrigtCheck":false,"nuvarandeArbete":false,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":true,"kontaktMedFk":false,"forskrivarkodOchArbetsplatskod":"2481632 - 0000000","namnfortydligandeOchAdress":"$patientFornamn $patientEfternamn\\\\n$enhetnamn","samsjuklighet":false,"nedsattMed25Beskrivning":null,"nedsattMed50Beskrivning":null,"nedsattMed75Beskrivning":null,"nedsattMed100Beskrivning":null,"arbetsformagaPrognosGarInteAttBedomaBeskrivning":null,"nedsattMed25":{"from":"2015-05-05","tom":"2015-05-11"}}"""
        }

        println("basic json : " + json);
    }
}
