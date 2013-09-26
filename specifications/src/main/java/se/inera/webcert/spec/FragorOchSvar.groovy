package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

/**
 *
 * @author andreaskaltenbach
 */
class FragorOchSvar extends RestClientFixture {

    String internReferens
    String externReferens
    def fragaSvar

    public void execute() {
        def restClient = new RESTClient(baseUrl)

        if (internReferens)
            fragaSvar = restClient.get(path: "questions/${internReferens}").data

        if (externReferens)
            fragaSvar = restClient.get(path: "questions/extern/${externReferens}").data
    }

    public boolean finns() {
        return fragaSvar != null
    }

    def fraga() {
        fragaSvar.frageText
    }

    def svar() {
        fragaSvar.svarsText
    }
}
