package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

class RensaFragorOchSvar extends RestClientFixture {

    def restClient = createRestClient(baseUrl)

    def taBortFragaMedExternReferens(String externReferens) {
        restClient.delete(path: "questions/extern/${externReferens}")
    }

    def taBortFragaMedInternReferens(String internReferens) {
        restClient.delete(path: "questions/${internReferens}")
    }

    def taBortAllaFragor() {
        restClient.delete(path: "questions/")
    }
}
