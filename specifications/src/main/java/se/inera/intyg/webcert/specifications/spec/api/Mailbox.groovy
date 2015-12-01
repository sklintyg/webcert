package se.inera.intyg.webcert.specifications.spec.api

import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

/**
 * @author andreaskaltenbach
 */
class Mailbox extends RestClientFixture {

    public void rensa() {
        def restClient = createRestClient("${baseUrl}services/")
        restClient.delete(path: 'mail-stub/mails')
    }
}
