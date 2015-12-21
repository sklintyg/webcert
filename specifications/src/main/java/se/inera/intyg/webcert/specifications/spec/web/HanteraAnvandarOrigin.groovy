package se.inera.intyg.webcert.specifications.spec.web

import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

public class HanteraAnvandarOrigin extends RestClientFixture {

    def origin

    def restClient = createRestClient("${baseUrl}testability/")

    public HanteraAnvandarOrigin() {
        super()
    }

    def hamtaOrigin() {
        def response = restClient.get(
                path: "anvandare/origin"
        )
        origin = response;
    }

    def uppdateraOriginTill(String origin) {
        def response = restClient.put(
                path: "anvandare/origin/${origin}"
        )
    }

}
