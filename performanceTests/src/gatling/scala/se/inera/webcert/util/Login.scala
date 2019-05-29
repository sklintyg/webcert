package se.inera.webcert.util

import io.gatling.http.Predef.http
import io.gatling.core.session.ExpressionWrapper

object Login {

  val logins = Map("Åsa-Enhet1" -> """{"forNamn": "Åsa", "efterNamn": "Andersson", "hsaId": "IFV1239877878-104B", "enhetId": "IFV1239877878-1042","legitimeradeYrkesgrupper": ["Läkare"], "origin": "NORMAL"}""",
    "Åsa-Enhet2" -> """{"forNamn": "Åsa", "efterNamn": "Andersson", "hsaId": "IFV1239877878-104B", "enhetId": "IFV1239877878-1045","legitimeradeYrkesgrupper": ["Läkare"], "origin": "NORMAL"}""",
    "Eva" -> """{"forNamn": "Eva", "efterNamn": "Holgersson", "hsaId": "eva", "enhetId": "centrum-vast", "legitimeradeYrkesgrupper": ["Läkare"], "origin": "NORMAL"}""",
    "Leonie" -> """{"forNamn": "Leonie", "efterNamn": "Koehl", "hsaId": "TSTNMT2321000156-103F", "enhetId": "TSTNMT2321000156-1039", "legitimeradeYrkesgrupper": ["Läkare"], "origin": "NORMAL"}""")

  def loginAs(login: String) = {
    http(ExpressionWrapper("Login").expression)
      .post(ExpressionWrapper("/fake").expression)
      .headers(Headers.form_urlencoded)
      .formParam(ExpressionWrapper("userJsonDisplay").expression, ExpressionWrapper(logins(login)).expression)
  }
}
