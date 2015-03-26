package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

/**
 * @author pehra
 */
class RensaFragorSvarTillFk extends RestClientFixture {

    def restClient = createRestClient("${baseUrl}services/")

    def rensaFragor() {
        restClient.delete(path: "fk-stub/fragor/")
    }

    def rensaSvar() {
        restClient.delete(path: "fk-stub/svar/")
    }
}
