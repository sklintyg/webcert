package se.inera.webcert.spec.api

import groovyx.net.http.HttpResponseException
import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

class FeatureModuler extends RestClientFixture{

    String typ
    String intygStatus
    String verb
    String metod

    boolean ex
    int status

    def execute() {
        status = 0;
        ex = false

        def client = createRestClient(baseUrl)

        def headers = new HashMap<String,String>()
        headers.put("Cookie","JSESSIONID="+Browser.getJSession())

        def intygsId = "webcert-fitnesse-features-1"
        if (intygStatus.equals("intyg")) {
            if (typ.equals("fk7263"))
                intygsId = "intyg-fit-1"
            else if (typ.equals("ts-bas"))
                intygsId = "intyg-fit-4"
            else if (typ.equals("ts-diabetes"))
                intygsId = "intyg-fit-5"
        }

        def path = "/moduleapi/"+intygStatus+"/"+typ+"/"+intygsId+(metod?"/"+metod:"");

        try {
            if (verb.equals("PUT"))
                client.put(path: path, requestContentType: JSON, headers: headers, body: "{}")
            else if (verb.equals("POST"))
                client.post(path: path, requestContentType: JSON, headers: headers, body: "{}")
            else if (verb.equals("DELETE"))
                client.delete(path: path, headers: headers)
            else
                client.get(path: path, headers: headers)
        }
        catch(HttpResponseException e) {
            status = e.statusCode
            if (status == 403) {
                ex = true
            }
        }
    }

    def avstangd() {
        ex
    }

    def status() {
        status
    }

}
