package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture
/**
 *
 * @author pehra
 */
class RensaFragorSvarTillFk extends RestClientFixture implements  GroovyObject {

    public void rensaFraga() {
        def restClient = new RESTClient(baseUrl)

        restClient.delete(path: "fk-stub/fragor/")
    }

    public void rensaSvar() {
        def restClient = new RESTClient(baseUrl)

        restClient.delete(path: "fk-stub/svar/")
    }


}
