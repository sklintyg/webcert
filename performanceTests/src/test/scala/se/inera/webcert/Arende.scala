package se.inera.webcert
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._
import java.util.UUID
import io.gatling.core.feeder._
import scala.collection.mutable.ListBuffer

class Arende extends Simulation {

  private var internReferenser = new ListBuffer[String]

  val intyg = csv("data/intyg.csv").circular

  val uuid = Iterator.continually(
    // Random UUID will be accessible in session under variable "UUID"
    Map("UUID" -> UUID.randomUUID()))

  val scn = scenario("Arende")
    .exec(Login.loginAs("Åsa-Enhet1"))
    .exec(http("Get user details")
      .get("/siths.jsp"))
    .repeat(100, "i") {
      feed(intyg)
      .feed(uuid)
        .exec(http("Inject Intyg")
          .post("/testability/intyg/utkast")
          .headers(Headers.json)
            .body(ELFileBody("request/intyg.json")).check(status.is(200)))
        .exec(http("Inject Arende")
          .post("/testability/arendetest")
          .headers(Headers.json)
          .body(StringBody("""
            {"id": 0,
            "timestamp": "2016-10-06T07:25:17.989Z",
            "meddelandeId": "${UUID}",
            "referensId": "LUSE-${intygsId}",
            "skickatTidpunkt": "2016-10-06T07:25:17.989Z",
            "intygsId": "${intygsId}",
            "patientPersonId": "${personNr}",
            "amne": "KOMPLT",
            "rubrik": "string",
            "meddelande": "string",
            "paminnelseMeddelandeId": "string",
            "svarPaId": "string",
            "svarPaReferens": "string",
            "skickatAv": "string",
            "kontaktInfo": [
              "string"
            ],
            "komplettering": [ ],
            "sistaDatumForSvar": "2016-10-06",
            "intygTyp": "luse",
            "signeratAv": "string",
            "signeratAvName": "string",
            "enhetId": "IFV1239877878-1042",
            "enhetName": "${enhetsNamn}",
            "status": "PENDING_INTERNAL_ACTION",
            "senasteHandelse": "2016-10-06T07:25:17.990Z",
            "vidarebefordrad": false,
            "vardaktorName": "${vardPersonNamn}",
            "vardgivareName": "${vardgivarNamn}"}"""))
          .check(
              status.is(200),
              jsonPath("$.meddelandeId").saveAs("internReferens")))
        .exec(session => {
          internReferenser += session("internReferens").as[String]
          session
        })
        .pause(50 milliseconds)
        .exec(http("Dashboard")
          .get("/web/dashboard#/unhandled-qa.html")
          .headers(Headers.default))
        .exec(http("Get statistics")
          .get("/moduleapi/stat/")
          .headers(Headers.json))
        .exec(http("Get doctor status")
          .get("/api/fragasvar/lakare")
          .headers(Headers.json))
        .exec(http("Get Arende for unit")
          .get("/api/fragasvar/sok?pageSize=10&questionFromFK=false&questionFromWC=false&startFrom=0&vantarPa=ALLA_OHANTERADE")
          .headers(Headers.json))
        .exec(http("Get QAs")
          .get("/moduleapi/arende/${intygsId}")
          .headers(Headers.json))
        .doIf("${internReferens}" != null && !"${internReferens}".isEmpty()) {
          exec(http("Answer the question ${internReferens}")
            .put("/moduleapi/arende/luse/${internReferens}/besvara")
            .body(StringBody("""SvarsText-${internReferens}"""))
            .headers(Headers.json))
        }

    }
    .exec(http("Logout")
      .get("/logout")
      .headers(Headers.default))

  setUp(scn.inject(rampUsers(10) over (10 seconds)).protocols(Conf.httpConf))

  after {
    println("Cleaning test data")
    Utils.deleteItemsFromUrl("/testability/arendetest", internReferenser.toList)
    Utils.cleanCertificates()
  }
}