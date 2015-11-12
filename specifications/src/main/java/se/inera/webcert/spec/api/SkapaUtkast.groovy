package se.inera.webcert.spec.api

import se.inera.webcert.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.JSON

class SkapaUtkast extends RestClientFixture {

    String intygId
    String intygTyp

    String patientPersonnummer
    String patientFornamn = "Test"
    String patientEfternamn = "Testsson"

    // hosperson
    String hsaId = "SE4815162344-1B01"
    String namn = "enLäkare"

    // Enhet
    String enhetId = "SE4815162344-1A02"
    String enhetnamn = "VårdEnhet1A"
    String telefonnummer = "123456789"
    String postadress = "Storgatan 12"
    String postnummer = "12345"
    String postort = "Ankeborg"
    String arbetsplatskod = "arbetsplatskod"
    String epost = "enhet1@webcert.se.invalid"
    String responseStatus;

    // vardgivare
    String vardgivarId = "SE4815162344-1A01"
    String vardgivarnamn = "VårdEnhet1A"

    private boolean komplett
    String kodverk = "ICD_10_SE"

    public setKomplett(String value) {
        komplett = value.equalsIgnoreCase('ja')
    }
    public String respons() {
        return responseStatus;
    }
    public String intygId() {
        return intygId;
    }

    def restClient = createRestClient("${baseUrl}testability/")

    def generator = { 
        String alphabet = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join(), int n ->
        new Random().with {(1..n).collect { alphabet[ nextInt( alphabet.length() ) ] }.join()}
    }

    def execute() {

        //Assign random id if intygsid = null
        if(!intygId){
            intygId = "fitnesse-" + generator( (('A'..'Z')+('0'..'9')).join(), 9 )
        }

        def resp = restClient.post(path: "intyg", body: json(), requestContentType: JSON)

        def json
        //Ts-bas
        if ("ts-bas" == intygTyp) {
            if (!komplett) {
                json = """{"id":"$intygId","typ":"TS_BAS_U06_V06","grundData":{"skapadAv":{"personId":"$hsaId","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"},"postadress":"Gatan 1","postnummer":"12345","postort":"Storstaden"}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort"}},"vardkontakt":{"idkontroll":"KORKORT"},"intygAvser":{"korkortstyp":[{"type":"C1","selected":true},{"type":"C1E","selected":true},{"type":"C","selected":true},{"type":"CE","selected":true},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false},{"type":"ANNAT","selected":false}]},"syn":{"synfaltsdefekter":false,"nattblindhet":false,"progressivOgonsjukdom":false,"diplopi":false,"hogerOga":{"utanKorrektion":2,"medKorrektion":2},"vansterOga":{"utanKorrektion":2,"medKorrektion":2},"binokulart":{"utanKorrektion":2,"medKorrektion":2},"nystagmus":false},"horselBalans":{"balansrubbningar":false},"funktionsnedsattning":{"funktionsnedsattning":false},"hjartKarl":{"hjartKarlSjukdom":false,"hjarnskadaEfterTrauma":false,"riskfaktorerStroke":false},"diabetes":{"harDiabetes":false},"neurologi":{"neurologiskSjukdom":false},"medvetandestorning":{"medvetandestorning":false},"njurar":{"nedsattNjurfunktion":false},"kognitivt":{"sviktandeKognitivFunktion":false},"somnVakenhet":{"teckenSomnstorningar":false},"narkotikaLakemedel":{"teckenMissbruk":false,"foremalForVardinsats":false,"lakarordineratLakemedelsbruk":false},"psykiskt":{"psykiskSjukdom":false},"utvecklingsstorning":{"psykiskUtvecklingsstorning":false,"harSyndrom":false},"sjukhusvard":{"sjukhusEllerLakarkontakt":false},"medicinering":{"stadigvarandeMedicinering":false},"bedomning":{"korkortstyp":[{"type":"C1","selected":true},{"type":"C1E","selected":true},{"type":"C","selected":false},{"type":"CE","selected":true},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false},{"type":"ANNAT","selected":false}]}}"""
            } else {
                json = """{"id":"$intygId","typ":"TS_BAS_U06_V06","grundData":{"skapadAv":{"personId":"$hsaId","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"},"postadress":"Gatan 1","postnummer":"12345","postort":"Storstaden","telefonnummer":"012-123456"}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort"}},"vardkontakt":{"idkontroll":"KORKORT"},"intygAvser":{"korkortstyp":[{"type":"C1","selected":true},{"type":"C1E","selected":true},{"type":"C","selected":true},{"type":"CE","selected":true},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false},{"type":"ANNAT","selected":false}]},"syn":{"synfaltsdefekter":false,"nattblindhet":false,"progressivOgonsjukdom":false,"diplopi":false,"hogerOga":{"utanKorrektion":2,"medKorrektion":2},"vansterOga":{"utanKorrektion":2,"medKorrektion":2},"binokulart":{"utanKorrektion":2,"medKorrektion":2},"nystagmus":false},"horselBalans":{"balansrubbningar":false},"funktionsnedsattning":{"funktionsnedsattning":false},"hjartKarl":{"hjartKarlSjukdom":false,"hjarnskadaEfterTrauma":false,"riskfaktorerStroke":false},"diabetes":{"harDiabetes":false},"neurologi":{"neurologiskSjukdom":false},"medvetandestorning":{"medvetandestorning":false},"njurar":{"nedsattNjurfunktion":false},"kognitivt":{"sviktandeKognitivFunktion":false},"somnVakenhet":{"teckenSomnstorningar":false},"narkotikaLakemedel":{"teckenMissbruk":false,"foremalForVardinsats":false,"lakarordineratLakemedelsbruk":false},"psykiskt":{"psykiskSjukdom":false},"utvecklingsstorning":{"psykiskUtvecklingsstorning":false,"harSyndrom":false},"sjukhusvard":{"sjukhusEllerLakarkontakt":false},"medicinering":{"stadigvarandeMedicinering":false},"bedomning":{"korkortstyp":[{"type":"C1","selected":true},{"type":"C1E","selected":true},{"type":"C","selected":false},{"type":"CE","selected":true},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false},{"type":"ANNAT","selected":false}]}}"""
            }
        //Ts-diabetes
        } else if ("ts-diabetes" == intygTyp) {
            if (!komplett) {
                json = """{"id":"$intygId","typ":"TS_DIABETES_U06_V02","grundData":{"skapadAv":{"personId":"$hsaId","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"},"postadress":"Gatan 1","postnummer":"12345","postort":"Staden"}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort"}},"intygAvser":{"korkortstyp":[{"type":"AM","selected":true},{"type":"A1","selected":true},{"type":"A2","selected":true},{"type":"A","selected":true},{"type":"B","selected":false},{"type":"BE","selected":false},{"type":"TRAKTOR","selected":false},{"type":"C1","selected":false},{"type":"C1E","selected":false},{"type":"C","selected":false},{"type":"CE","selected":false},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false}]},"diabetes":{"diabetestyp":"DIABETES_TYP_1","endastKost":true,"observationsperiod":"2014"},"hypoglykemier":{"kunskapOmAtgarder":false,"teckenNedsattHjarnfunktion":false},"syn":{"separatOgonlakarintyg":false,"synfaltsprovningUtanAnmarkning":false,"hoger":{"utanKorrektion":2,"medKorrektion":2},"vanster":{"utanKorrektion":2,"medKorrektion":2},"binokulart":{"utanKorrektion":2,"medKorrektion":2},"diplopi":false},"bedomning":{"korkortstyp":[{"type":"AM","selected":true},{"type":"A1","selected":true},{"type":"A2","selected":true},{"type":"A","selected":true},{"type":"B","selected":false},{"type":"BE","selected":false},{"type":"TRAKTOR","selected":false},{"type":"C1","selected":false},{"type":"C1E","selected":false},{"type":"C","selected":false},{"type":"CE","selected":false},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false}]},"vardkontakt":{"idkontroll":"KORKORT"}}"""
            } else {
                json = """{"id":"$intygId","typ":"TS_DIABETES_U06_V02","grundData":{"skapadAv":{"personId":"$hsaId","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"},"postadress":"Gatan 1","postnummer":"12345","postort":"Staden","telefonnummer":"013-123456"}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort"}},"intygAvser":{"korkortstyp":[{"type":"AM","selected":true},{"type":"A1","selected":true},{"type":"A2","selected":true},{"type":"A","selected":true},{"type":"B","selected":false},{"type":"BE","selected":false},{"type":"TRAKTOR","selected":false},{"type":"C1","selected":false},{"type":"C1E","selected":false},{"type":"C","selected":false},{"type":"CE","selected":false},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false}]},"diabetes":{"diabetestyp":"DIABETES_TYP_1","endastKost":true,"observationsperiod":"2014"},"hypoglykemier":{"kunskapOmAtgarder":false,"teckenNedsattHjarnfunktion":false},"syn":{"separatOgonlakarintyg":false,"synfaltsprovningUtanAnmarkning":false,"hoger":{"utanKorrektion":2,"medKorrektion":2},"vanster":{"utanKorrektion":2,"medKorrektion":2},"binokulart":{"utanKorrektion":2,"medKorrektion":2},"diplopi":false},"bedomning":{"korkortstyp":[{"type":"AM","selected":true},{"type":"A1","selected":true},{"type":"A2","selected":true},{"type":"A","selected":true},{"type":"B","selected":false},{"type":"BE","selected":false},{"type":"TRAKTOR","selected":false},{"type":"C1","selected":false},{"type":"C1E","selected":false},{"type":"C","selected":false},{"type":"CE","selected":false},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false}]},"vardkontakt":{"idkontroll":"KORKORT"}}"""
            }
        //FK7263
        } else {
            if (!komplett) {
                //Make incomplete json
                json = """{"id":"$intygId","grundData":{"skapadAv":{"personId":"$hsaId","forskrivarKod":"1234567890","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","telefonnummer":"$telefonnummer","epost":"$epost","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"}}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"Adressen","postnummer":"22222","postort":"Hemma"}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":false,"kontaktMedFk":false,"namnfortydligandeOchAdress":"$patientFornamn $patientEfternamn\\n$enhetnamn","undersokningAvPatienten":"2014-05-06","sjukdomsforlopp":"Trillade och skrapade armen mot trottoaren","funktionsnedsattning":"Kan inte lyfta armen","basedOnWork":"CURRENT","nuvarandeArbete":true,"nuvarandeArbetsuppgifter":"Armlyftare","nedsattMed100":{"from":"2014-05-06","tom":"2014-07-31"},"prognosis":"YES"}"""
            } else {
                String diagnos = """"diagnosKodsystem1":"ICD_10_SE","diagnosKod":"S50","diagnosBeskrivning1" : "Kontusion på armbåge", "diagnosKodsystem2":"ICD_10_SE","diagnosKod2" : "Z233",  "diagnosBeskrivning2" : "Vaccination avseende pest", "diagnosKodsystem3":"ICD_10_SE","diagnosKod3" : "Z321", "diagnosBeskrivning3" : "Graviditet bekräftad"""
                if (kodverk == "KSH_97_P") {
                    diagnos = """"diagnosKodsystem1":"$kodverk","diagnosKod":"S50","diagnosBeskrivning1" : "Kontusion på armbåge", "diagnosKodsystem2":"$kodverk", "diagnosKod2" : "Z233",  "diagnosBeskrivning2" : "Vaccination avseende pest", "diagnosKodsystem3":"$kodverk", "diagnosKod3" : "Z321", "diagnosBeskrivning3" : "Graviditet bekräftad"""
                }
                json = """{"id":"$intygId","grundData":{"skapadAv":{"personId":"$hsaId","forskrivarKod":"1234567890","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","telefonnummer":"$telefonnummer","epost":"$epost","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"}}},"patient":{"personId":"$patientPersonnummer","fullstandigtNamn":"$patientFornamn $patientEfternamn","fornamn":"$patientFornamn","efternamn":"$patientEfternamn","postadress":"Adressen","postnummer":"22222","postort":"Hemma"}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":false,"kontaktMedFk":false,"namnfortydligandeOchAdress":"$patientFornamn $patientEfternamn\\n$enhetnamn","undersokningAvPatienten":"2014-05-06", "annanReferens": "2014-05-07", "annanReferensBeskrivning" : "Mailkontakt med patienten", "diagnosBeskrivning":"Skada underarm", $diagnos", "sjukdomsforlopp":"Trillade och skrapade armen mot trottoaren","funktionsnedsattning":"Kan inte lyfta armen","nuvarandeArbete":true,"nuvarandeArbetsuppgifter":"Armlyftare","nedsattMed100":{"from":"2014-05-06","tom":"2014-07-31"},"prognosBedomning" : "arbetsformagaPrognosGarInteAttBedoma", "arbetsformagaPrognosGarInteAttBedomaBeskrivning" : "Oklart varför", "arbetsformagaPrognos" : "Skadan har förvärrats vid varje tillfälle patienten använt armen. Måste hållas i total stillhet tills läkningsprocessen kommit en bit på väg. Eventuellt kan utredning visa att operation är nödvändig för att läka skadan.", "aktivitetsbegransning" : "aktivitetsbegransning"}"""
            }
        }

        //Break the json

        resp = restClient.put(path: "intyg/$intygId", body: json, requestContentType: JSON)

        if (komplett) {
            
           resp = restClient.put(path: "intyg/$intygId/komplett")
        }

        responseStatus = resp.status

    }

    private json() {
        [intygId  : intygId, intygType: intygTyp, patient: [personnummer: patientPersonnummer, fornamn: patientFornamn, efternamn: patientEfternamn],
            hosPerson: [namn: "Namn", hsaId: "hsaid", forskivarkod: "1234567890"],
            vardenhet: [hsaId: enhetId, namn: enhetnamn, vardgivare: [hsaId: vardgivarId, namn: vardgivarnamn]]]
    }
}
