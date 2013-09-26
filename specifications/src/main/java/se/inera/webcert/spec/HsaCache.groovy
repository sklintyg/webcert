package se.inera.webcert.spec

import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

import static groovyx.net.http.ContentType.JSON

/**
 * @author johannesc
 */
public class HsaCache extends RestClientFixture {

    public void rensa() {
        System.out.println("HsaCache.execute()!!!!!!")
        def restClient = new RESTClient(baseUrl)
        restClient.delete(
                path: 'hsa-api/rensa-cache'
        )
    }

}
