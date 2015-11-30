package se.inera.certificate.spec.util

import groovyx.net.http.RESTClient

/**
 *
 * @author andreaskaltenbach
 */
class RestClientFixture {

    String baseUrl = System.getProperty("certificate.baseUrl") + "resources/"
    
    /**
     * Creates a RestClient which accepts all server certificates
     * @return
     */
    def createRestClient() {
        createRestClient(baseUrl)
    }

    /**
     * Creates a RestClient which accepts all server certificates
     * @return
     */
    static def createRestClient(String url) {
        def restClient = new RESTClient(url)
        restClient.ignoreSSLIssues()
        restClient
    }
}
