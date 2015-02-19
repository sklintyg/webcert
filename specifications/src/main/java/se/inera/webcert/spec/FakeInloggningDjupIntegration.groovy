package se.inera.webcert.spec
import se.inera.webcert.spec.util.RestClientFixture
import static groovyx.net.http.ContentType.URLENC

class FakeInloggningDjupIntegration extends RestClientFixture {
    def resp

    public void execute() {
        def restClient = createRestClient("http://localhost:9088/")
        def postBody = 'userJsonDisplay={"fornamn" : "Ivar", "efternamn" : "Integration", "hsaId" : "SE4815162344-1B01", "enhetId" : "SE4815162344-1A02", "lakare" : true,"forskrivarKod": "2481632"}'
        resp = restClient.post(path: "fake", requestContentType: URLENC, body: postBody)
    }
    
    public String status() {
        if (resp.success) {
            "OK" + " " + header
        } else {
            "FAIL"
        }
    }

}
