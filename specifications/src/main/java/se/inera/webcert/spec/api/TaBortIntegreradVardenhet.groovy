package se.inera.webcert.spec.api

import se.inera.webcert.spec.util.RestClientFixture

/**
 * Created by eriklupander on 2015-12-02.
 */
class TaBortIntegreradVardenhet extends RestClientFixture {

    private def restClient = createRestClient("${baseUrl}testability/")

    String hsaId
    def response;

    public String respons(){
        return response.status;
    }

    // Allow use both as DecisionTable and Script fixture
    def execute() {
        if (hsaId) taBortIntegreradVardenhet(hsaId)
    }

    def taBortIntegreradVardenhet(String hsaId) {
        response = restClient.delete(path: "integreradevardenheter/${hsaId}")
    }

}