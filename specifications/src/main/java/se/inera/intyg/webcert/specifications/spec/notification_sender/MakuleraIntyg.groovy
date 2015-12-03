package se.inera.intyg.webcert.specifications.spec.notification_sender

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils.*
import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils

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
