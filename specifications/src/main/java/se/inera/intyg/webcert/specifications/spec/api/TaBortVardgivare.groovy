package se.inera.intyg.webcert.specifications.spec.api

import se.inera.intyg.webcert.specifications.spec.util.RestClientFixture

/**
 * @author andreaskaltenbach
 */
public class TaBortVardgivare extends RestClientFixture {

    def vardgivarid

    public void execute() {
        def restClient = createRestClient("${baseUrl}services/")
        restClient.delete(path: "hsa-api/vardgivare/$vardgivarid")
    }
}
