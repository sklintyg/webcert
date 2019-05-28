package se.inera.webcert.util

import io.gatling.http.Predef.http
import io.gatling.core.session.ExpressionWrapper

object Conf {

  val baseUrl = System.getProperty("baseUrl", "http://localhost:9088")
  println("Base url: " + baseUrl)
  val httpConf = http
    .baseURL(baseUrl)
    .acceptHeader(ExpressionWrapper("*/*").expression)
    .acceptEncodingHeader(ExpressionWrapper("gzip, deflate").expression)
    .connection(ExpressionWrapper("keep-alive").expression)

}
