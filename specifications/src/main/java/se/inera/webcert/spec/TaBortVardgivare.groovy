package se.inera.webcert.spec

import se.inera.webcert.spec.util.RestClientFixture

/**
 * @author andreaskaltenbach
 */
public class TaBortVardgivare extends RestClientFixture {

    def vardgivarid

    public void execute() {
        def restClient = createRestClient(baseUrl)
        restClient.delete(path: "hsa-api/vardgivare/$vardgivarid")
    }
}
