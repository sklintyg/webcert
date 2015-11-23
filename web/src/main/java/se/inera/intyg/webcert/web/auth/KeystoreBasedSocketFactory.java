package se.inera.intyg.webcert.web.auth;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ServiceConfigurationError;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.saml.key.JKSKeyManager;

/**
 * @author andreaskaltenbach
 */
public class KeystoreBasedSocketFactory implements SecureProtocolSocketFactory {

    private static final Logger LOG = LoggerFactory.getLogger(KeystoreBasedSocketFactory.class);

    private static final String TLS = "TLS";

    private SSLContext sslContext;

    public KeystoreBasedSocketFactory(Resource trustStoreFile, String trustStorePassword) {

        JKSKeyManager keyManager = new JKSKeyManager(trustStoreFile, trustStorePassword, null, null);
        KeyStore trustStore = keyManager.getKeyStore();

        try {
            sslContext = createSSLContext(trustStore);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | UnrecoverableKeyException e) {
            String message = "Failed to create SSL context";
            LOG.error(message, e);
            throw new ServiceConfigurationError(message, e);
        }
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException {
        return sslContext.getSocketFactory().createSocket(host, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort,
            HttpConnectionParams params) throws IOException {

        int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            return createSocket(host, port, localAddress, localPort);
        } else {
            return new SSLProtocolSocketFactory().createSocket(host, port, localAddress, localPort, params);
        }
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return sslContext.getSocketFactory().createSocket(host, port);
    }

    private static SSLContext createSSLContext(final KeyStore truststore) throws NoSuchAlgorithmException,
            KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(null, null);
        KeyManager[] keymanagers = kmfactory.getKeyManagers();
        TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(truststore);
        TrustManager[] trustmanagers = tmfactory.getTrustManagers();

        SSLContext sslcontext = SSLContext.getInstance(TLS);
        sslcontext.init(keymanagers, trustmanagers, null);
        return sslcontext;
    }
}
