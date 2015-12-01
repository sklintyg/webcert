package se.inera.intyg.webcert.specifications.spec.api

import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

class TaBortFraga extends RestClientFixture {

    private def restClient = createRestClient("${baseUrl}testability/")
    
    String internReferens
    String externReferens
    String frageText
    String svarsText
	def response;
	
	
	public String respons(){
		return response.status;
	}
    
    // Allow use both as DecisionTable and Script fixture
    def execute() {
        if (internReferens) taBortFragaMedInternReferens(internReferens)
        if (externReferens) taBortFragaMedExternReferens(externReferens)
        if (frageText) taBortFragaMedFrageText(frageText)
        if (svarsText) taBortFragaMedSvarsText(svarsText)
    }

    def taBortFragaMedExternReferens(String externReferens) {
        response = restClient.delete(path: "questions/extern/${externReferens}")
    }

    def taBortFragaMedInternReferens(String internReferens) {
        response = restClient.delete(path: "questions/${internReferens}")
    }

    def taBortFragaMedFrageText(String frageText) {
        response = restClient.delete(path: "questions/frageText/${frageText}")
    }

    def taBortFragaMedSvarsText(String svarsText) {
        response = restClient.delete(path: "questions/svarsText/${svarsText}")
    }

    def taBortFragorForEnhet(String enhetsId) {
		System.out.println("tar bort alla frågor för enhet: "+ enhetsId + "..")
        response = restClient.delete(path: "questions/enhet/${enhetsId}")
    }

    def taBortAllaFragor() {
        response = restClient.delete(path: "questions/")
    }
}
