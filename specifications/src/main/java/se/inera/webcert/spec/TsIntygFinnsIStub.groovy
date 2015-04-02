package se.inera.webcert.spec

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.core.io.ClassPathResource
import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class TsIntygFinnsIStub extends RestClientFixture {

    String id;
    def responseData = null;

    def execute() {
        def restClient = createRestClient("${intygsTjanstBaseUrl}")
        def response = restClient.get(path: 'ts-certificate-stub/certificates')
        responseData = response.data;
    }

    boolean exists(){
        return responseData[id] != null;
    }
}
