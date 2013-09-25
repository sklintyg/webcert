package se.inera.webcert.spec

import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

/**
 * @author johannesc
 */
public class Medarbetare extends RestClientFixture{

    String hsaId
    String name
    String email
    String[] medarbetaruppdrag


    public void execute() {
        def restClient = new RESTClient(baseUrl)
        //def restClient = new RESTClient('http://localhost:9088/services/questions/')
            restClient.post(
                    path: 'hsa-api/medarbetaruppdrag',
                    body:  questionJson(),
                    requestContentType: JSON
            )
    }

    private questionJson() {
        JsonOutput.toJson(this)
    }

}
