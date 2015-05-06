package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

class TaBortOmsandning extends RestClientFixture {

    private def restClient = createRestClient("${baseUrl}services/")

    // Allow use both as DecisionTable and Script fixture
    def execute() {
        taBortOmsandningar()
    }

    void taBortOmsandningar() {
        restClient.delete(path: "omsandning")
    }

}
