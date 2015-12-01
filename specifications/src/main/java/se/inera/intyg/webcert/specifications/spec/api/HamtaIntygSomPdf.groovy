package se.inera.intyg.webcert.specifications.spec.api

import static groovyx.net.http.ContentType.JSON
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture
import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils

public class HamtaIntygSomPdf extends RestClientFixture {

    String intygsId
    String intygsTyp
    
    String hsaId = "SE4815162344-1B01"
    String enhetId = "SE4815162344-1A02"
    
    def execute() {
        def restClient = createRestClient(baseUrl)
        WebcertRestUtils.login(restClient, hsaId, enhetId)
        def response = restClient.get(
                path: "moduleapi/intyg/${intygsTyp}/${intygsId}/pdf",
                requestContentType: JSON
        )
    }
}
