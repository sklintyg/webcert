package se.inera.intyg.common.specifications.spec

import se.inera.intyg.common.specifications.spec.util.RestClientFixture

public class TaBortTsIntygStub extends RestClientFixture {

    private String url = System.getProperty("certificate.baseUrl");
    
    public void execute(){
        def restClient = createRestClient("${url}")
        def response = restClient.delete(path: 'ts-certificate-stub/certificates')
        assert response.status == 204
    }
}
