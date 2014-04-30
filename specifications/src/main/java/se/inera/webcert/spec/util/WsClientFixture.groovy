package se.inera.webcert.spec.util

import org.apache.cxf.configuration.jsse.TLSClientParameters
import org.apache.cxf.configuration.security.FiltersType
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.frontend.ClientProxyFactoryBean
import org.apache.cxf.jaxws.JaxWsClientFactoryBean
import org.apache.cxf.message.Message
import org.apache.cxf.transport.http.HTTPConduit
import org.w3.wsaddressing10.AttributedURIType
import se.inera.certificate.integration.json.CustomObjectMapper
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum

import javax.net.ssl.*
import java.security.KeyStore
import java.security.cert.X509Certificate

class WsClientFixture {

	final static String LOGICAL_ADDRESS = "5565594230"

	private CustomObjectMapper jsonMapper = new CustomObjectMapper();
	protected AttributedURIType logicalAddress = new AttributedURIType()
	
	public WsClientFixture() {
		this(LOGICAL_ADDRESS)
	}
	
	public WsClientFixture(String address) {
		logicalAddress.setValue(address)
	}
	
	def asJson(def object) {
		StringWriter sw = new StringWriter()
		jsonMapper.writeValue(sw, object)
		return sw.toString()
	}
	
	def asErrorMessage(String s) {
		throw new Exception("message:<<${s.replace(System.getProperty('line.separator'), ' ')}>>")
	}
	
    static String baseUrl = System.getProperty("webcert.baseUrl")

	def setEndpoint(def responder, String serviceName, String url = baseUrl + serviceName) {
		if (!url) url = baseUrl + serviceName
		Client client = ClientProxy.getClient(responder)
		client.getRequestContext().put(Message.ENDPOINT_ADDRESS, url)
	}
	
	def createClient(def responderInterface, String url, boolean ntjpClientAuthentication = false) {
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean(new JaxWsClientFactoryBean());
		factory.setServiceClass( responderInterface );
		factory.setAddress(url);
		def responder = factory.create();
		if (url.startsWith("https:")) {
			setupSSLCertificates(responder, ntjpClientAuthentication)
		}
		return responder
	}
	
	def resultAsString(response) {
		if (response) {
	        switch (response.result.resultCode) {
	            case ResultCodeEnum.OK:
	                return response.result.resultCode.toString()
	            case ResultCodeEnum.INFO:
	                return "[${response.result.resultCode.toString()}] - ${response.result.infoText}"
	            default:
					return "[${response.result.resultCode.toString()}] - ${response.result.errorText}"
	        }
		}
		else null
	}

	def setupSSLCertificates(def responder, boolean ntjpClientAuthentication) {
		Client client = ClientProxy.getClient(responder)
		HTTPConduit httpConduit = (HTTPConduit)client.getConduit();
		TLSClientParameters tlsParams = new TLSClientParameters();
		tlsParams.setDisableCNCheck(true);

        if (ntjpClientAuthentication) {
    		KeyStore trustStore = KeyStore.getInstance("JKS");
    		String trustpass = "password";//provide trust pass
    
    		trustStore.load(se.inera.certificate.spec.util.WsClientFixture.class.getResourceAsStream("/truststore-ntjp.jks"), trustpass.toCharArray());
    		TrustManagerFactory trustFactory =
    				TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    		trustFactory.init(trustStore);
    		TrustManager[] tm = trustFactory.getTrustManagers();
    		tlsParams.setTrustManagers(tm);
    
    		KeyStore certStore = KeyStore.getInstance("PKCS12");
    		String certPass = "psgccsSeBZ"
    		certStore.load(se.inera.certificate.spec.util.WsClientFixture.class.getResourceAsStream("/hsaws-user.ifv.sjunet.org_auth.p12"), certPass.toCharArray());
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
        filter.getInclude().add(".*_WITH_NULL_.*");
        filter.getInclude().add(".*_DH_anon_.*");
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

