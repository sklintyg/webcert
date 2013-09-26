package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

/**
 *
 * @author andreaskaltenbach
 */
class Mailbox extends RestClientFixture {

    public void rensa() {
        def restClient = new RESTClient(baseUrl)
        restClient.delete(path: 'mail-stub/mails')
    }
}