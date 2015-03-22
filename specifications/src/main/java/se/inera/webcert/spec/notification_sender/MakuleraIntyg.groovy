package se.inera.webcert.spec.notification_sender

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static se.inera.webcert.spec.util.WebcertRestUtils.*
import se.inera.webcert.spec.util.WebcertRestUtils

class MakuleraIntyg {

    String intygId
    String intygTyp

    def response

    public void execute() {
        WebcertRestUtils.login()
        response = WebcertRestUtils.deleteIntyg(intygTyp, intygId)
    }

    public boolean intygMakulerat() {
        response.success
    }
}
