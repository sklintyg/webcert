package se.inera.webcert.spec.pp_terms

import se.inera.certificate.spec.Browser
import se.inera.webcert.spec.util.RestClientFixture

/**
 * Created by eriklupander on 2015-08-19.
 */
class TaBortGodkannande extends RestClientFixture {

    private def restClient = createRestClient("${baseUrl}api/")

    def response

    public TaBortGodkannande() {
        super()
    }

    public String respons(){
        return response.status;
    }

    def taBortGodkannande() {
        response = restClient.delete(path: "anvandare/privatlakaravtal", headers: ["Cookie":"JSESSIONID="+Browser.getJSession()])
    }

}
