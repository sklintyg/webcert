package se.inera.webcert.spec.pp_terms

import se.inera.certificate.spec.Browser
import se.inera.webcert.spec.util.RestClientFixture

/**
 * Created by eriklupander on 2015-08-19.
 */
class LaggTillGodkannande extends RestClientFixture {

    public LaggTillGodkannande() {
        super()
    }

    def laggTillGodkannande(String hsaId) {
        def restClient = createRestClient("${baseUrl}services/")
        def response = restClient.put(
                path: "anvandare/godkannavtal/${hsaId}"
        )
    }

}
