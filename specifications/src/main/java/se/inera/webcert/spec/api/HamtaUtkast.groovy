package se.inera.webcert.spec.api

import se.inera.webcert.spec.util.RestClientFixture
import se.inera.webcert.spec.util.WebcertRestUtils

import static groovyx.net.http.ContentType.JSON

public class HamtaUtkast extends RestClientFixture {

    String intygsId
    String intygsTyp
    
    String hsaId = "SE4815162344-1B01"
    String enhetId = "SE4815162344-1A02"

    def response

    def execute() {
        def restClient = createRestClient(baseUrl)
        WebcertRestUtils.login(restClient, hsaId, enhetId)

        response = restClient.get(
                path: "moduleapi/utkast/${intygsTyp}/${intygsId}",
                requestContentType: JSON
        )
    }

    public boolean utkastHamtat() {
        response.success
    }

    long version() {
        response.data.version
    }

}
