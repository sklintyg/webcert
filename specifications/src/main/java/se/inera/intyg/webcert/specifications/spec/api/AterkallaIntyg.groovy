package se.inera.intyg.webcert.specifications.spec.api

import static groovyx.net.http.ContentType.JSON
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture
import se.inera.intyg.webcert.specifications.spec.util.WebcertRestUtils

public class AterkallaIntyg extends RestClientFixture {

    String intygsId
    String intygsTyp
    
    String hsaId = "SE4815162344-1B01"
    String enhetId = "SE4815162344-1A02"
    String meddelande = "ett meddelande"
    
    def execute() {
        def restClient = createRestClient(baseUrl)
        WebcertRestUtils.login(restClient, hsaId, enhetId)
        def response = restClient.post(
                path: "moduleapi/intyg/${intygsTyp}/${intygsId}/aterkalla",
                body: '{"revokeMessage": "' + meddelande + '"}',
                requestContentType: JSON
        )
    }
}
