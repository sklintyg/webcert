package se.inera.intyg.webcert.web.auth.eleg;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml2.core.NameID;
import org.springframework.security.saml.SAMLCredential;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolver;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.auth.common.BaseSAMLCredentialTest;
import se.inera.intyg.webcert.web.auth.exceptions.HsaServiceException;
import se.inera.intyg.webcert.web.auth.exceptions.PrivatePractitionerAuthorizationException;
import se.inera.intyg.webcert.web.service.feature.WebcertFeatureService;
import se.inera.intyg.webcert.web.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.HsaId;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.PersonId;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.VardgivareType;

import java.util.HashSet;

/**
 * Created by eriklupander on 2015-06-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElegWebCertUserDetailsServiceTest extends BaseSAMLCredentialTest {

    private static final String LOCAL_ENTITY_ID = "localEntityId";
    private static final String REMOTE_ENTITY_ID = "remoteEntityId";
    private static final String HSA_ID = "191212121212";
    private static final String PERSON_ID = "197705232382";

    @Mock
    private PPService ppService;

    @Mock
    private WebcertFeatureService webcertFeatureService;

    @Mock
    AuthoritiesResolver authoritiesResolver;

    @Mock
    private AvtalService avtalService;

    @Mock
    private ElegAuthenticationAttributeHelper elegAuthenticationAttributeHelper;

    @Mock
    private ElegAuthenticationMethodResolver elegAuthenticationMethodResolver;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private ElegWebCertUserDetailsService testee;

    @BeforeClass
    public static void readSamlAssertions() throws Exception {
        bootstrapSamlAssertions();
    }

    @Before
    public void setupForSuccess() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_PRIVATLAKARE);

        when(authoritiesResolver.getRole(anyString())).thenReturn(role);
        when(ppService.getPrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(buildHosPerson());
        when(ppService.validatePrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(true);
        when(webcertFeatureService.getActiveFeatures()).thenReturn(new HashSet<String>());
        when(avtalService.userHasApprovedLatestAvtal(anyString())).thenReturn(true);
    }

    @Test
    public void testSuccessfulLogin() {
        Object o = testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
        assertNotNull(o);

        // WEBCERT-2028
        verify(avtalService, times(1)).userHasApprovedLatestAvtal(anyString());
    }

    @Test
    public void testNotValidPrivatePractitionerThrowsException() {
        reset(ppService);
        when(ppService.validatePrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(false);

        thrown.expect(PrivatePractitionerAuthorizationException.class);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

    @Test
    public void testNotFoundInHSAThrowsException() {
        reset(ppService);
        when(ppService.validatePrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(true);
        when(ppService.getPrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(null);

        thrown.expect(HsaServiceException.class);

        testee.loadUserBySAML(new SAMLCredential(mock(NameID.class), assertionPrivatlakare, REMOTE_ENTITY_ID, LOCAL_ENTITY_ID));
    }

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
