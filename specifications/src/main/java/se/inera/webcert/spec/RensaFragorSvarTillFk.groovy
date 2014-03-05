package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

/**
 * @author pehra
 */
class RensaFragorSvarTillFk extends RestClientFixture {

    def rensaFragor() {
        def restClient = createRestClient(baseUrl)
        restClient.delete(path: "fk-stub/fragor/")
    }

    def rensaSvar() {
        def restClient = createRestClient(baseUrl)
        restClient.delete(path: "fk-stub/svar/")
    }
}
