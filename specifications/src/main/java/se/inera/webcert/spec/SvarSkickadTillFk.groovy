package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture
/**
 *
 * @author pehra
 */
class SvarSkickadTillFk extends RestClientFixture implements  GroovyObject {

    def svarJson

    public String svar(){
        svarJson.svar.meddelandeText[0]
    }

    public void execute() {
        def restClient = new RESTClient(baseUrl)


        svarJson = restClient.get(path: "fk-stub/svar/").data
    }


}
