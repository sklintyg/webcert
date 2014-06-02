package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

class SkapaUtkast extends RestClientFixture {

    String intygId
    String intygTyp

    String personNummer
    String forNamn = "Test"
    String efterNamn = "Testsson"

    // hosperson
    String hsaId
    String namn

    // Enhet
    String enhetId = "IFV1239877878-103F"
    String enhetnamn = "VårdEnhet1A"
    String telefonnummer = "123456789"
    String postadress = "Storgatan 12"
    String postnummer = "12345"
    String postort = "Ankeborg"
    String arbetsplatskod = "arbetsplatskod"
    String epost = "enhet1@webcert.se.invalid"

    // vardgivare
    String vardgivarId = "IFV1239877878-103F"
    String vardgivarnamn = "VårdEnhet1A"

    String komplett

    def execute() {
        def restClient = createRestClient(baseUrl)
        restClient.post(path: "intyg", body: json(), requestContentType: JSON)

        def json
        if ("ts-bas" == intygTyp) {
            json = """{"id":"$intygId","skapadAv":{"personid":"$hsaId","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"},"postadress":"Gatan 1","postnummer":"12345","postort":"Storstaden","telefonnummer":"012-123456"}},"patient":{"personid":"$personNummer","fullstandigtNamn":"$forNamn $efterNamn","fornamn":"$forNamn","efternamn":"$efterNamn"},"vardkontakt":{"idkontroll":"KORKORT"},"intygAvser":{"korkortstyp":[{"type":"C1","selected":true},{"type":"C1E","selected":true},{"type":"C","selected":true},{"type":"CE","selected":true},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false},{"type":"ANNAT","selected":false}]},"syn":{"synfaltsdefekter":false,"nattblindhet":false,"progressivOgonsjukdom":false,"diplopi":false,"hogerOga":{"utanKorrektion":2,"medKorrektion":2},"vansterOga":{"utanKorrektion":2,"medKorrektion":2},"binokulart":{"utanKorrektion":2,"medKorrektion":2},"nystagmus":false},"horselBalans":{"balansrubbningar":false},"funktionsnedsattning":{"funktionsnedsattning":false},"hjartKarl":{"hjartKarlSjukdom":false,"hjarnskadaEfterTrauma":false,"riskfaktorerStroke":false},"diabetes":{"harDiabetes":false},"neurologi":{"neurologiskSjukdom":false},"medvetandestorning":{"medvetandestorning":false},"njurar":{"nedsattNjurfunktion":false},"kognitivt":{"sviktandeKognitivFunktion":false},"somnVakenhet":{"teckenSomnstorningar":false},"narkotikaLakemedel":{"teckenMissbruk":false,"foremalForVardinsats":false,"lakarordineratLakemedelsbruk":false},"psykiskt":{"psykiskSjukdom":false},"utvecklingsstorning":{"psykiskUtvecklingsstorning":false,"harSyndrom":false},"sjukhusvard":{"sjukhusEllerLakarkontakt":false},"medicinering":{"stadigvarandeMedicinering":false},"bedomning":{"korkortstyp":[{"type":"C1","selected":true},{"type":"C1E","selected":true},{"type":"C","selected":false},{"type":"CE","selected":true},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false},{"type":"ANNAT","selected":false}]}}"""
        } else if ("ts-diabetes" == intygTyp) {
            json = """{"id":"$intygId","skapadAv":{"personid":"$hsaId","fullstandigtNamn":"$namn","vardenhet":{"enhetsid":"$enhetId","enhetsnamn":"$enhetnamn","vardgivare":{"vardgivarid":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"},"postadress":"Gatan 1","postnummer":"12345","postort":"Staden","telefonnummer":"013-123456"}},"patient":{"personid":"$personNummer","fullstandigtNamn":"$forNamn $efterNamn","fornamn":"$forNamn","efternamn":"$efterNamn"},"intygAvser":{"korkortstyp":[{"type":"AM","selected":true},{"type":"A1","selected":true},{"type":"A2","selected":true},{"type":"A","selected":true},{"type":"B","selected":false},{"type":"BE","selected":false},{"type":"TRAKTOR","selected":false},{"type":"C1","selected":false},{"type":"C1E","selected":false},{"type":"C","selected":false},{"type":"CE","selected":false},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false}]},"diabetes":{"diabetestyp":"DIABETES_TYP_1","endastKost":true,"observationsperiod":"2014"},"hypoglykemier":{"kunskapOmAtgarder":false,"teckenNedsattHjarnfunktion":false},"syn":{"separatOgonlakarintyg":false,"synfaltsprovningUtanAnmarkning":false,"hoger":{"utanKorrektion":2,"medKorrektion":2},"vanster":{"utanKorrektion":2,"medKorrektion":2},"binokulart":{"utanKorrektion":2,"medKorrektion":2},"diplopi":false},"bedomning":{"korkortstyp":[{"type":"AM","selected":true},{"type":"A1","selected":true},{"type":"A2","selected":true},{"type":"A","selected":true},{"type":"B","selected":false},{"type":"BE","selected":false},{"type":"TRAKTOR","selected":false},{"type":"C1","selected":false},{"type":"C1E","selected":false},{"type":"C","selected":false},{"type":"CE","selected":false},{"type":"D1","selected":false},{"type":"D1E","selected":false},{"type":"D","selected":false},{"type":"DE","selected":false},{"type":"TAXI","selected":false}]},"vardkontakt":{"idkontroll":"KORKORT"}}"""
        } else {
            json = """{"id":"$intygId","patientNamn":"$forNamn $efterNamn","patientPersonnummer":"$personNummer","avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"rehabiliteringAktuell":false,"rehabiliteringEjAktuell":false,"rehabiliteringGarInteAttBedoma":false,"arbetsloshet":false,"foraldrarledighet":false,"arbetsformataPrognosJa":false,"arbetsformataPrognosJaDelvis":false,"arbetsformataPrognosNej":false,"arbetsformataPrognosGarInteAttBedoma":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":false,"kontaktMedFk":false,"vardperson":{"hsaId":"$hsaId","namn":"$namn","enhetsId":"$enhetId","enhetsnamn":"$enhetnamn","arbetsplatsKod":"$arbetsplatskod","postadress":"$postadress","postnummer":"$postnummer","postort":"$postort","telefonnummer":"$telefonnummer","epost":"$epost" ,"vardgivarId":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"},"namnfortydligandeOchAdress":"$forNamn $efterNamn\\n$enhetnamn","undersokningAvPatienten":"2014-05-06","diagnosKod":"S50","diagnosBeskrivning":"Skada underarm","sjukdomsforlopp":"Trillade och skrapade armen mot trottoaren","funktionsnedsattning":"Kan inte lyfta armen","basedOnWork":"CURRENT","nuvarandeArbetsuppgifter":"Armlyftare","nedsattMed100":{"from":"2014-05-06","tom":"2014-07-31"},"prognosis":"YES"}"""
        }

        restClient = createRestClient(baseUrl)
        restClient.put(path: "intyg/$intygId", body: json, requestContentType: JSON)

        if ("ja".equalsIgnoreCase(komplett)) {
            restClient = createRestClient(baseUrl)
            restClient.put(path: "intyg/$intygId/komplett")
        }
    }

    private json() {
        [intygId  : intygId, intygType: intygTyp, patient: [personNummer: personNummer, forNamn: forNamn, efterNamn: efterNamn],
         hosPerson: [namn: "Namn", hsaId: "hsaid", forskivarkod: "1234567890"],
         vardenhet: [hsaId: enhetId, namn: enhetnamn, vardgivare: [hsaId: vardgivarId, namn: vardgivarnamn]]]
    }
}
