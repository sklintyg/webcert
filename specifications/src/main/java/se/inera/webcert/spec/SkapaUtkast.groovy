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
    String hsaId;
    String namn;

    // Enhet
    String enhetId = "IFV1239877878-103F";
    String enhetnamn = "VårdEnhet1A";
    String telefonnummer;

    // vardgivare
    String vardgivarId = "IFV1239877878-103F"
    String vardgivarnamn = "VårdEnhet1A"

    String komplett

    def execute() {
        def restClient = createRestClient(baseUrl)
        restClient.post(path: "intyg",
        body: json(),
        requestContentType: JSON
        )

        restClient = createRestClient(baseUrl)
        restClient.put(path: "intyg/$intygId",
            body: """{"id":"$intygId","patientNamn":"$forNamn $efterNamn","patientPersonnummer":"$personNummer","avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"rehabiliteringAktuell":false,"rehabiliteringEjAktuell":false,"rehabiliteringGarInteAttBedoma":false,"arbetsloshet":false,"foraldrarledighet":false,"arbetsformataPrognosJa":false,"arbetsformataPrognosJaDelvis":false,"arbetsformataPrognosNej":false,"arbetsformataPrognosGarInteAttBedoma":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":false,"kontaktMedFk":false,"vardperson":{"hsaId":"$hsaId","namn":"$namn","enhetsId":"$enhetId","enhetsnamn":"$enhetnamn","vardgivarId":"$vardgivarId","vardgivarnamn":"$vardgivarnamn"},"namnfortydligandeOchAdress":"$forNamn $efterNamn\\n$enhetnamn","undersokningAvPatienten":"2014-05-06","diagnosKod":"S50","diagnosBeskrivning":"Skada underarm","sjukdomsforlopp":"Trillade och skrapade armen mot trottoaren","funktionsnedsattning":"Kan inte lyfta armen","basedOnWork":"CURRENT","nuvarandeArbetsuppgifter":"Armlyftare","nedsattMed100":{"from":"2014-05-06","tom":"2014-07-31"},"prognosis":"YES"}""",
            requestContentType: JSON
        )

        if ("ja".equalsIgnoreCase(komplett)) {
            restClient = createRestClient(baseUrl)
            restClient.put(path: "intyg/$intygId/komplett")
        }
    }

    private json() {
        [intygId:intygId, intygType:intygTyp, patient: [personNummer:personNummer,forNamn:forNamn,efterNamn:efterNamn],
         hosPerson:[namn:"Namn", hsaId:"hsaid",forskivarkod:"1234567890"],
         vardenhet:[hsaId:enhetId, namn:enhetnamn, vardgivare:[hsaId:vardgivarId, namn:vardgivarnamn]]]
    }
}
