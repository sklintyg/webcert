package se.inera.webcert.spec.api

import se.inera.webcert.spec.util.RestClientFixture

/**
 * @author pehra
 */
class SvarSkickadTillFk extends RestClientFixture implements GroovyObject {

    def svarJson

    public String svar() {
        svarJson.svar.meddelandeText[0]
    }

    public void execute() {
        def restClient = createRestClient("${baseUrl}services/")
        svarJson = restClient.get(path: "fk-stub/svar/").data
    }
}
