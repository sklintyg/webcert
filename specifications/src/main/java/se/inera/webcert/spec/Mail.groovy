package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

/**
 *
 * @author andreaskaltenbach
 */
class Mail extends RestClientFixture {

    def mail

    void execute() {
        def restClient = new RESTClient(baseUrl)
        mail = restClient.get(path: "mail-stub/mails").data[0]
    }

    def mottagare() {
        mail.recipients.toString()
    }

    def amne() {
        mail.subject
    }

    def innehall() {
        mail.body
    }
}
