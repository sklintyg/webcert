package se.inera.intyg.common.specifications.spec

import se.inera.intyg.common.specifications.spec.util.RestClientFixture

public class TsIntygFinnsIStub extends RestClientFixture {

    String id;
    def responseData = null;

    private String url = System.getProperty("certificate.baseUrl")
    
    def execute() {
        def restClient = createRestClient("${url}")
        def response = restClient.get(path: 'ts-certificate-stub/certificates')
        responseData = response.data;
    }

    boolean exists(){
        return responseData[id] != null;
    }
}
