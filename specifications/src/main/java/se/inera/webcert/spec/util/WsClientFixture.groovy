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

class WsClientFixture extends se.inera.certificate.spec.util.WsClientFixture {

	private final static String LOGICAL_ADDRESS = "5565594230"

    public WsClientFixture() {
        super(LOGICAL_ADDRESS)
    }
    
    public WsClientFixture(String address) {
        super(address)
    }
    
    String baseUrl = System.getProperty("webcert.baseUrl")

}

