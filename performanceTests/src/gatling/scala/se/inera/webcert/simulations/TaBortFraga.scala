package se.inera.webcert.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class TaBortFraga extends Simulation {

  val intyg = csv("intyg.csv").queue
  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080")

  val httpConf = http
    .baseURL(baseUrl)
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip,deflate")

  val headers_json = Map(
    "Content-Type" -> """application/json""")

  val scn = scenario("TaBortFraga")
    .feed(intyg)
    .exec(http("Delete the question")
      .delete("/testability/fragasvar/extern/FK-${intygsId}"))

  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}
