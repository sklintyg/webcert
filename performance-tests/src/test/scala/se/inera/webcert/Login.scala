package se.inera.webcert 
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

object Login {

	val logins = Map("Åsa-Enhet1" -> """{"fornamn": "Åsa", "efternamn": "Andersson", "hsaId": "IFV1239877878-104B", "enhetId": "IFV1239877878-1042","lakare": true}""",
	                 "Åsa-Enhet2" -> """{"fornamn": "Åsa", "efternamn": "Andersson", "hsaId": "IFV1239877878-104B", "enhetId": "IFV1239877878-1045","lakare": true}""",
	                 "Eva" -> """{"fornamn": "Eva", "efternamn": "Holgersson", "hsaId": "eva", "enhetId": "centrum-vast", "lakare": true}""",
	                 "Leonie" -> """{"fornamn": "Leonie", "efternamn": "Koehl", "hsaId": "TSTNMT2321000156-103F", "enhetId": "TSTNMT2321000156-1039", "lakare": true}"""
	                 )
	
	def loginAs(login: String) = {
		http("Login")
				.post("/fake")
				.headers(Headers.form_urlencoded)
					.formParam("userJsonDisplay", logins(login))
	}
}