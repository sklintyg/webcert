package se.inera.webcert.spec

import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

/**
 * @author johannesc
 */
public class HsaRensaCache extends RestClientFixture {

    public void execute() {
        System.out.println("HsaRensaCache.execute()!!!!!!")
        def restClient = new RESTClient(baseUrl)
        restClient.post(
                path: 'hsa-api/rensa-cache',
                body: '',
                requestContentType: JSON
        )
    }

}
