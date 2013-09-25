package se.inera.webcert.spec

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
import org.springframework.core.io.ClassPathResource
import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

/**
 * @author johannesc
 */
public class Hsaenhet extends RestClientFixture{

    String vardgivarid;
    String hsaId;
    String name;
    String email;

    public void execute() {
        def restClient = new RESTClient(baseUrl)
        //def restClient = new RESTClient('http://localhost:9088/services/questions/')
            restClient.post(
                    path: 'hsa-api/enhet',
                    body:  questionJson(),
                    requestContentType: JSON
            )
    }

    private questionJson() {
        JsonOutput.toJson(this)
    }

}
