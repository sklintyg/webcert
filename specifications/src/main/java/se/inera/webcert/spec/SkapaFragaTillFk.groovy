package se.inera.webcert.spec

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

class SkapaFragaTillFk extends RestClientFixture {

    String intygId
    String intygTyp
    String hsaUser = "user1"

    public setIntygId (String value) {
        intygId = value
    }
    public setIntygTyp(String value) {
        intygTyp = value
    }
    public setHsaUser(String value) {
        hsaUser = value 
    }

    def response

    public void execute() {
        WebcertRestUtils.login(hsaUser)
        response = WebcertRestUtils.createQuestionToFk(intygTyp,intygId,makeJson())
    }

    def makeJson() {
        def fraga = new JsonSlurper().parse(new InputStreamReader(new ClassPathResource("fraga_svar_template.json").getInputStream()))
        JsonOutput.toJson(fraga)
    }

    public boolean fragaSkapad() {
        response.success
    }
}
