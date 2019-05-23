package se.inera.webcert.util

object Headers {

  val default = Map(
    "Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""")

  val form_urlencoded = Map(
    "Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
    "Content-Type" -> """application/x-www-form-urlencoded""",
    "Pragma" -> """no-cache""")

  val json = Map(
    "Accept" -> """application/json, text/plain, */*""",
    "Content-Type" -> """application/json;charset=UTF-8""")
}
