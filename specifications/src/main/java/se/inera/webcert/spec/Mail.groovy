package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

/**
 *
 * @author andreaskaltenbach
 */
class Mail extends RestClientFixture {

    def mail;

    public void execute() {
        def restClient = new RESTClient(baseUrl)
        mail = restClient.get(path: "mail-stub/mails").data[0]
    }

    public def mottagare() {
        mail.recipients.toString()
    }

    public def amne() {
        mail.subject
    }

    public def innehall() {
        mail.body
    }

}
