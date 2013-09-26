package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

/**
 *
 * @author andreaskaltenbach
 */
class FragorOchSvar extends RestClientFixture {

    String externReferens;
    def fragaSvar

    public void execute() {
        def restClient = new RESTClient(baseUrl)
        fragaSvar = restClient.get(path: "questions/${externReferens}")
    }

    public boolean finns() {
        return fragaSvar.data != null
    }
}
