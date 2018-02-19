package se.inera.webcert
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.io.Source._
import scala.concurrent.duration._
import scalaj.http._

object Utils {
  val baseUrl = System.getProperty("baseUrl", "http://localhost:9088" )

  def injectPersonsIntoPU(file : String, column : Int) = {
    val bufferedSource = fromFile("src/test/resources/data/" + file)
    for (line <- bufferedSource.getLines) {
      val cols = line.split(",").map(_.trim)
      injectPersonIntoPU(cols(column))
    }
    bufferedSource.close
  }

  def injectPersonIntoPU(personnummer: String) = {
    var url = baseUrl + "/services/api/pu-api/person"
    Http(url)
      .postData(s"""{ "sekretessmarkering": "N", "senasteAndringFolkbokforing": null, "personpost": { "personId" : "${personnummer}", "namn": { "fornamn": "Test", "efternamn": "Testsson" }, "folkbokforingsadress": { "utdelningsadress2": "Adress1", "postNr": "12345", "postort": "postort" }}}""")
      .method("put")
      .option(HttpOptions.allowUnsafeSSL)
      .option(HttpOptions.connTimeout(5000))
      .option(HttpOptions.readTimeout(5000))
      .header("Content-type", "application/json").asString.code
  }

  def removePersonsFromPU(file : String, column : Int) = {
    val bufferedSource = fromFile("src/test/resources/data/" + file)
    for (line <- bufferedSource.getLines) {
      val cols = line.split(",").map(_.trim)
      removePersonFromPU(cols(column))
    }
    bufferedSource.close
  }

  def removePersonFromPU(personnummer: String) = {
    var url = baseUrl + "/services/api/pu-api/person/" + personnummer
    Http(url)
      .method("delete")
      .header("Content-type", "application/json")
      .option(HttpOptions.allowUnsafeSSL)
      .option(HttpOptions.connTimeout(5000))
      .option(HttpOptions.readTimeout(5000))
      .asString
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
      .option(HttpOptions.allowUnsafeSSL)
      .option(HttpOptions.connTimeout(5000))
      .option(HttpOptions.readTimeout(5000))
      .asString
  }

  def deleteItemsFromUrl(url: String, issues: List[String]) = {
    for (item <- issues) {
      var finalUrl = baseUrl + url + item
      Http(finalUrl)
        .method("delete")
        .header("content-type", "application/json")
        .option(HttpOptions.allowUnsafeSSL)
        .option(HttpOptions.connTimeout(5000))
        .option(HttpOptions.readTimeout(5000))
        .asString
    }
  }
}
