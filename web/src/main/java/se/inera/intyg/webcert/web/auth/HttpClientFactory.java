package se.inera.intyg.webcert.web.auth;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author andreaskaltenbach
 */
public class HttpClientFactory {

    private static final int HTTPS_PORT = 443;

    @Value("${sakerhetstjanst.saml.truststore.file}")
    private org.springframework.core.io.Resource trustStoreFile;

    @Value("${sakerhetstjanst.saml.truststore.password}")
    private String trustStorePassword;

    public HttpClient createInstance() {
        HttpClient httpClient = new HttpClient();

        ProtocolSocketFactory factory = new KeystoreBasedSocketFactory(trustStoreFile, trustStorePassword);
        Protocol protocol = new Protocol("https", factory, HTTPS_PORT);
        Protocol.registerProtocol("https", protocol);

        return httpClient;
    }
}
