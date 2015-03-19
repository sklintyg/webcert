package se.inera.webcert.spec.notification_sender

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static se.inera.webcert.spec.util.WebcertRestUtils.*
import se.inera.webcert.spec.util.WebcertRestUtils

class MakuleraIntyg {

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
        response = WebcertRestUtils.deleteIntyg(intygTyp, intygId)
    }

    public boolean intygMakulerat() {
        response.success
    }
}
