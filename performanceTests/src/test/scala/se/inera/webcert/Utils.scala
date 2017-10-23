package se.inera.webcert
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.io.Source._
import scala.concurrent.duration._
import scalaj.http._

object Utils {
  val baseUrl = System.getProperty("baseUrl", "http://localhost:9088" )

  def injectPersonsIntoPU() = {
    val bufferedSource = fromFile("src/test/resources/data/intyg.csv")
    for (line <- bufferedSource.getLines) {
      val cols = line.split(",").map(_.trim)
      injectPersonIntoPU(cols(1))
    }
    bufferedSource.close
  }

  def injectPersonIntoPU(personnummer: String) = {
    http(s"Inject person $personnummer")
      .put("/services/pu-api/person")
      .headers(Headers.json)
      .body(StringBody(
        s"""{ "sekretessmarkering": "N", "senasteAndringFolkbokforing": null, "personpost": { "personId" : "${personnummer}", "namn": { "fornamn": "Test", "efternamn": "Testsson" }, "folkbokforingsadress": { "utdelningsadress2": "Adress1", "postNr": "12345", "postort": "postort" }}}"""
      ))
      .check(status.is(200))
  }

  def removePersonsFromPU() = {
    val bufferedSource = fromFile("src/test/resources/data/intyg.csv")
    for (line <- bufferedSource.getLines) {
      val cols = line.split(",").map(_.trim)
      removePersonFromPU(cols(1))
    }
    bufferedSource.close
  }

  def removePersonFromPU(personnummer: String) = {
    http(s"Remove person $personnummer")
      .delete("/services/pu-api/person")
      .headers(Headers.json)
      .body(StringBody(
        """{ "sekretessmarkering": "N", "senasteAndringFolkbokforing": null, "personpost": { "personId" : "$personnummer", "namn": { "fornamn": "Test", "efternamn": "Testsson" }, "folkbokforingsadress": { "utdelningsadress2": "Adress1", "postNr": "12345", "postort": "postort" }}}"""
      ))
  }

  def cleanCertificates() = {
    val bufferedSource = fromFile("src/test/resources/data/intyg.csv")
    for (line <- bufferedSource.getLines) {
      val cols = line.split(",").map(_.trim)
      deleteCertificate(cols(0))
    }
    bufferedSource.close
  }

  def deleteCertificate(id : String) : HttpResponse[String] = {
    var url = baseUrl + "/testability/intyg/" + id
    Http(url)
      .method("delete")
      .header("content-type", "application/json")
      .asString
  }

  def deleteItemsFromUrl(url: String, issues: List[String]) = {
    for (item <- issues) {
      var finalUrl = baseUrl + url + item
      Http(finalUrl)
        .method("delete")
        .header("content-type", "application/json")
        .asString
    }
  }
}