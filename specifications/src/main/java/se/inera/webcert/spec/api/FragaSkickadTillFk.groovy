package se.inera.webcert.spec.api

import se.inera.webcert.spec.util.RestClientFixture

class FragaSkickadTillFk extends RestClientFixture {

    def fragaJson

    public String fraga() {
        fragaJson.fraga.meddelandeText[0]
    }

    public String internReferens() {
        fragaJson.vardReferensId[0]
    }

    public String intygsId() {
        fragaJson.lakarutlatande.lakarutlatandeId[0]
    }

    public void execute() {
        def restClient = createRestClient("${baseUrl}services/")
        fragaJson = restClient.get(path: "fk-stub/fragor/").data
    }
}
