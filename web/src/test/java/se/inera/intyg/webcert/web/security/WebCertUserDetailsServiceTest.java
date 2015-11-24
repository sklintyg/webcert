package se.inera.intyg.webcert.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.SPRING_SECURITY_SAVED_REQUEST_KEY;

import org.apache.cxf.staxutils.StaxUtils;
import org.junit.Before;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.Document;
import se.inera.intyg.webcert.web.auth.exceptions.HsaServiceException;
import se.inera.intyg.webcert.web.auth.exceptions.MissingMedarbetaruppdragException;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType.HsaTitles;
import se.inera.ifv.hsawsresponder.v3.GetHsaPersonHsaUserType.SpecialityNames;
import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.services.HsaOrganizationsService;
import se.inera.webcert.hsa.services.HsaPersonService;
import se.inera.intyg.webcert.persistence.roles.model.Privilege;
import se.inera.intyg.webcert.persistence.roles.model.Role;
import se.inera.intyg.webcert.persistence.roles.model.TitleCode;
import se.inera.intyg.webcert.persistence.roles.repository.RoleRepository;
import se.inera.intyg.webcert.persistence.roles.repository.TitleCodeRepository;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.xml.transform.stream.StreamSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author andreaskaltenbach
 */
@RunWith(MockitoJUnitRunner.class)
public class WebCertUserDetailsServiceTest {

    private static final String PERSONAL_HSAID = "TST5565594230-106J";

    private static final String VARDGIVARE_HSAID = "IFV1239877878-0001";
    private static final String ENHET_HSAID_1 = "IFV1239877878-103H";
    private static final String ENHET_HSAID_2 = "IFV1239877878-103P";

    private static final String TITLE_HEAD_DOCTOR = "Överläkare";

    @InjectMocks
    private WebCertUserDetailsService userDetailsService = new WebCertUserDetailsService();

    @Mock
    private HsaOrganizationsService hsaOrganizationsService;

    @Mock
    private HsaPersonService hsaPersonService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private TitleCodeRepository titleCodeRepository;

    @Mock
    private WebcertFeatureService webcertFeatureService;

    @Mock
    private MonitoringLogService monitoringLogService;

    private Vardgivare vardgivare;

    @BeforeClass
    public static void bootstrapOpenSaml() throws Exception {
        DefaultBootstrap.bootstrap();
    }

    @Before
    public void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserHasTitleLakare() throws Exception {
        // given
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-lakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(roleRepository.findByName(UserRole.ROLE_LAKARE.name())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_LAKARE.name()));
        assertUserPrivileges(UserRole.ROLE_LAKARE, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserHasTitleTandLakare() throws Exception {
        // given
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-tandlakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(roleRepository.findByName(UserRole.ROLE_TANDLAKARE.name())).thenReturn(getUserRoles(UserRole.ROLE_TANDLAKARE).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_TANDLAKARE.name()));
        assertUserPrivileges(UserRole.ROLE_TANDLAKARE, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserHasMultipleTitles() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-multiple-titles.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(roleRepository.findByName(UserRole.ROLE_LAKARE.name())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_LAKARE.name()));
        assertUserPrivileges(UserRole.ROLE_LAKARE, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserIsAtLakare() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-at-lakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        TitleCode titleCode = new TitleCode("204010", "0000000‘", getUserRoles(UserRole.ROLE_LAKARE).get(0));

        // when
        when(roleRepository.findByName(UserRole.ROLE_LAKARE.name())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE).get(0));
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode(anyString(), anyString())).thenReturn(titleCode);

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_LAKARE.name()));
        assertUserPrivileges(UserRole.ROLE_LAKARE, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserIsAtLakareButWithoutLicense() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-at-lakare-utan-legitimation.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        TitleCode titleCode = new TitleCode("204090", "9100009‘", getUserRoles(UserRole.ROLE_LAKARE).get(0));

        // when
        when(roleRepository.findByName(UserRole.ROLE_LAKARE.name())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE).get(0));
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode(anyString(), anyString())).thenReturn(titleCode);

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_LAKARE.name()));
        assertUserPrivileges(UserRole.ROLE_LAKARE, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilgesWhenUserIsDoctorButHasNotYetASwedishLicense() throws Exception {
        // given
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-lakare-with-titleCode-and-groupPrescriptionCode.xml");
        List<GetHsaPersonHsaUserType> userTypes = Collections.singletonList(buildGetHsaPersonHsaUserType(PERSONAL_HSAID, null, null, null));
        TitleCode titleCode = new TitleCode("204090", "9100009", getUserRoles(UserRole.ROLE_LAKARE).get(0));

        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(hsaPersonService.getHsaPersonInfo(PERSONAL_HSAID)).thenReturn(userTypes);
        when(roleRepository.findByName(anyString())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE).get(0));
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode(anyString(), anyString())).thenReturn(titleCode);

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_LAKARE.name()));
        assertUserPrivileges(UserRole.ROLE_LAKARE, webCertUser);
    }

    @Test(expected = RuntimeException.class)
    public void assertRoleAndPrivilgesWhenTitleCodeAndGroupPrescriptionCodeIsEmptyObject() throws Exception {
        // given
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-lakare-with-titleCode-and-groupPrescriptionCode.xml");
        List<GetHsaPersonHsaUserType> userTypes = Collections.singletonList(buildGetHsaPersonHsaUserType(PERSONAL_HSAID, null, null, null));

        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(hsaPersonService.getHsaPersonInfo(PERSONAL_HSAID)).thenReturn(userTypes);
        when(roleRepository.findByName(anyString())).thenReturn(getUserRoles(UserRole.ROLE_VARDADMINISTRATOR).get(0));
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode(anyString(), anyString())).thenReturn(new TitleCode());

        // then
        userDetailsService.loadUserBySAML(samlCredential);
    }

    @Test
    public void assertRoleAndPrivilgesWhenTitleCodeAndGroupPrescriptionCodeDoesNotMatch() throws Exception {
        // given
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-lakare-with-titleCode-and-bad-groupPrescriptionCode.xml");
        List<GetHsaPersonHsaUserType> userTypes = Collections.singletonList(buildGetHsaPersonHsaUserType(PERSONAL_HSAID, null, null, null));

        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(hsaPersonService.getHsaPersonInfo(PERSONAL_HSAID)).thenReturn(userTypes);
        when(roleRepository.findByName(anyString())).thenReturn(getUserRoles(UserRole.ROLE_VARDADMINISTRATOR).get(0));
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode(anyString(), anyString())).thenReturn(null);

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_VARDADMINISTRATOR.name()));
        assertTrue("0000000".equals(webCertUser.getForskrivarkod()));
        assertUserPrivileges(UserRole.ROLE_VARDADMINISTRATOR, webCertUser);
    }

    @Test
    public void assertRoleLakareWhenUserHasMultipleTitleCodes() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-multiple-title-codes.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        TitleCode titleCode = new TitleCode("204010", "0000000‘", getUserRoles(UserRole.ROLE_LAKARE).get(0));

        // when
        when(roleRepository.findByName(UserRole.ROLE_LAKARE.name())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE).get(0));
        when(titleCodeRepository.findByTitleCodeAndGroupPrescriptionCode(anyString(), anyString())).thenReturn(titleCode);

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_LAKARE.name()));
        assertUserPrivileges(UserRole.ROLE_LAKARE, webCertUser);
    }

    @Test
    public void assertRoleVardadministratorWhenUserIsNotADoctor() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-no-lakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();
        // setupCallToGetHsaPersonInfo();

        // when
        when(roleRepository.findByName(UserRole.ROLE_VARDADMINISTRATOR.name())).thenReturn(getUserRoles(UserRole.ROLE_VARDADMINISTRATOR).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_VARDADMINISTRATOR.name()));
        assertUserPrivileges(UserRole.ROLE_VARDADMINISTRATOR, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserHasTitleDoctorAndUsesDjupintegrationsLink() throws Exception {
        // given
        String requestURI = "/visa/intyg/789YAU453999KL2JK/alternatePatientSSn=191212121212&responsibleHospName=ÅsaAndersson";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequestAttributes(requestURI)));

        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-lakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(roleRepository.findByName(UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE_DJUPINTEGRERAD).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name()));
        assertUserPrivileges(UserRole.ROLE_LAKARE_DJUPINTEGRERAD, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserHasTitleDoctorAndUsesUthoppsLink() throws Exception {
        // given
        String requestURI = "/webcert/web/user/certificate/789YAU453999KL2JK/questions";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequestAttributes(requestURI)));

        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-lakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(roleRepository.findByName(UserRole.ROLE_LAKARE_UTHOPP.name())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE_UTHOPP).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_LAKARE_UTHOPP.name()));
        assertUserPrivileges(UserRole.ROLE_LAKARE_UTHOPP, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserHasTitleTandlakareAndUsesDjupintegrationsLink() throws Exception {
        // given
        String requestURI = "/visa/intyg/789YAU453999KL2JK/alternatePatientSSn=191212121212&responsibleHospName=ÅsaAndersson";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequestAttributes(requestURI)));

        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-tandlakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(roleRepository.findByName(UserRole.ROLE_TANDLAKARE_DJUPINTEGRERAD.name())).thenReturn(getUserRoles(UserRole.ROLE_TANDLAKARE_DJUPINTEGRERAD).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_TANDLAKARE_DJUPINTEGRERAD.name()));
        assertUserPrivileges(UserRole.ROLE_TANDLAKARE_DJUPINTEGRERAD, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserHasTitleTandlakareAndUsesUthoppsLink() throws Exception {
        // given
        String requestURI = "/webcert/web/user/certificate/789YAU453999KL2JK/questions";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequestAttributes(requestURI)));

        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-tandlakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(roleRepository.findByName(UserRole.ROLE_TANDLAKARE_UTHOPP.name())).thenReturn(getUserRoles(UserRole.ROLE_TANDLAKARE_UTHOPP).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_TANDLAKARE_UTHOPP.name()));
        assertUserPrivileges(UserRole.ROLE_TANDLAKARE_UTHOPP, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserIsNotDoctorAndUsesDjupintegrationsLink() throws Exception {
        // given
        String requestURI = "/visa/intyg/789YAU453999KL2JK/alternatePatientSSn=191212121212&responsibleHospName=ÅsaAndersson";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequestAttributes(requestURI)));

        SAMLCredential samlCredential = createSamlCredential("saml-assertion-no-lakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(roleRepository.findByName(UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD.name()))
                .thenReturn(getUserRoles(UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD.name()));
        assertUserPrivileges(UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD, webCertUser);
    }

    @Test
    public void assertRoleAndPrivilegesWhenUserIsNotDoctorAndUsesUthoppsLink() throws Exception {
        // given
        String requestURI = "/webcert/web/user/certificate/789YAU453999KL2JK/questions";
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequestAttributes(requestURI)));

        SAMLCredential samlCredential = createSamlCredential("saml-assertion-no-lakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(roleRepository.findByName(UserRole.ROLE_VARDADMINISTRATOR_UTHOPP.name()))
                .thenReturn(getUserRoles(UserRole.ROLE_VARDADMINISTRATOR_UTHOPP).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_VARDADMINISTRATOR_UTHOPP.name()));
        assertUserPrivileges(UserRole.ROLE_VARDADMINISTRATOR_UTHOPP, webCertUser);
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

    @Test
    public void testNoGivenName() throws Exception {
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-no-givenname.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        // when
        when(roleRepository.findByName(UserRole.ROLE_LAKARE.name())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertEquals("Gran", webCertUser.getNamn());
    }

    @Test
    public void testPopulatingWebCertUser() throws Exception {
        // given
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-lakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();
        setupCallToGetHsaPersonInfo();
        setupCallToWebcertFeatureService();

        // when
        when(roleRepository.findByName(UserRole.ROLE_LAKARE.name())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertEquals(PERSONAL_HSAID, webCertUser.getHsaId());
        assertEquals("Markus Gran", webCertUser.getNamn());
        assertEquals(1, webCertUser.getVardgivare().size());
        assertEquals(VARDGIVARE_HSAID, webCertUser.getVardgivare().get(0).getId());
        assertEquals(vardgivare, webCertUser.getVardgivare().get(0));
        assertEquals(vardgivare, webCertUser.getValdVardgivare());
        assertNotNull(webCertUser.getValdVardenhet());
        assertEquals(ENHET_HSAID_1, webCertUser.getValdVardenhet().getId());
        assertEquals(3, webCertUser.getSpecialiseringar().size());
        assertEquals(2, webCertUser.getLegitimeradeYrkesgrupper().size());
        assertEquals(TITLE_HEAD_DOCTOR, webCertUser.getTitel());
        assertFalse(webCertUser.getAktivaFunktioner().isEmpty());

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_LAKARE.name()));
        assertUserPrivileges(UserRole.ROLE_LAKARE, webCertUser);

        verify(hsaOrganizationsService).getAuthorizedEnheterForHosPerson(PERSONAL_HSAID);
        verify(hsaPersonService).getHsaPersonInfo(PERSONAL_HSAID);
        verify(webcertFeatureService).getActiveFeatures();
    }

    @Test
    public void testPopulatingWebCertUserWithTwoUserTypes() throws Exception {
        // given
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-lakare.xml");
        setupCallToAuthorizedEnheterForHosPerson();

        GetHsaPersonHsaUserType userType1 = buildGetHsaPersonHsaUserType(PERSONAL_HSAID, "Titel1",
                Arrays.asList("Kirurgi", "Öron-, näs- och halssjukdomar"), Collections.singletonList("Läkare"));
        GetHsaPersonHsaUserType userType2 = buildGetHsaPersonHsaUserType(PERSONAL_HSAID, "Titel2", Arrays.asList("Kirurgi", "Reumatologi"),
                Collections.singletonList("Psykoterapeut"));
        List<GetHsaPersonHsaUserType> userTypes = Arrays.asList(userType1, userType2);

        // when
        when(hsaPersonService.getHsaPersonInfo(PERSONAL_HSAID)).thenReturn(userTypes);
        when(roleRepository.findByName(UserRole.ROLE_LAKARE.name())).thenReturn(getUserRoles(UserRole.ROLE_LAKARE).get(0));

        // then
        WebCertUser webCertUser = (WebCertUser) userDetailsService.loadUserBySAML(samlCredential);

        assertEquals(PERSONAL_HSAID, webCertUser.getHsaId());
        assertEquals("Markus Gran", webCertUser.getNamn());

        assertEquals(3, webCertUser.getSpecialiseringar().size());
        assertEquals(2, webCertUser.getLegitimeradeYrkesgrupper().size());

        assertEquals("Titel1, Titel2", webCertUser.getTitel());

        assertTrue(webCertUser.getRoles().containsKey(UserRole.ROLE_LAKARE.name()));
        assertUserPrivileges(UserRole.ROLE_LAKARE, webCertUser);

        verify(hsaOrganizationsService).getAuthorizedEnheterForHosPerson(PERSONAL_HSAID);
        verify(hsaPersonService).getHsaPersonInfo(PERSONAL_HSAID);
    }

    @Test(expected = HsaServiceException.class)
    public void unexpectedExceptionWhenProcessingData() throws Exception {
        // given
        SAMLCredential samlCredential = createSamlCredential("saml-assertion-with-title-lakare.xml");

        // when
        when(hsaOrganizationsService.getAuthorizedEnheterForHosPerson(anyString())).thenThrow(new NullPointerException());

        // then
        userDetailsService.loadUserBySAML(samlCredential);

        // fail the test if we come to this point
        fail("Expected exception");
    }

    @Test
    public void testDjupintegrationRegexp() throws Exception {
        assertTrue("/visa/intyg/99aaa4f1-d862-4750-a628-f7dcb9c8bac0".matches(WebCertUserDetailsService.REGEXP_REQUESTURI_DJUPINTEGRATION));
        assertTrue("/visa/intyg/99aaa4f1-d862-4750-a628-f7dcb9c8bac0/".matches(WebCertUserDetailsService.REGEXP_REQUESTURI_DJUPINTEGRATION));
    }

    // ~ Private assertion methods
    // =====================================================================================

    private void assertUserPrivileges(UserRole userRole, WebCertUser user) {
        switch (userRole) {
        case ROLE_LAKARE:
            assertUserPrivileges(UserPrivilege.values(), user);
            break;
        case ROLE_TANDLAKARE:
            assertUserPrivileges(UserPrivilege.values(), user);
            break;
        case ROLE_LAKARE_DJUPINTEGRERAD:
            assertUserPrivileges(getUserPrivilegesForDjupintegreradLakare(), user);
            break;
        case ROLE_LAKARE_UTHOPP:
            assertUserPrivileges(getUserPrivilegesForUthoppLakare(), user);
            break;
        case ROLE_TANDLAKARE_DJUPINTEGRERAD:
            assertUserPrivileges(getUserPrivilegesForDjupintegreradTandlakare(), user);
            break;
        case ROLE_TANDLAKARE_UTHOPP:
            assertUserPrivileges(getUserPrivilegesForUthoppTandlakare(), user);
            break;
        case ROLE_VARDADMINISTRATOR:
            assertUserPrivileges(getUserPrivilegesForVardadministrator(), user);
            break;
        case ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD:
            assertUserPrivileges(getUserPrivilegesForDjupintegreradVardadministrator(), user);
            break;
        case ROLE_VARDADMINISTRATOR_UTHOPP:
            assertUserPrivileges(getUserPrivilegesForUthoppVardadministrator(), user);
            break;
        default:
            fail("Cannot assert user privileges");
        }
    }

    private void assertUserPrivileges(UserPrivilege[] userPrivileges, WebCertUser user) {
        Map<String, UserPrivilege> authorities = user.getAuthorities();

        for (UserPrivilege up : userPrivileges) {
            assertTrue(authorities.containsKey(up.name()));
        }
    }

    // ~ Private setup methods
    // =====================================================================================

    private GetHsaPersonHsaUserType buildGetHsaPersonHsaUserType(String hsaId, String title, List<String> specialities, List<String> titles) {

        GetHsaPersonHsaUserType type = new GetHsaPersonHsaUserType();
        type.setHsaIdentity(hsaId);

        if (title != null) {
            type.setTitle(title);
        }

        if ((titles != null) && (titles.size() > 0)) {
            HsaTitles hsaTitles = new HsaTitles();
            hsaTitles.getHsaTitle().addAll(titles);
            type.setHsaTitles(hsaTitles);
        }

        if ((specialities != null) && (specialities.size() > 0)) {
            SpecialityNames specNames = new SpecialityNames();
            specNames.getSpecialityName().addAll(specialities);
            type.setSpecialityNames(specNames);
        }

        return type;
    }

    private SAMLCredential createSamlCredential(String filename) throws Exception {
        Document doc = StaxUtils.read(new StreamSource(new ClassPathResource(
                "WebCertUserDetailsServiceTest/" + filename).getInputStream()));
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(Assertion.DEFAULT_ELEMENT_NAME);

        Assertion assertion = (Assertion) unmarshaller.unmarshall(doc.getDocumentElement());
        NameID nameId = assertion.getSubject().getNameID();
        return new SAMLCredential(nameId, assertion, "remoteId", "localId");
    }

    private MockHttpServletRequest mockRequestAttributes(String requestURI) {
        MockHttpServletRequest request = new MockHttpServletRequest();

        if ((requestURI != null) && (requestURI.length() > 0)) {
            request.setRequestURI(requestURI);
        }

        SavedRequest savedRequest = new DefaultSavedRequest(request, new PortResolverImpl());
        request.getSession().setAttribute(SPRING_SECURITY_SAVED_REQUEST_KEY, savedRequest);

        return request;
    }

    private void setupCallToAuthorizedEnheterForHosPerson() {
        vardgivare = new Vardgivare(VARDGIVARE_HSAID, "IFV Testlandsting");
        vardgivare.getVardenheter().add(new Vardenhet(ENHET_HSAID_1, "VårdEnhet2A"));
        vardgivare.getVardenheter().add(new Vardenhet(ENHET_HSAID_2, "Vårdcentralen"));

        List<Vardgivare> vardgivareList = Collections.singletonList(vardgivare);
        when(hsaOrganizationsService.getAuthorizedEnheterForHosPerson(PERSONAL_HSAID)).thenReturn(vardgivareList);
    }

    private void setupCallToGetHsaPersonInfo() {

        List<String> specs = Arrays.asList("Kirurgi", "Öron-, näs- och halssjukdomar", "Reumatologi");
        List<String> titles = Arrays.asList("Läkare", "Psykoterapeut");

        List<GetHsaPersonHsaUserType> userTypes = Collections.singletonList(buildGetHsaPersonHsaUserType(PERSONAL_HSAID, TITLE_HEAD_DOCTOR, specs, titles));

        when(hsaPersonService.getHsaPersonInfo(PERSONAL_HSAID)).thenReturn(userTypes);
    }

    private void setupCallToWebcertFeatureService() {
        Set<String> availableFeatures = new TreeSet<>();
        availableFeatures.add("feature1");
        availableFeatures.add("feature2");
        when(webcertFeatureService.getActiveFeatures()).thenReturn(availableFeatures);
    }

    // ~ Private setup methods for roles and privileges
    // =====================================================================================

    private List<Role> getUserRoles(UserRole userRole) {
        Role role;
        UserPrivilege[] ups;

        List<Privilege> privileges = new ArrayList<>();

        switch (userRole) {
        case ROLE_LAKARE:
            role = new Role(UserRole.ROLE_LAKARE.name());
            ups = UserPrivilege.values();
            break;
        case ROLE_TANDLAKARE:
            role = new Role(UserRole.ROLE_TANDLAKARE.name());
            ups = UserPrivilege.values();
            break;
        case ROLE_LAKARE_DJUPINTEGRERAD:
            role = new Role(UserRole.ROLE_LAKARE_DJUPINTEGRERAD.name());
            ups = getUserPrivilegesForDjupintegreradLakare();
            break;
        case ROLE_LAKARE_UTHOPP:
            role = new Role(UserRole.ROLE_LAKARE_UTHOPP.name());
            ups = getUserPrivilegesForUthoppLakare();
            break;
        case ROLE_TANDLAKARE_DJUPINTEGRERAD:
            role = new Role(UserRole.ROLE_TANDLAKARE_DJUPINTEGRERAD.name());
            ups = getUserPrivilegesForDjupintegreradTandlakare();
            break;
        case ROLE_TANDLAKARE_UTHOPP:
            role = new Role(UserRole.ROLE_TANDLAKARE_UTHOPP.name());
            ups = getUserPrivilegesForUthoppTandlakare();
            break;
        case ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD:
            role = new Role(UserRole.ROLE_VARDADMINISTRATOR_DJUPINTEGRERAD.name());
            ups = getUserPrivilegesForDjupintegreradVardadministrator();
            break;
        case ROLE_VARDADMINISTRATOR_UTHOPP:
            role = new Role(UserRole.ROLE_VARDADMINISTRATOR_UTHOPP.name());
            ups = getUserPrivilegesForUthoppVardadministrator();
            break;
        default:
            role = new Role(UserRole.ROLE_VARDADMINISTRATOR.name());
            ups = getUserPrivilegesForVardadministrator();
        }

        for (UserPrivilege up : ups) {
            Privilege privilege = new Privilege(up.name());
            privileges.add(privilege);
        }

        role.setPrivileges(privileges);

        return Collections.singletonList(role);
    }

    private UserPrivilege[] getUserPrivilegesForUthoppVardadministrator() {
        return new UserPrivilege[] {
                UserPrivilege.PRIVILEGE_KOPIERA_INTYG,
                UserPrivilege.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR,
                UserPrivilege.PRIVILEGE_VIDAREBEFORDRA_UTKAST,
                UserPrivilege.PRIVILEGE_ATKOMST_ANDRA_ENHETER,
                UserPrivilege.PRIVILEGE_HANTERA_PERSONUPPGIFTER,
                UserPrivilege.PRIVILEGE_HANTERA_MAILSVAR,
                UserPrivilege.PRIVILEGE_NAVIGERING };
    }

    private UserPrivilege[] getUserPrivilegesForDjupintegreradVardadministrator() {
        return new UserPrivilege[] {
                UserPrivilege.PRIVILEGE_KOPIERA_INTYG };
    }

    private UserPrivilege[] getUserPrivilegesForUthoppLakare() {
        return new UserPrivilege[] {
                UserPrivilege.PRIVILEGE_SIGNERA_INTYG,
                UserPrivilege.PRIVILEGE_VIDAREBEFORDRA_UTKAST,
                UserPrivilege.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR,
                UserPrivilege.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA,
                UserPrivilege.PRIVILEGE_ATKOMST_ANDRA_ENHETER,
                UserPrivilege.PRIVILEGE_HANTERA_PERSONUPPGIFTER,
                UserPrivilege.PRIVILEGE_HANTERA_MAILSVAR,
                UserPrivilege.PRIVILEGE_NAVIGERING };
    }

    private UserPrivilege[] getUserPrivilegesForDjupintegreradLakare() {
        return new UserPrivilege[] {
                UserPrivilege.PRIVILEGE_KOPIERA_INTYG,
                UserPrivilege.PRIVILEGE_MAKULERA_INTYG,
                UserPrivilege.PRIVILEGE_SIGNERA_INTYG,
                UserPrivilege.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA };
    }

    private UserPrivilege[] getUserPrivilegesForUthoppTandlakare() {
        return new UserPrivilege[] {
                UserPrivilege.PRIVILEGE_SIGNERA_INTYG,
                UserPrivilege.PRIVILEGE_VIDAREBEFORDRA_UTKAST,
                UserPrivilege.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR,
                UserPrivilege.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA,
                UserPrivilege.PRIVILEGE_ATKOMST_ANDRA_ENHETER,
                UserPrivilege.PRIVILEGE_HANTERA_PERSONUPPGIFTER,
                UserPrivilege.PRIVILEGE_HANTERA_MAILSVAR,
                UserPrivilege.PRIVILEGE_NAVIGERING };
    }

    private UserPrivilege[] getUserPrivilegesForDjupintegreradTandlakare() {
        return new UserPrivilege[] {
                UserPrivilege.PRIVILEGE_KOPIERA_INTYG,
                UserPrivilege.PRIVILEGE_MAKULERA_INTYG,
                UserPrivilege.PRIVILEGE_SIGNERA_INTYG,
                UserPrivilege.PRIVILEGE_BESVARA_KOMPLETTERINGSFRAGA };
    }

    private UserPrivilege[] getUserPrivilegesForVardadministrator() {
        return new UserPrivilege[] {
                UserPrivilege.PRIVILEGE_KOPIERA_INTYG,
                UserPrivilege.PRIVILEGE_VIDAREBEFORDRA_FRAGASVAR,
                UserPrivilege.PRIVILEGE_VIDAREBEFORDRA_UTKAST };
    }

}
