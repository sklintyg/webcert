package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture
/**
 * @author andreaskaltenbach
 */
public class TaBortVardgivare extends RestClientFixture {

    def vardgivarid

    public void execute() {
        def restClient = new RESTClient(baseUrl)
        restClient.delete(
                path: "hsa-api/vardgivare/$vardgivarid"
        )
    }

}
