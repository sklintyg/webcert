package se.inera.intyg.webcert.specifications.spec.notification_sender
import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils

class SigneraUtkast {

    String intygId
    String intygTyp
    long   version

    def response

    public void execute() {
        //WebcertRestUtils.login(WebcertRestUtils.restClient)
        response = WebcertRestUtils.signUtkastUsingBrowserSesssion(intygTyp, intygId, version)
    }

    public boolean utkastSignerat() {
        response.success
    }

    public long version() {
        response.data.version
    }

}
