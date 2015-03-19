package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

/**
 * @author andreaskaltenbach
 */
class Mailbox extends RestClientFixture {

    public void rensa() {
        def restClient = createRestClient("${baseUrl}services/")
        restClient.delete(path: 'mail-stub/mails')
    }
}
