package se.inera.certificate.spec.util

import java.security.KeyStore
import java.security.cert.X509Certificate

import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

import org.apache.cxf.configuration.jsse.TLSClientParameters
import org.apache.cxf.configuration.security.FiltersType
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.frontend.ClientProxyFactoryBean
import org.apache.cxf.jaxws.JaxWsClientFactoryBean
import org.apache.cxf.message.Message
import org.apache.cxf.transport.http.HTTPConduit
import org.w3.wsaddressing10.AttributedURIType

import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType
import se.inera.certificate.integration.json.CustomObjectMapper
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum

class WsClientFixture {

	private final static String LOGICAL_ADDRESS = "FKORG"

    String baseUrl = System.getProperty("certificate.baseUrl")
    
    boolean nyaKontraktet = false
    
	private CustomObjectMapper jsonMapper = new CustomObjectMapper();
	protected AttributedURIType logicalAddress = new AttributedURIType()

	public WsClientFixture() {
		this(LOGICAL_ADDRESS)
	}

    public WsClientFixture(String address) {
        logicalAddress.setValue(address)
        init()
    }

    public WsClientFixture(String address, String baseUrl) {
        logicalAddress.setValue(address)
        this.baseUrl = baseUrl
        init()
    }

    public void init() {}
    
	def asJson(def object) {
		StringWriter sw = new StringWriter()
		jsonMapper.writeValue(sw, object)
		return sw.toString()
	}

	def asErrorMessage(String s) {
		throw new Exception("message:<<${s.replace(System.getProperty('line.separator'), ' ')}>>")
	}

	def setEndpoint(def responder, String serviceName, String url = baseUrl + serviceName) {
		if (!url) url = baseUrl + serviceName
		Client client = ClientProxy.getClient(responder)
		client.getRequestContext().put(Message.ENDPOINT_ADDRESS, url)
	}

	def createClient(def responderInterface, String url) {
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean(new JaxWsClientFactoryBean());
		factory.setServiceClass( responderInterface );
		factory.setAddress(url);
		def responder = factory.create();
		if (url.startsWith("https:")) {
			setupSSLCertificates(responder)
		}
		return responder
	}

	def resultAsString(response) {
        String result = null
		if (response) {
            if (nyaKontraktet) {
                switch (response.result.resultCode) {
                    case ResultCodeType.OK:
                        result = response.result.resultCode.toString()
                        break
                    case ResultCodeType.INFO:
                        result = "[${response.result.resultCode.toString()}] - ${response.result.resultText}"
                        break
                    case ResultCodeType.ERROR:
                        result = "[${response.result.errorId.toString()}] - ${response.result.resultText}"
                        break
                }
            } else {
    	        switch (response.result.resultCode) {
    	            case ResultCodeEnum.OK:
    	                result = response.result.resultCode.toString()
                        break
    	            case ResultCodeEnum.INFO:
    	                result = "[${response.result.resultCode.toString()}] - ${response.result.infoText}"
                        break
                    case ResultCodeEnum.ERROR:
    					result = "[${response.result.errorId.toString()}] - ${response.result.errorText}"
                        break
    	        }
            }
		}
		return result
	}

	def setupSSLCertificates(def responder) {
        boolean ntjpClientAuthentication = Boolean.getBoolean("service.ntjpClientAuthentication")
		Client client = ClientProxy.getClient(responder)
		HTTPConduit httpConduit = (HTTPConduit)client.getConduit();
		TLSClientParameters tlsParams = new TLSClientParameters();
		tlsParams.setDisableCNCheck(true);

        if (ntjpClientAuthentication) {
    		KeyStore trustStore = KeyStore.getInstance("JKS");
    		String trustpass = "password";//provide trust pass

    		trustStore.load(WsClientFixture.class.getResourceAsStream("/truststore-ntjp.jks"), trustpass.toCharArray());
    		TrustManagerFactory trustFactory =
    				TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    		trustFactory.init(trustStore);
    		TrustManager[] tm = trustFactory.getTrustManagers();
    		tlsParams.setTrustManagers(tm);

    		KeyStore certStore = KeyStore.getInstance("PKCS12");
            String certFile = System.getProperty("ws.certificate.file");
            String certPass = System.getProperty("ws.certificate.password");
    		certStore.load(WsClientFixture.class.getResourceAsStream(certFile), certPass.toCharArray());
    		KeyManagerFactory keyFactory =
    				KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    		keyFactory.init(certStore, certPass.toCharArray());
    		KeyManager[] km = keyFactory.getKeyManagers();
    		tlsParams.setKeyManagers(km);
        } else {
        TrustManager[] tm = [new TrustAllX509TrustManager()]
        tlsParams.setTrustManagers(tm);
    }
		FiltersType filter = new FiltersType();
		filter.getInclude().add(".*_EXPORT_.*");
		filter.getInclude().add(".*_EXPORT1024_.*");
		filter.getInclude().add(".*_WITH_DES_.*");
        filter.getInclude().add(".*_WITH_AES_.*");
		filter.getInclude().add(".*_WITH_NULL_.*");
		filter.getExclude().add(".*_DH_anon_.*");
		tlsParams.setCipherSuitesFilter(filter);//set all the needed include filters.

		httpConduit.setTlsClientParameters(tlsParams);
	}

    /**
     * This class allow any X509 certificates to be used to authenticate the remote side of a secure socket, including
     * self-signed certificates.
     */
    public class TrustAllX509TrustManager implements X509TrustManager {

        /** Empty array of certificate authority certificates. */
        private static final X509Certificate[] acceptedIssuers = [];

        /**
         * Always trust for client SSL chain peer certificate chain with any authType authentication types.
         *
         * @param chain the peer certificate chain.
         * @param authType the authentication type based on the client certificate.
         */
        public void checkClientTrusted( X509Certificate[] chain, String authType ) {
        }

        /**
         * Always trust for server SSL chain peer certificate chain with any authType exchange algorithm types.
         *
         * @param chain the peer certificate chain.
         * @param authType the key exchange algorithm used.
         */
        public void checkServerTrusted( X509Certificate[] chain, String authType ) {
        }

        /**
         * Return an empty array of certificate authority certificates which are trusted for authenticating peers.
         *
         * @return a empty array of issuer certificates.
         */
        public X509Certificate[] getAcceptedIssuers() {
            return ( acceptedIssuers );
        }
    }

}
