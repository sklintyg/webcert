package se.inera.webcert.spec.util

import groovyx.net.http.RESTClient
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory

import se.inera.certificate.spec.util.ssl.TestSSLSocketFactory
import java.security.KeyStore

/**
 *
 * @author andreaskaltenbach
 */
class RestClientFixture {

    static String baseUrl = System.getProperty("webcert.baseUrl")
    static String logSenderBaseUrl = System.getProperty("logsender.baseUrl")

    /**
     * Creates a RestClient which accepts all server certificates
     * @return
     */
    static def createRestClient(String url) {
        def restClient = new RESTClient(url)

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        SSLSocketFactory sf = new TestSSLSocketFactory(trustStore);
        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        restClient.client.connectionManager.schemeRegistry.register(new Scheme("https", sf, 443))

        restClient
    }
}
