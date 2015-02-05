package se.inera.webcert 
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.concurrent.duration._

class FragaSvarUserSimulation extends Simulation {

	val intyg = csv("intyg.csv").circular
	val baseUrl = System.getProperty("baseUrl", "http://localhost:8080" )
	
	val httpConf = http
			.baseURL(baseUrl)
			.acceptHeader("*/*")
			.acceptEncodingHeader("gzip, deflate")
			.connection("keep-alive")


	val headers_default = Map(
			"Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"""
	)

	val headers_form_urlencoded = Map(
			"Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
			"Content-Type" -> """application/x-www-form-urlencoded""",
			"Pragma" -> """no-cache"""
	)

	val headers_json = Map(
			"Accept" -> """application/json, text/plain, */*""",
			"Content-Type" -> """application/json;charset=UTF-8"""
	)


	val scn = scenario("FragaSvar")
	.exec(http("Login")
				.post("/fake")
				.headers(headers_form_urlencoded)
					.formParam("""jsonSelect""", """1""")
					.formParam("""userjson""", """%7B%0A%20%22fornamn%22%3A%20%22Åsa%22%2C%0A%20%22efternamn%22%3A%20%22Andersson%22%2C%0A%20%22hsaId%22%3A%20%22IFV1239877878-104B%22%2C%0A%20%22enhetId%22%3A%20%22IFV1239877878-1042%22%2C%0A%20%22lakare%22%3A%20true%0A%7D""")
					.formParam("""userJsonDisplay""", """
{
 "fornamn": "Åsa",
 "efternamn": "Andersson",
 "hsaId": "IFV1239877878-104B",
 "enhetId": "IFV1239877878-1042",
 "lakare": true
}""")
		)
	.pause(200 milliseconds)
	.exec(http("Get user details")
				.get("/siths.jsp")
		)
	.pause(50 milliseconds)
	.repeat(100, "i") {
		feed(intyg)
		.exec(http("Inject question")
			.post("/services/questions")
			.headers(headers_json)
			.body(StringBody("""
{"amne":"OVRIGT",
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
 "vardperson":{"enhetsId":"${enhetsId}",
	       "enhetsnamn":"${enhetsNamn}",
	       "vardgivarId": "${vardgivarId}",
	       "vardgivarnamn" : "${vardgivarNamn}",
	       "hsaId":"${vardPersonId}",
	       "namn":"${vardPersonNamn}"}}"""))
			.check(jsonPath("$.internReferens").saveAs("internReferens-franfk"))
				)
		.pause(2 seconds)
		.exec(http("Dashboard")
					.get("/views/dashboard/unhandled-qa.html")
					.headers(headers_json)
			)
		.pause(2 seconds)
		.exec(http("Get statistics")
					.get("/moduleapi/stat/")
					.headers(headers_json)
			)
		.pause(2 seconds)
		.exec(http("Get metadata for unit")
					.get("/api/fragasvar/mdlist/${enhetsId}")
					.headers(headers_json)
			)
		.pause(500 milliseconds)
		.exec(http("Get Fraga/Svar for unit")
					.put("/api/fragasvar/query")
					.headers(headers_json)
					.body(StringBody("""
						{"startFrom":0,
						 "pageSize":10,
						 "filter":{"enhetsId":"${enhetsId}",
							   "questionFromFK":false,
							   "questionFromWC":true,
							   "vantarPaSelector":{"label":"Alla som kräver åtgärd",
								               "value":"ALLA_OHANTERADE"},
							   "doctorSelector":{"name":"Alla"},
							   "vantarPa":"ALLA_OHANTERADE"}
						}"""))
			)
		.pause(5 seconds)
		.exec(http("Get specific certificate")
					.get("/m/fk7263/webcert/intyg/${intygsId}")
					.headers(headers_default)
			)
		.pause(10 milliseconds)
		.exec(http("Verify user step 1")
					.get("/usercontext.jsp")
			)
		.pause(10 milliseconds)
		.exec(http("Verify user step 2")
					.get("/siths.jsp")
			)
		.pause(100 milliseconds)
		.exec(http("Request the certificate")
					.get("/m/fk7263/webcert/views/view-cert.html")
					.headers(headers_json)
			)
		.pause(100 milliseconds)
		.exec(http("Retrieve the certificate content")
					.get("/moduleapi/intyg/${intygsId}")
					.headers(headers_json)
			)
		.pause(100 milliseconds)
		.exec(http("Retrieve the questions/aswers")
					.get("/moduleapi/fragasvar/${intygsId}")
					.headers(headers_json)
			)
		.pause(10 seconds)
		.exec(http("Answer the question")
			.put("/moduleapi/fragasvar/${internReferens-franfk}/answer")
			.headers(headers_default)
			.body(StringBody("""SvarsText-${internReferens-franfk}"""))
			)
		.pause(10 seconds)
		.exec(http("Send new question")
			.post("/moduleapi/fragasvar/${intygsId}")
			.headers(headers_json)
			.body(StringBody("""{"amne":"OVRIGT","frageText":"fraga-${intygsId}"}"""))
			.check(jsonPath("$.internReferens").saveAs("internReferens-tillfk"))
			)
		.pause(10 seconds)
		.exec(http("Mark sent question as handled")
			.get("/moduleapi/fragasvar/close/${internReferens-tillfk}")
			.headers(headers_default)
			)
		.pause(2 seconds)
		.exec(http("Delete incoming question")
			.delete("/services/questions/${internReferens-franfk}")
			)
		.pause(200 milliseconds)
		.exec(http("Delete sent question")
			.delete("/services/questions/${internReferens-tillfk}")
			)
		.pause(200 milliseconds)
	}
	.exec(http("Logout")
		.get("/logout")
		.headers(headers_default)
		)
	
	setUp(scn.inject(rampUsers(200) over (1800 seconds)).protocols(httpConf))
}