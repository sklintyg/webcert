package se.inera.webcert.spec

import groovyx.net.http.RESTClient
import se.inera.webcert.spec.util.RestClientFixture

/**
 * Created by pehr on 9/23/13.
 */
public class TaBortFraga extends RestClientFixture {

    String internReferens
    String externReferens

    public void execute() {
        def restClient = new RESTClient(baseUrl)

        if (internReferens)
            restClient.delete(path: "questions/${internReferens}")

        if (externReferens)
            restClient.delete(path: "questions/extern/${externReferens}")
    }
}

