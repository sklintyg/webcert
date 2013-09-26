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
public class HsaEnhet extends RestClientFixture {

    String vardgivarid;
    String hsaId;
    String name;
    String email;

    public void execute() {
        def restClient = new RESTClient(baseUrl)
        restClient.post(
                path: 'hsa-api/enheter',
                body: questionJson(),
                requestContentType: JSON
        )
    }

    private questionJson() {
        JsonOutput.toJson(this)
    }

}
