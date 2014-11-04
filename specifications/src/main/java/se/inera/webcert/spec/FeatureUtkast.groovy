package se.inera.webcert.spec

import groovyx.net.http.HttpResponseException
import se.inera.webcert.spec.util.RestClientFixture

class FeatureUtkast extends RestClientFixture{

    boolean ex
    int statusCode

    def execute() {
        ex = false
        def client = createRestClient("http://localhost:9088/")
        def headers = new HashMap<String,String>()
        headers.put("Cookie","JSESSIONID="+Browser.getJSession())
        try {
            client.get(path: "/api/utkast/")
            return false
        }
        catch(HttpResponseException e) {
            statusCode = e.statusCode
            ex = true
        }
    }

    def avstangd() {
        ex
    }

    def statusCode() {
        statusCode
    }

}
