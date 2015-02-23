package se.inera.webcert.spec.notification_sender

import se.inera.webcert.spec.util.WebcertRestUtils;
import se.inera.webcert.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static se.inera.webcert.spec.util.WebcertRestUtils.*

import org.apache.commons.io.IOUtils

class SigneraUtkast extends RestClientFixture {

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
        response = WebcertRestUtils.signUtkast(intygTyp, intygId)
    }

    public boolean utkastSignerat() {
        response.success
    }
}
