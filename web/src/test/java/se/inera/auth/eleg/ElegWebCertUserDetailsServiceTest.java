package se.inera.auth.eleg;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.webcert.service.feature.WebcertFeatureService;
import se.inera.webcert.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.HsaId;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.PersonId;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.VardgivareType;

import javax.xml.transform.stream.StreamSource;
import java.util.HashSet;

/**
 * Created by eriklupander on 2015-06-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElegWebCertUserDetailsServiceTest {

    private static final String LOCAL_ENTITY_ID = "localEntityId";
    private static final String REMOTE_ENTITY_ID = "remoteEntityId";
    private static final String HSA_ID = "191212121212";
    private static final String PERSON_ID = "191212121212";
    private static Assertion assertionPrivatlakare;

    @Mock
    private PPService ppService;

    @Mock
    private WebcertFeatureService webcertFeatureService;

    @Mock
    private AvtalService avtalService;

    @Mock
    private ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;

    @Mock
    private ElegAuthenticationMethodResolver elegAuthenticationMethodResolver;

    @InjectMocks
    private ElegWebCertUserDetailsService testee;

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


    }

    @Test
    public void testSuccessfulLogin() {
        when(ppService.getPrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(buildHosPerson());
        when(ppService.validatePrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(true);
        when(webcertFeatureService.getActiveFeatures()).thenReturn(new HashSet<String>());
        when(avtalService.userHasApprovedLatestAvtal(anyString())).thenReturn(true);

        NameID nameId = mock(NameID.class);
        Object o = testee.loadUserBySAML(new SAMLCredential(nameId, assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
        assertNotNull(o);
    }

    // TODO tests for not OK avtal, not OK validate vs pp, not found in HSA etc.

    private HoSPersonType buildHosPerson() {
        HoSPersonType hoSPersonType = new HoSPersonType();
        HsaId hsaId = new HsaId();
        hsaId.setExtension(HSA_ID);
        hoSPersonType.setHsaId(hsaId);
        PersonId personId = new PersonId();
        personId.setExtension(PERSON_ID);
        hoSPersonType.setPersonId(personId);

        EnhetType vardEnhet = new EnhetType();
        vardEnhet.setEnhetsnamn("enhetsNamn");
        HsaId enhetsId = new HsaId();
        enhetsId.setExtension("enhetsId");
        vardEnhet.setEnhetsId(enhetsId);
        VardgivareType vardgivare = new VardgivareType();
        HsaId vardgivareId = new HsaId();
        enhetsId.setExtension("vardgivareId");
        vardgivare.setVardgivareId(vardgivareId);
        vardgivare.setVardgivarenamn("vardgivareName");
        vardEnhet.setVardgivare(vardgivare);
        hoSPersonType.setEnhet(vardEnhet);

        return hoSPersonType;

    }


}
