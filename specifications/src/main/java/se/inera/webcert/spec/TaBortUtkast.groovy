package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

class TaBortUtkast extends RestClientFixture {

    private def restClient = createRestClient("${baseUrl}services/")
    
    String utkastId
    String enhetsId
    
    // Allow use both as DecisionTable and Script fixture
    def execute() {
        if (utkastId) taBortUtkast(utkastId)
        if (enhetsId) taBortUtkastForEnhet(enhetsId)
    }

    def taBortUtkast(String utkastId) {
        restClient.delete(path: "intyg/${utkastId}")
    }
    
    def taBortUtkastForEnhet(String enhetsId) {
        restClient.delete(path: "intyg/enhet/${enhetsId}")
    }
    
}
