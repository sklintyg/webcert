package se.inera.auth.common;

import org.apache.cxf.helpers.XMLUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.saml.SAMLCredential;
import org.w3c.dom.Document;
import se.inera.auth.WebCertUserDetailsService;
import se.inera.auth.eleg.ElegWebCertUserDetailsService;

import javax.xml.transform.stream.StreamSource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests so the unified userdetails service forwards the SAMLCredential to the correct underlying userDetailsService
 * depending on authContextClassRef.
 *
 * That may have to change if we for example have privatläkare using BankID on card which also could be a TLSClient. In
 * that case, the UnifiedUserDetailsService will have to be rewritten to introspect some other attribute on the SAMLCredential
 * in order to route the request correctly.
 *
 * Created by eriklupander on 2015-08-20.
 */
@RunWith(MockitoJUnitRunner.class)
public class UnifiedUserDetailsServiceTest {

    static Assertion assertionPrivatlakare;
    static Assertion assertionLandstingslakare;
    static Assertion assertionUnknownAuthCtx;

    @Mock
    ElegWebCertUserDetailsService elegWebCertUserDetailsService;

    @Mock
    WebCertUserDetailsService webCertUserDetailsService;

    @InjectMocks
    UnifiedUserDetailsService unifiedUserDetailsService;

    @BeforeClass
    public static void readSamlAssertions() throws Exception {
        DefaultBootstrap.bootstrap();

        Document doc = (Document) XMLUtils.fromSource(new StreamSource(new ClassPathResource(
                "CGIElegAssertiontest/sample-saml2-response.xml").getInputStream()));
        org.w3c.dom.Element documentElement = doc.getDocumentElement();

        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(documentElement);
        XMLObject responseXmlObj = unmarshaller.unmarshall(documentElement);
        Response response = (Response) responseXmlObj;
        assertionPrivatlakare = response.getAssertions().get(0);


        doc = (Document) XMLUtils.fromSource(new StreamSource(new ClassPathResource(
                "WebCertUserDetailsServiceTest/saml-assertion-with-title-code-lakare.xml").getInputStream()));

        documentElement = doc.getDocumentElement();

        unmarshallerFactory = Configuration.getUnmarshallerFactory();
        unmarshaller = unmarshallerFactory.getUnmarshaller(documentElement);
        responseXmlObj = unmarshaller.unmarshall(documentElement);

        assertionLandstingslakare = (Assertion) responseXmlObj;

        doc = (Document) XMLUtils.fromSource(new StreamSource(new ClassPathResource(
                "CGIElegAssertiontest/sample-saml2-response-unknown-auth-ctx.xml").getInputStream()));
        documentElement = doc.getDocumentElement();

        unmarshallerFactory = Configuration.getUnmarshallerFactory();
        unmarshaller = unmarshallerFactory.getUnmarshaller(documentElement);
        responseXmlObj = unmarshaller.unmarshall(documentElement);
        response = (Response) responseXmlObj;
        assertionUnknownAuthCtx = response.getAssertions().get(0);
    }

    @Test
    public void testSoftwarePKI() {
        unifiedUserDetailsService.loadUserBySAML(buildPrivatlakareSamlCredential());
        verify(elegWebCertUserDetailsService, times(1)).loadUserBySAML(any(SAMLCredential.class));
    }

    @Test
    public void testTLSClient() {
        unifiedUserDetailsService.loadUserBySAML(buildLandstingslakareSamlCredential());
        verify(webCertUserDetailsService, times(1)).loadUserBySAML(any(SAMLCredential.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownAuthContext() {
        unifiedUserDetailsService.loadUserBySAML(buildUnknownSamlCredential());
        verify(webCertUserDetailsService, times(0)).loadUserBySAML(any(SAMLCredential.class));
    }


    private SAMLCredential buildPrivatlakareSamlCredential() {
        return new SAMLCredential(mock(NameID.class), assertionPrivatlakare, "", "");
    }
    private SAMLCredential buildLandstingslakareSamlCredential() {
        return new SAMLCredential(mock(NameID.class), assertionLandstingslakare, "", "");
    }
    private SAMLCredential buildUnknownSamlCredential() {
        return new SAMLCredential(mock(NameID.class), assertionUnknownAuthCtx, "", "");
    }
}
