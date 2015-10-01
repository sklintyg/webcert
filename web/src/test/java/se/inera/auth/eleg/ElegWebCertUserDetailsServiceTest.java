package se.inera.auth.eleg;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml2.core.NameID;
import org.springframework.security.saml.SAMLCredential;
import se.inera.auth.common.BaseSAMLCredentialTest;
import se.inera.intyg.webcert.integration.pp.services.PPService;
import se.inera.webcert.common.security.authority.UserPrivilege;
import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.persistence.roles.model.Privilege;
import se.inera.webcert.persistence.roles.model.Role;
import se.inera.webcert.persistence.roles.repository.RoleRepository;
import se.inera.webcert.service.feature.WebcertFeatureService;
import se.inera.webcert.service.privatlakaravtal.AvtalService;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.HsaId;
import se.riv.infrastructure.directory.privatepractitioner.types.v1.PersonId;
import se.riv.infrastructure.directory.privatepractitioner.v1.EnhetType;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;
import se.riv.infrastructure.directory.privatepractitioner.v1.VardgivareType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by eriklupander on 2015-06-25.
 */
@RunWith(MockitoJUnitRunner.class)
public class ElegWebCertUserDetailsServiceTest extends BaseSAMLCredentialTest {

    private static final String LOCAL_ENTITY_ID = "localEntityId";
    private static final String REMOTE_ENTITY_ID = "remoteEntityId";
    private static final String HSA_ID = "191212121212";
    private static final String PERSON_ID = "197705232382";
    //private static Assertion assertionPrivatlakare;

    @Mock
    private PPService ppService;

    @Mock
    private RoleRepository roleRepository;

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
        bootstrapSamlAssertions();
    }

    @Test
    public void testSuccessfulLogin() {
        when(ppService.getPrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(buildHosPerson());
        when(ppService.validatePrivatePractitioner(anyString(), anyString(), anyString())).thenReturn(true);
        when(roleRepository.findByName(anyString())).thenReturn(buildUserRoles().get(0));
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

    private List<Role> buildUserRoles() {
        List<Privilege> privileges = new ArrayList<>();

        for (UserPrivilege up: UserPrivilege.values()) {
            Privilege privilege = new Privilege(up.name());
            privileges.add(privilege);
        }

        Role role = new Role(UserRole.ROLE_LAKARE.name());
        role.setPrivileges(privileges);

        return Arrays.asList(role);
    }
}
