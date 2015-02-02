package se.inera.webcert 
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._

class InjiceraFraga extends Simulation {

	val intyg = csv("intyg.csv").queue
	val baseUrl = System.getProperty("baseUrl", "http://localhost:8080" )

	val httpConf = http
			.baseURL(baseUrl)
			.acceptHeader("*/*")
			.acceptEncodingHeader("gzip,deflate")


	val headers_json = Map(
			"Content-Type" -> """application/json"""
	)


	val scn = scenario("InjiceraFraga")
		.feed(intyg)
		.exec(http("Inject a question")
					.post("/services/questions")
					.headers(headers_json)
					.body(StringBody("""{"amne":"OVRIGT",
 "externReferens":"FK-${intygsId}",
 "frageSigneringsDatum":"2012-12-22T21:00:00.000",
 "frageSkickadDatum":"2013-01-01",
 "frageStallare":"FK",
 "frageText":"frageText",
 "intygsReferens":{"intygsId":"${intygsId}",
	           "intygsTyp":"fk7263",
		   "patientId":{"patientIdExtension":"${personNr}",
				"patientIdRoot":"1.2.752.129.2.1.3.1"},
		   "patientNamn":"${namn}",
		   "signeringsDatum":"2012-12-23T21:00:00.000"},
 "meddelandeRubrik":"meddelandeRubrik",
 "status":"PENDING_INTERNAL_ACTION",
 "vardperson":{"enhetsId":"SE2321000081-b3283",
	       "enhetsnamn":"Psyk Karlskrona Öst",
	       "vardgivarId": "SE2321000081-L001",
	       "vardgivarnamn" : "Landstinget Blekinge",
	       "hsaId":"${vardPersonId}",
	       "namn":"${vardPersonNamn}"}}"""))
		.check(jsonPath("$.internReferens").saveAs("internReferens"))
			)
		.exec { session =>
		  println(session("internReferens"))
		  session
		}

	setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}