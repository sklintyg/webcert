package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

class TaBortFraga extends RestClientFixture {

    String internReferens
    String externReferens

    def execute() {
        def restClient = createRestClient(baseUrl)
        if (internReferens) restClient.delete(path: "questions/${internReferens}")
        if (externReferens) restClient.delete(path: "questions/extern/${externReferens}")
    }
}
