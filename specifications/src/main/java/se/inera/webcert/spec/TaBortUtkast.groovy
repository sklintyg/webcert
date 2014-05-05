package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

class TaBortUtkast extends RestClientFixture {

    String utkastId

    def execute() {
        def restClient = createRestClient(baseUrl)
        restClient.delete(path: "intyg/$utkastId")
    }

}
