package se.inera.webcert.spec.pp_terms

import se.inera.webcert.spec.util.RestClientFixture

/**
 * Fixture to manipulate user agreements
 *
 * Created by marced on 21/10/15.
 */
class HanteraAnvandarAvtal extends RestClientFixture {

    def restClient = createRestClient("${baseUrl}testability/")

    public HanteraAnvandarAvtal() {
        super()
    }

    def laggTillGodkannandeForHsaid(String hsaId) {
        def response = restClient.put(
                path: "anvandare/godkannavtal/${hsaId}"
        )
    }

    def taBortGodkannandeForHsaid(String hsaId) {
        def response = restClient.put(
                path: "anvandare/avgodkannavtal/${hsaId}"
        )
    }
}
