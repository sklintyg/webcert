package se.inera.webcert.spec

import static groovyx.net.http.ContentType.JSON
import se.inera.webcert.spec.util.RestClientFixture
import se.inera.webcert.spec.util.WebcertRestUtils

public class HanteraFragaSvar extends RestClientFixture {

    String frageId
    String intygsTyp
    
    String hsaId = "SE4815162344-1B01"
    String enhetId = "SE4815162344-1A02"
    boolean hanterad = true
        
    def execute() {
        String operation = hanterad ? "stang" : "oppna"
        def restClient = createRestClient(baseUrl)
        WebcertRestUtils.login(restClient, hsaId, enhetId)
        def response = restClient.get(
                path: "moduleapi/fragasvar/${intygsTyp}/${frageId}/${operation}"
        )
        assert response.status == 200
    }
}
