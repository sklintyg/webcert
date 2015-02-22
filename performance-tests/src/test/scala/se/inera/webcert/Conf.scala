package se.inera.webcert 
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

object Conf {

	val baseUrl = System.getProperty("baseUrl", "http://localhost:8080" )
	val httpConf = http
			.baseURL(baseUrl)
			.acceptHeader("*/*")
			.acceptEncodingHeader("gzip, deflate")
			.connection("keep-alive")

}