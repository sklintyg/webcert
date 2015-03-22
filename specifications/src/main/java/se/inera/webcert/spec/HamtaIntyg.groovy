package se.inera.webcert.spec

import static groovyx.net.http.ContentType.JSON
import se.inera.webcert.spec.util.RestClientFixture
import se.inera.webcert.spec.util.WebcertRestUtils

public class HamtaIntyg extends RestClientFixture {

    String intygsId
    String intygsTyp
    
    String hsaId = "SE4815162344-1B01"
    String enhetId = "SE4815162344-1A02"
    
    def execute() {
        def restClient = createRestClient(baseUrl)
        WebcertRestUtils.login(restClient, hsaId, enhetId)
        def response = restClient.get(
                path: "moduleapi/intyg/${intygsTyp}/${intygsId}",
                requestContentType: JSON
        )
    }
}
