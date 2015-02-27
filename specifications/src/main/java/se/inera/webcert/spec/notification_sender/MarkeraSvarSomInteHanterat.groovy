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

class MarkeraSvarSomInteHanterat extends RestClientFixture {

    String intygTyp
    String hsaUser = "user1"
    String internReferens

    public setIntygTyp(String value) {
        intygTyp = value
    }
    public setHsaUser(String value) {
        hsaUser = value 
    }

    def response

    public void execute() {
        WebcertRestUtils.login(hsaUser)
        response = WebcertRestUtils.setQuestionAsUnhandled(intygTyp, internReferens)
    }

    public boolean markeradSomInteHanterad() {
        response.success
    }

}
