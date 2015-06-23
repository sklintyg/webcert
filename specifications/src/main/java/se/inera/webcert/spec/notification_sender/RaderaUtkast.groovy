package se.inera.webcert.spec.notification_sender
import se.inera.webcert.spec.util.WebcertRestUtils

class RaderaUtkast {

    String intygId
    String intygTyp
    long   version

    def response

    public void execute() {
        WebcertRestUtils.login()
        response = WebcertRestUtils.deleteUtkast(intygTyp, intygId, version)
    }

    public boolean utkastRaderat() {
        response.success
    }
}
