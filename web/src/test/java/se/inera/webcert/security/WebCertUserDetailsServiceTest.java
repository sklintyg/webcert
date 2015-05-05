package se.inera.webcert.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.stream.StreamSource;

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
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.saml.SAMLCredential;
import org.w3c.dom.Document;

import se.inera.auth.WebCertUserDetailsService;
import se.inera.auth.exceptions.HsaServiceException;
import se.inera.auth.exceptions.MissingMedarbetaruppdragException;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType.HsaTitles;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType.SpecialityNames;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.hsa.services.HsaOrganizationsService;
import se.inera.webcert.hsa.services.HsaPersonService;
import se.inera.webcert.service.feature.WebcertFeatureService;
import se.inera.webcert.service.monitoring.MonitoringLogService;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class WebCertUserDetailsServiceTest {

    private static final String PERSONAL_HSA_ID = "TST5565594230-106J";
    private static final String ENHET_HSA_ID = "IFV1239877878-103H";
    private static final String HEAD_DOCTOR = "Överläkare";

    @InjectMocks
    private WebCertUserDetailsService userDetailsService = new WebCertUserDetailsService();

    @Mock
    private HsaOrganizationsService hsaOrganizationsService;

    @Mock
    private HsaPersonService hsaPersonService;
    
    @Mock
    private WebcertFeatureService webcertFeatureService;

    @Mock
    private MonitoringLogService monitoringLogService;

    private Vardgivare vardgivare;

    @BeforeClass
    public static void bootstrapOpenSaml() throws Exception {
        DefaultBootstrap.bootstrap();
    }

    @Test
    public void testPopulatingWebCertUser() throws Exception {

        setupCallToAuthorizedEnheterForHosPerson();
        setupCallToGetHsaPersonInfo();
        setupCallToWebcertFeatureService();

        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-code-lakare.xml");

        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertEquals(PERSONAL_HSA_ID, webCertUser.getHsaId());
        assertEquals("Markus Gran", webCertUser.getNamn());
        assertEquals(1, webCertUser.getVardgivare().size());
        assertEquals("vg", webCertUser.getVardgivare().get(0).getId());

        assertEquals(vardgivare, webCertUser.getVardgivare().get(0));

        assertEquals(vardgivare, webCertUser.getValdVardgivare());

        assertNotNull(webCertUser.getValdVardenhet());
        assertEquals(ENHET_HSA_ID, webCertUser.getValdVardenhet().getId());

        assertEquals(3, webCertUser.getSpecialiseringar().size());
        assertEquals(2, webCertUser.getLegitimeradeYrkesgrupper().size());
        
        assertEquals(HEAD_DOCTOR, webCertUser.getTitel());
        
        assertFalse(webCertUser.getAktivaFunktioner().isEmpty());

        verify(hsaOrganizationsService).getAuthorizedEnheterForHosPerson(PERSONAL_HSA_ID);
        verify(hsaPersonService).getHsaPersonInfo(PERSONAL_HSA_ID);
        verify(webcertFeatureService).getActiveFeatures();
    }

    @Test
    public void testPopulatingWebCertUserWithTwoUserTypes() throws Exception {

        setupCallToAuthorizedEnheterForHosPerson();
                
        GetHsaPersonHsaUserType userType1 = buildGetHsaPersonHsaUserType(PERSONAL_HSA_ID, "Titel1", 
                Arrays.asList("Kirurgi", "Öron-, näs- och halssjukdomar"), Arrays.asList("Läkare"));
        
        GetHsaPersonHsaUserType userType2 = buildGetHsaPersonHsaUserType(PERSONAL_HSA_ID, "Titel2", 
                Arrays.asList("Kirurgi", "Reumatologi"), Arrays.asList("Psykoterapeut"));
        
        List<GetHsaPersonHsaUserType> userTypes = Arrays.asList(userType1, userType2);

        when(hsaPersonService.getHsaPersonInfo(PERSONAL_HSA_ID)).thenReturn(userTypes);

        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-code-lakare.xml");

        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertEquals(PERSONAL_HSA_ID, webCertUser.getHsaId());
        assertEquals("Markus Gran", webCertUser.getNamn());
        
        assertEquals(3, webCertUser.getSpecialiseringar().size());
        assertEquals(2, webCertUser.getLegitimeradeYrkesgrupper().size());
        
        assertEquals("Titel1, Titel2", webCertUser.getTitel());

        verify(hsaOrganizationsService).getAuthorizedEnheterForHosPerson(PERSONAL_HSA_ID);
        verify(hsaPersonService).getHsaPersonInfo(PERSONAL_HSA_ID);
    }
    
    private void setupCallToAuthorizedEnheterForHosPerson() {
        vardgivare = new Vardgivare("vg", "Landstinget Ingenmansland");
        vardgivare.getVardenheter().add(new Vardenhet("vardcentralen", "Vårdcentralen"));
        vardgivare.getVardenheter().add(new Vardenhet(ENHET_HSA_ID, "TestVårdEnhet2A VårdEnhet2A"));

        List<Vardgivare> vardgivareList = Collections.singletonList(vardgivare);

        when(hsaOrganizationsService.getAuthorizedEnheterForHosPerson(PERSONAL_HSA_ID)).thenReturn(
                vardgivareList);
    }
    
    private void setupCallToGetHsaPersonInfo() {
        
        List<String> specs = Arrays.asList("Kirurgi", "Öron-, näs- och halssjukdomar", "Reumatologi");
        List<String> titles = Arrays.asList("Läkare", "Psykoterapeut");
        
        List<GetHsaPersonHsaUserType> userTypes = Arrays.asList(buildGetHsaPersonHsaUserType(PERSONAL_HSA_ID, HEAD_DOCTOR, specs, titles));

        when(hsaPersonService.getHsaPersonInfo(PERSONAL_HSA_ID)).thenReturn(userTypes);
    }
    
    private void setupCallToWebcertFeatureService() {
        Set<String> availableFeatures = new TreeSet<String>();
        availableFeatures.add("feature1");
        availableFeatures.add("feature2");
        when(webcertFeatureService.getActiveFeatures()).thenReturn(availableFeatures);
    }
    
    @Test
    public void testLakareTitle() throws Exception {
        setupCallToAuthorizedEnheterForHosPerson();
        setupCallToGetHsaPersonInfo();
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-lakare.xml");
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);
        assertTrue(webCertUser.isLakare());
    }

    @Test
    public void testLakareTitleCode() throws Exception {
        setupCallToAuthorizedEnheterForHosPerson();
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-code-lakare.xml");
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);
        assertTrue(webCertUser.isLakare());
    }

    @Test
    public void testNoLakare() throws Exception {
        setupCallToAuthorizedEnheterForHosPerson();
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-no-lakare.xml");
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);
        assertFalse(webCertUser.isLakare());
    }

    @Test
    public void testNoGivenName() throws Exception {
        setupCallToAuthorizedEnheterForHosPerson();
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-no-givenname.xml");
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);
        assertEquals("Gran", webCertUser.getNamn());
    }

    @Test(expected = MissingMedarbetaruppdragException.class)
    public void testMissingMedarbetaruppdrag() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-no-lakare.xml");
        userDetailsService.loadUserBySAML(samlCredential);
    }

    @Test(expected = MissingMedarbetaruppdragException.class)
    public void testMissingSelectedUnit() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-without-enhet.xml");
        userDetailsService.loadUserBySAML(samlCredential);
    }

    @Test(expected = HsaServiceException.class)
    public void unexpectedExceptionWhenprocessingData() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-lakare.xml");
        when(hsaOrganizationsService.getAuthorizedEnheterForHosPerson(anyString())).thenThrow(new NullPointerException());
        userDetailsService.loadUserBySAML(samlCredential);
        fail("Expected exception");
    }

    private SAMLCredential createSamlCredential(String filename) throws Exception {
        Document doc = (Document) XMLUtils.fromSource(new StreamSource(new ClassPathResource(
                "WebCertUserDetailsServiceTest/" + filename).getInputStream()));
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(Assertion.DEFAULT_ELEMENT_NAME);

        Assertion assertion = (Assertion) unmarshaller.unmarshall(doc.getDocumentElement());
        NameID nameId = assertion.getSubject().getNameID();
        return new SAMLCredential(nameId, assertion, "remoteId", "localId");
    }
    
    private GetHsaPersonHsaUserType buildGetHsaPersonHsaUserType(String hsaId, String title, List<String> specialities, List<String> titles) {

        GetHsaPersonHsaUserType type = new GetHsaPersonHsaUserType();
        type.setHsaIdentity(hsaId);
        type.setTitle(title);
        
        HsaTitles hsaTitles = new HsaTitles();
        hsaTitles.getHsaTitle().addAll(titles);
        type.setHsaTitles(hsaTitles);

        SpecialityNames specNames = new SpecialityNames();
        specNames.getSpecialityName().addAll(specialities);
        type.setSpecialityNames(specNames);
        
        return type;
    }
}
