package se.inera.webcert.spec.notification_sender

import se.inera.webcert.spec.util.WebcertRestUtils;
import se.inera.webcert.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static se.inera.webcert.spec.util.WebcertRestUtils.*

import org.apache.commons.io.IOUtils

class RaderaUtkast {

    String intygId
    String intygTyp

    def response

    public void execute() {
        WebcertRestUtils.login()
        response = WebcertRestUtils.deleteUtkast(intygTyp, intygId)
    }

    public boolean utkastRaderat() {
        response.success
    }
}
