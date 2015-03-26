package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

class TaBortFraga extends RestClientFixture {

    String internReferens
    String externReferens
    String frageText
    String svarsText
    
    def execute() {
        def restClient = createRestClient("${baseUrl}services/")
        if (internReferens) restClient.delete(path: "questions/${internReferens}")
        if (externReferens) restClient.delete(path: "questions/extern/${externReferens}")
        if (frageText) restClient.delete(path: "questions/frageText/${frageText}")
        if (svarsText) restClient.delete(path: "questions/svarsText/${svarsText}")
    }
}
