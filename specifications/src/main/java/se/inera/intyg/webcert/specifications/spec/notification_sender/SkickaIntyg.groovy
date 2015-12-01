package se.inera.intyg.webcert.specifications.spec.notification_sender

import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils;
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils.*

import org.apache.commons.io.IOUtils

class SkickaIntyg {

    String intygId
    String intygTyp
    String mottagarId = "FK"

    def response

    public void execute() {
        WebcertRestUtils.login()
        response = WebcertRestUtils.sendIntyg(intygTyp, intygId, mottagarId)
    }

    public boolean intygSkickat() {
        response.success
    }
}
