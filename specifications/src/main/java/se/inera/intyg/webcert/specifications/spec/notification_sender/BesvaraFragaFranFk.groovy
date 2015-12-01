package se.inera.webcert.spec.notification_sender

import se.inera.webcert.spec.util.WebcertRestUtils;
import se.inera.webcert.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.core.io.ClassPathResource

import static se.inera.webcert.spec.util.WebcertRestUtils.*

import org.apache.commons.io.IOUtils

class BesvaraFragaFranFk {

    String intygTyp
    String externReferens

    def response

    public void execute() {
        WebcertRestUtils.login()
        def internReferens = WebcertRestUtils.translateExternalToInternalReferens(externReferens)
        response = WebcertRestUtils.answerQuestion(intygTyp, internReferens, makeText())
    }

    def makeText() {
        "Här är ett svar"
    }

    public boolean fragaBesvarad() {
        response.success
    }
}
