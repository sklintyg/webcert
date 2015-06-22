package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

class TaBortFraga extends RestClientFixture {

    private def restClient = createRestClient("${baseUrl}services/")
    
    String internReferens
    String externReferens
    String frageText
    String svarsText
    
    // Allow use both as DecisionTable and Script fixture
    def execute() {
        if (internReferens) taBortFragaMedInternReferens(internReferens)
        if (externReferens) taBortFragaMedExternReferens(externReferens)
        if (frageText) taBortFragaMedFrageText(frageText)
        if (svarsText) taBortFragaMedSvarsText(svarsText)
    }

    def taBortFragaMedExternReferens(String externReferens) {
        restClient.delete(path: "questions/extern/${externReferens}")
    }

    def taBortFragaMedInternReferens(String internReferens) {
        restClient.delete(path: "questions/${internReferens}")
    }

    def taBortFragaMedFrageText(String frageText) {
        restClient.delete(path: "questions/frageText/${frageText}")
    }

    def taBortFragaMedSvarsText(String svarsText) {
        restClient.delete(path: "questions/svarsText/${svarsText}")
    }

    def taBortFragorForEnhet(String enhetsId) {
        restClient.delete(path: "questions/enhet/${enhetsId}")
    }

    def taBortAllaFragor() {
        restClient.delete(path: "questions/")
    }
}
