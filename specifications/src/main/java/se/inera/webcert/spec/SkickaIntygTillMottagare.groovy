package se.inera.webcert.spec

import static groovyx.net.http.ContentType.JSON
import se.inera.webcert.spec.util.RestClientFixture
import se.inera.webcert.spec.util.WebcertRestUtils

public class SkickaIntygTillMottagare extends RestClientFixture {

    String intygsId
    String intygsTyp
    
    String hsaId = "SE4815162344-1B01"
    String enhetId = "SE4815162344-1A02"
    String mottagare = "enMottagare"
    boolean samtycke = true
        
    def execute() {
        def restClient = createRestClient(baseUrl)
        WebcertRestUtils.login(restClient, hsaId, enhetId)
        def response = restClient.post(
                path: "moduleapi/intyg/${intygsTyp}/${intygsId}/skicka",
                body: '{"recipient":"' + mottagare + '","patientConsent":' + samtycke + '}',
                requestContentType: JSON
        )
    }
}
