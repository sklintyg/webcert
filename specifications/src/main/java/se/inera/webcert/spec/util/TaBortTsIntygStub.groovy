package se.inera.webcert.spec.util

import se.inera.certificate.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

public class TaBortTsIntygStub extends RestClientFixture {

    public void execute(){
        def restClient = createRestClient("${intygsTjanstBaseUrl}")
        def response = restClient.delete(path: 'ts-certificate-stub/certificates')
        assert response.status == 204
    }
}
