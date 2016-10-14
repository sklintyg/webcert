package se.inera.webcert
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._

class FragaSvar extends Simulation {

  val intyg = csv("data/intyg.csv").circular

  val scn = scenario("FragaSvar")
    .exec(Login.loginAs("Åsa-Enhet1"))
    .exec(http("Get user details")
      .get("/siths.jsp"))
    .repeat(100, "i") {
      feed(intyg)
        .exec(http("Inject question")
          .post("/testability/questions")
          .headers(Headers.json)
          .body(StringBody("""
{"amne":"OVRIGT",
 "externReferens":"FK-${intygsId}",
 "frageSigneringsDatum":"2012-12-22T21:00:00.000",
 "frageSkickadDatum":"2013-01-01",
 "frageStallare":"FK",
 "frageText":"frageText",
 "intygsReferens":{"intygsId":"${intygsId}",
	           "intygsTyp":"fk7263",
		   "patientId":"${personNr}",
		   "patientNamn":"${namn}",
		   "signeringsDatum":"2012-12-23T21:00:00.000"},
 "meddelandeRubrik":"meddelandeRubrik",
 "status":"PENDING_INTERNAL_ACTION",
 "vardperson":{"enhetsId":"${enhetsId}",
	       "enhetsnamn":"${enhetsNamn}",
	       "vardgivarId": "${vardgivarId}",
	       "vardgivarnamn" : "${vardgivarNamn}",
	       "hsaId":"${vardPersonId}",
	       "namn":"${vardPersonNamn}"}}"""))
          .check(jsonPath("$.internReferens").saveAs("internReferens-franfk")))
        .exec(http("Dashboard")
          .get("/web/dashboard#/unhandled-qa.html")
          .headers(Headers.default))
        .exec(http("Get statistics")
          .get("/moduleapi/stat/")
          .headers(Headers.json))
        .exec(http("Get doctor status")
          .get("/api/fragasvar/lakare")
          .headers(Headers.json))
        .exec(http("Get Fraga/Svar for unit")
          .get("/api/fragasvar/sok?pageSize=10&questionFromFK=false&questionFromWC=false&startFrom=0&vantarPa=ALLA_OHANTERADE")
          .headers(Headers.json))
        .exec(http("Get QAs")
          .get("/moduleapi/fragasvar/fk7263/${intygsId}")
          .headers(Headers.json))
        .exec(http("Answer the question")
          .put("/moduleapi/fragasvar/fk7263/${internReferens-franfk}/besvara")
          .body(StringBody("""SvarsText-${internReferens-franfk}"""))
          .headers(Headers.json))
        .exec(http("Delete incoming question")
          .delete("/testability/questions/${internReferens-franfk}"))
    }
    .exec(http("Logout")
      .get("/logout")
      .headers(Headers.default))

  setUp(scn.inject(rampUsers(10) over (10 seconds)).protocols(Conf.httpConf))
}