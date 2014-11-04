package se.inera.webcert.spec

import groovyx.net.http.HttpResponseException
import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

class FeatureModuleUtkast extends RestClientFixture{

    String typ
    String verb

    boolean ex
    int statusCode

    def execute() {
        ex = false
        def client = createRestClient("http://localhost:9088/")
        def headers = new HashMap<String,String>()
        headers.put("Cookie","JSESSIONID="+Browser.getJSession())
        try {
            if (verb.equals("PUT"))
                client.put(path: "/moduleapi/utkast/"+typ+"/webcert-fitnesse-features-1", requestContentType: JSON, headers: headers)
            else if (verb.equals("DELETE"))
                client.delete(path: "/moduleapi/utkast/"+typ+"/webcert-fitnesse-features-1", headers: headers)
            else
                client.get(path: "/moduleapi/utkast/"+typ+"/webcert-fitnesse-features-1", headers: headers)
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
