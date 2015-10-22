package se.inera.webcert.spec.api

import se.inera.webcert.spec.util.RestClientFixture

class TaBortUtkast extends RestClientFixture {

    private def restClient = createRestClient("${baseUrl}services/")
    
    String utkastId
    String enhetsId
	def response;
	
	public String respons(){
		return response.status;
	}
    
    // Allow use both as DecisionTable and Script fixture
    def execute() {
        if (utkastId) taBortUtkast(utkastId)
        if (enhetsId) taBortUtkastForEnhet(enhetsId)
    }

    def taBortAllaUtkast() {
        response = restClient.delete(path: "intyg/")
    }
    
    def taBortUtkast(String utkastId) {
        response = restClient.delete(path: "intyg/${utkastId}")
    }
    
    def taBortUtkastForEnhet(String enhetsId) {
        response = restClient.delete(path: "intyg/enhet/${enhetsId}")
    }
    
}
