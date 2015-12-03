package se.inera.intyg.webcert.specifications.spec.api

import groovyx.net.http.RESTClient
import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

/**
 * @author andreaskaltenbach
 */
class FragorOchSvar extends RestClientFixture {

    String internReferens
    String externReferens
    def fragaSvar

    public void execute() {
        def restClient = createRestClient("${baseUrl}testability/")

        if (internReferens)
            fragaSvar = restClient.get(path: "questions/${internReferens}").data

        if (externReferens)
            fragaSvar = restClient.get(path: "questions/extern/${externReferens}").data
    }

    def finns() {
        return fragaSvar != null
    }

    def internId() {
        fragaSvar.internReferens
    }

    def fraga() {
        fragaSvar.frageText
    }

    def svar() {
        fragaSvar.svarsText
    }

    def status() {
        fragaSvar.status
    }

    def amne() {
        fragaSvar.amne
    }
}
