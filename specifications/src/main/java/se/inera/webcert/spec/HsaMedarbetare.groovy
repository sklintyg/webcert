package se.inera.webcert.spec

import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

/**
 * @author johannesc
 */
public class HsaMedarbetare extends RestClientFixture {

    String hsaId
    String name
    String email
    String[] medarbetaruppdrag


    public void execute() {
        System.out.println("HsaMedarbetare.execute()!!!!!!")
        def restClient = new RESTClient(baseUrl)
        restClient.post(
                path: 'hsa-api/medarbetaruppdrag',
                body: questionJson(),
                requestContentType: JSON
        )
    }

    private questionJson() {
        JsonOutput.toJson(this)
    }

}
