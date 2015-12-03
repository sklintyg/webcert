package se.inera.intyg.webcert.specifications.spec.api

import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

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
