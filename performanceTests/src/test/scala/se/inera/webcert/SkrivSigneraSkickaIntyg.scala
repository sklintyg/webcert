package se.inera.webcert
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._

class SkrivSigneraSkickaIntyg extends Simulation {

  val testpersonnummer = csv("data/testpersonnummer_skatteverket_subset.cvs").circular
  val bootstrapPu = csv("data/testpersonnummer_skatteverket_subset.cvs").records

  val bootstrap = exec("Bootstrap PU")
    .foreach(bootstrapPu, "personNr") {
      exec(flattenMapIntoAttributes("${personNr}"))
          .exec(Utils.injectPersonIntoPU("${personNr}"))
    }

  val scn = scenario("SkrivSigneraSkicka")
    .exec(bootstrap)
    .exec(Login.loginAs("Leonie"))
    .exec(http("Get user details")
      .get("/siths.jsp"))
    .pause(50 milliseconds)
    .repeat(10, "i") {
      feed(testpersonnummer)
        .exec(http("Dashboard")
          .get("/web/dashboard#/unhandled-qa.html")
          .headers(Headers.default))
        .exec(http("Get statistics")
          .get("/moduleapi/stat/")
          .headers(Headers.json))
        .exec(http("Get name from PU-tjanst")
          .get("/api/person/${personNr}")
          .headers(Headers.json))
        .exec(http("Create draft")
          .post("/api/utkast/fk7263")
          .headers(Headers.json)
          .body(StringBody("""{"intygType":"fk7263","patientPersonnummer":"${personNr}","patientFornamn":"test","patientEfternamn":"test"}"""))
            .check(
              status.is(200),
              jsonPath("$.intygsId").saveAs("intyg")))
        .exec(http("Get draft certificate")
          .get("/moduleapi/utkast/fk7263/${intyg}")
          .headers(Headers.json))
        .exec(http("Autosave certificate 1")
          .put("/moduleapi/utkast/fk7263/${intyg}/0?autoSave=true")
          .headers(Headers.json)
          .body(StringBody("""{"id":"${intyg}","grundData":{"skapadAv":{"personId":"TSTNMT2321000156-103F","fullstandigtNamn":"Leonie Koehl","befattningsKod": "203090","forskrivarKod":"9300005","vardenhet":{"enhetsid":"TSTNMT2321000156-1039","enhetsnamn":"NMT vg1 ve2","postadress":"Storgatan 1","postnummer":"12345","postort":"Småmåla","telefonnummer":"0101234567890","epost":"enhet1@webcert.invalid.se","vardgivare":{"vardgivarid":"TSTNMT2321000156-1002","vardgivarnamn":"NMT vg1"},"arbetsplatsKod":"0000000"}},"patient":{"personId":"${personNr}","fullstandigtNamn":"test test","fornamn":"test","efternamn":"test","samordningsNummer":false}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"rekommendationOvrigtCheck":false,"nuvarandeArbete":true,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":true,"kontaktMedFk":false,"forskrivarkodOchArbetsplatskod":"2481632 - 0000000","namnfortydligandeOchAdress":"Åsa Andersson\nWebCert-Enhet1\nStorgatan 1\n12345 Småmåla\n0101234567890","undersokningAvPatienten":"2015-02-20","diagnosKodsystem1":"ICD_10_SE","diagnosKodsystem2":"ICD_10_SE","diagnosKodsystem3":"ICD_10_SE","annanReferensBeskrivning":null,"nuvarandeArbetsuppgifter":null,"rekommendationOvrigt":null,"rehabilitering":"rehabiliteringEjAktuell","nedsattMed25Beskrivning":null,"nedsattMed50Beskrivning":null,"nedsattMed75Beskrivning":null,"nedsattMed100Beskrivning":null,"arbetsformagaPrognosGarInteAttBedomaBeskrivning":null,"prognosBedomning":"arbetsformagaPrognosJa"}""")))
        .exec(http("Lookup code")
          .post("/moduleapi/diagnos/kod/sok")
          .body(StringBody("""{"codeSystem":"ICD_10_SE","codeFragment":"A00","nbrOfResults":10}"""))
          .headers(Headers.json))
        .exec(http("Lookup FMB")
          .get("/api/fmb/J22?cacheSlayer=" + System.currentTimeMillis())
          .headers(Headers.json))
        .exec(http("Autosave certificate 2")
          .put("/moduleapi/utkast/fk7263/${intyg}/1?autoSave=true")
          .headers(Headers.json)
          .body(StringBody("""{"id":"${intyg}","grundData":{"skapadAv":{"personId":"TSTNMT2321000156-103F","fullstandigtNamn":"Leonie Koehl","befattningsKod": "203090","forskrivarKod":"9300005","vardenhet":{"enhetsid":"TSTNMT2321000156-1039","enhetsnamn":"NMT vg1 ve2","postadress":"Storgatan 1","postnummer":"12345","postort":"Småmåla","telefonnummer":"0101234567890","epost":"enhet1@webcert.invalid.se","vardgivare":{"vardgivarid":"TSTNMT2321000156-1002","vardgivarnamn":"NMT vg1"},"arbetsplatsKod":"0000000"}},"patient":{"personId":"${personNr}","fullstandigtNamn":"test test","fornamn":"test","efternamn":"test","samordningsNummer":false}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"rekommendationOvrigtCheck":false,"nuvarandeArbete":true,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":true,"kontaktMedFk":false,"forskrivarkodOchArbetsplatskod":"2481632 - 0000000","namnfortydligandeOchAdress":"Åsa Andersson\nWebCert-Enhet1\nStorgatan 1\n12345 Småmåla\n0101234567890","undersokningAvPatienten":"2015-02-20","diagnosKodsystem1":"ICD_10_SE","diagnosKodsystem2":"ICD_10_SE","diagnosKodsystem3":"ICD_10_SE","annanReferensBeskrivning":null,"nuvarandeArbetsuppgifter":null,"rekommendationOvrigt":null,"rehabilitering":"rehabiliteringEjAktuell","nedsattMed25Beskrivning":null,"nedsattMed50Beskrivning":null,"nedsattMed75Beskrivning":null,"nedsattMed100Beskrivning":null,"arbetsformagaPrognosGarInteAttBedomaBeskrivning":null,"prognosBedomning":"arbetsformagaPrognosJa","diagnosKod":"A00","diagnosBeskrivning1":"Kolera","sjukdomsforlopp":"sjukdom"}""")))
        .exec(http("Autosave certificate 3")
          .put("/moduleapi/utkast/fk7263/${intyg}/2?autoSave=true")
          .headers(Headers.json)
          .body(StringBody("""{"id":"${intyg}","grundData":{"skapadAv":{"personId":"TSTNMT2321000156-103F","fullstandigtNamn":"Leonie Koehl","befattningsKod": "203090","forskrivarKod":"9300005","vardenhet":{"enhetsid":"TSTNMT2321000156-1039","enhetsnamn":"NMT vg1 ve2","postadress":"Storgatan 1","postnummer":"12345","postort":"Småmåla","telefonnummer":"0101234567890","epost":"enhet1@webcert.invalid.se","vardgivare":{"vardgivarid":"TSTNMT2321000156-1002","vardgivarnamn":"NMT vg1"},"arbetsplatsKod":"0000000"}},"patient":{"personId":"${personNr}","fullstandigtNamn":"test test","fornamn":"test","efternamn":"test","samordningsNummer":false}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"rekommendationOvrigtCheck":false,"nuvarandeArbete":true,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":true,"kontaktMedFk":false,"forskrivarkodOchArbetsplatskod":"2481632 - 0000000","namnfortydligandeOchAdress":"Åsa Andersson\nWebCert-Enhet1\nStorgatan 1\n12345 Småmåla\n0101234567890","undersokningAvPatienten":"2015-02-20","diagnosKodsystem1":"ICD_10_SE","diagnosKodsystem2":"ICD_10_SE","diagnosKodsystem3":"ICD_10_SE","annanReferensBeskrivning":null,"nuvarandeArbetsuppgifter":null,"rekommendationOvrigt":null,"rehabilitering":"rehabiliteringEjAktuell","nedsattMed25Beskrivning":null,"nedsattMed50Beskrivning":null,"nedsattMed75Beskrivning":null,"nedsattMed100Beskrivning":null,"arbetsformagaPrognosGarInteAttBedomaBeskrivning":null,"prognosBedomning":"arbetsformagaPrognosJa","diagnosKod":"A00","diagnosBeskrivning1":"Kolera","sjukdomsforlopp":"sjukdom","funktionsnedsattning":"nedsättning","aktivitetsbegransning":"begränsning"}""")))
        .exec(http("Autosave certificate 4")
          .put("/moduleapi/utkast/fk7263/${intyg}/3?autoSave=true")
          .headers(Headers.json)
          .body(StringBody("""{"id":"${intyg}","grundData":{"skapadAv":{"personId":"TSTNMT2321000156-103F","fullstandigtNamn":"Leonie Koehl","befattningsKod": "203090","forskrivarKod":"9300005","vardenhet":{"enhetsid":"TSTNMT2321000156-1039","enhetsnamn":"NMT vg1 ve2","postadress":"Storgatan 1","postnummer":"12345","postort":"Småmåla","telefonnummer":"0101234567890","epost":"enhet1@webcert.invalid.se","vardgivare":{"vardgivarid":"TSTNMT2321000156-1002","vardgivarnamn":"NMT vg1"},"arbetsplatsKod":"0000000"}},"patient":{"personId":"${personNr}","fullstandigtNamn":"test test","fornamn":"test","efternamn":"test","samordningsNummer":false}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"rekommendationOvrigtCheck":false,"nuvarandeArbete":true,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":true,"kontaktMedFk":false,"forskrivarkodOchArbetsplatskod":"2481632 - 0000000","namnfortydligandeOchAdress":"Åsa Andersson\nWebCert-Enhet1\nStorgatan 1\n12345 Småmåla\n0101234567890","undersokningAvPatienten":"2015-02-20","diagnosKodsystem1":"ICD_10_SE","diagnosKodsystem2":"ICD_10_SE","diagnosKodsystem3":"ICD_10_SE","annanReferensBeskrivning":null,"nuvarandeArbetsuppgifter":"arbete","rekommendationOvrigt":null,"rehabilitering":"rehabiliteringEjAktuell","nedsattMed25Beskrivning":null,"nedsattMed50Beskrivning":null,"nedsattMed75Beskrivning":null,"nedsattMed100Beskrivning":null,"arbetsformagaPrognosGarInteAttBedomaBeskrivning":null,"prognosBedomning":"arbetsformagaPrognosJa","diagnosKod":"A00","diagnosBeskrivning1":"Kolera","sjukdomsforlopp":"sjukdom","funktionsnedsattning":"nedsättning","aktivitetsbegransning":"begränsning","nedsattMed100":{"from":"2016-02-20","tom":"2016-02-27"}}""")))
        .exec(http("Autosave certificate 5")
          .put("/moduleapi/utkast/fk7263/${intyg}/4?autoSave=true")
          .headers(Headers.json)
          .body(StringBody("""{"id":"${intyg}","grundData":{"skapadAv":{"personId":"TSTNMT2321000156-103F","fullstandigtNamn":"Leonie Koehl","befattningsKod": "203090","forskrivarKod":"9300005","vardenhet":{"enhetsid":"TSTNMT2321000156-1039","enhetsnamn":"NMT vg1 ve2","postadress":"Storgatan 1","postnummer":"12345","postort":"Småmåla","telefonnummer":"0101234567890","epost":"enhet1@webcert.invalid.se","vardgivare":{"vardgivarid":"TSTNMT2321000156-1002","vardgivarnamn":"NMT vg1"},"arbetsplatsKod":"0000000"}},"patient":{"personId":"${personNr}","fullstandigtNamn":"test test","fornamn":"test","efternamn":"test","samordningsNummer":false}},"avstangningSmittskydd":false,"rekommendationKontaktArbetsformedlingen":false,"rekommendationKontaktForetagshalsovarden":false,"rekommendationOvrigtCheck":false,"nuvarandeArbete":true,"arbetsloshet":false,"foraldrarledighet":false,"ressattTillArbeteAktuellt":false,"ressattTillArbeteEjAktuellt":true,"kontaktMedFk":false,"forskrivarkodOchArbetsplatskod":"2481632 - 0000000","namnfortydligandeOchAdress":"Åsa Andersson\nWebCert-Enhet1\nStorgatan 1\n12345 Småmåla\n0101234567890","undersokningAvPatienten":"2015-02-20","diagnosKodsystem1":"ICD_10_SE","diagnosKodsystem2":"ICD_10_SE","diagnosKodsystem3":"ICD_10_SE","annanReferensBeskrivning":null,"nuvarandeArbetsuppgifter":"arbete","rekommendationOvrigt":null,"rehabilitering":"rehabiliteringEjAktuell","nedsattMed25Beskrivning":null,"nedsattMed50Beskrivning":null,"nedsattMed75Beskrivning":null,"arbetsformagaPrognosGarInteAttBedomaBeskrivning":null,"prognosBedomning":"arbetsformagaPrognosJa","diagnosKod":"A00","diagnosBeskrivning1":"Kolera","sjukdomsforlopp":"sjukdom","funktionsnedsattning":"nedsättning","aktivitetsbegransning":"begränsning","nedsattMed100":{"from":"2016-02-20","tom":"2016-02-27"},"kommentar":"Övrigt"}""")))
        .exec(http("Sign certificate")
          .post("/moduleapi/utkast/fk7263/${intyg}/5/signeraserver")
          .headers(Headers.json))
        .exec(http("Get signature")
          .get("/moduleapi/utkast/fk7263/${intyg}/signeringsstatus")
          .headers(Headers.json))
        .exec(http("Get QAs")
          .get("/moduleapi/fragasvar/fk7263/${intyg}")
          .headers(Headers.json))
        .exec(http("Get certificate")
          .get("/moduleapi/intyg/fk7263/${intyg}")
          .headers(Headers.json))
        .exec(http("Send certificate")
          .post("/moduleapi/intyg/fk7263/${intyg}/skicka")
          .headers(Headers.json)
          .body(StringBody("""{"recipient":"FK","patientConsent":true}""")))
        .exec(http("Get signed certificate")
          .get("/moduleapi/intyg/fk7263/${intyg}")
          .headers(Headers.json))
    }
    .exec(http("Logout")
      .get("/logout")
      .headers(Headers.default))

  before {
    println("Boostrapping PU")
    exec(bootstrap)
  }

  setUp(scn.inject(rampUsers(100) over (120 seconds)).protocols(Conf.httpConf))
}