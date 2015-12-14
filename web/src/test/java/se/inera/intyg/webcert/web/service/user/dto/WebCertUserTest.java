package se.inera.intyg.webcert.web.service.user.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.webcert.integration.hsa.model.Mottagning;
import se.inera.intyg.webcert.integration.hsa.model.Vardenhet;
import se.inera.intyg.webcert.integration.hsa.model.Vardgivare;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesConstants;
import se.inera.intyg.webcert.web.auth.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.webcert.web.auth.authorities.Role;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class WebCertUserTest extends AuthoritiesConfigurationTestSetup {

    @InjectMocks
    private WebCertUser user;

    @Before
    public void setup() throws Exception {
        setupWebCertUser();
    }

    @Test
    public void testGetAsJson() {
        String res = user.getAsJson();
        assertNotNull(res);
        assertTrue(res.length() > 0);
        //System.out.println(res);
    }

    @Test
    public void testIsLakare() {
        assertTrue(user.isLakare());

        setUserRole(AuthoritiesConstants.ROLE_ADMIN);
        assertFalse(user.isLakare());

        setUserRole(AuthoritiesConstants.ROLE_PRIVATLAKARE);
        assertTrue(user.isLakare());

        setUserRole(AuthoritiesConstants.ROLE_TANDLAKARE);
        assertTrue(user.isLakare());
    }

    @Test
    public void testChangeValdVardenhetWithNullParam() {
        boolean res = user.changeValdVardenhet(null);
        assertFalse(res);
    }

    @Test
    public void testChangeValdVardenhetThatIsAVardenhet() {
        boolean res = user.changeValdVardenhet("VG1VE2");
        assertTrue(res);
        assertEquals("Vardenhet 2", user.getValdVardenhet().getNamn());
        assertEquals("Vardgivare 1", user.getValdVardgivare().getNamn());
    }

    @Test
    public void testChangeValdVardenhetThatIsAMottagning() {
        boolean res = user.changeValdVardenhet("VG2VE1M1");
        assertTrue(res);
        assertEquals("Mottagning 1", user.getValdVardenhet().getNamn());
        assertEquals("Vardgivare 2", user.getValdVardgivare().getNamn());
    }

    @Test
    public void testGetVardenheterIdsWithMottagningSelected() {

        // Set a Vardenhet that has no Mottagningar as selected
        boolean res = user.changeValdVardenhet("VG1VE1");
        assertTrue(res);

        List<String> ids = user.getIdsOfSelectedVardenhet();
        assertNotNull(ids);
        assertEquals(1, ids.size());
    }

    @Test
    public void testGetVardenheterIdsWithVardenhetSelected() {

        // Set the Vardenhet that has a Mottagning attached as selected
        boolean res = user.changeValdVardenhet("VG2VE1");
        assertTrue(res);

        List<String> ids = user.getIdsOfSelectedVardenhet();
        assertNotNull(ids);
        assertEquals(2, ids.size());
    }

    @Test
    public void testGetIdsOfAllVardenheter() {

        List<String> ids = user.getIdsOfAllVardenheter();
        assertNotNull(ids);
        assertEquals(5, ids.size());
    }

    @Test
    public void testGetTotaltAntalVardenheter() {
        int res = user.getTotaltAntalVardenheter();
        assertEquals(5, res);
    }

    @Test
    public void testGetTotaltAntalVardenheterWithNoVardgivare() {
        user.getVardgivare().clear();
        int res = user.getTotaltAntalVardenheter();
        assertEquals(0, res);
    }

    private void setupWebCertUser() {
        user.setNamn("A Name");
        user.setHsaId("HSA-id");
        user.setForskrivarkod("Forskrivarkod");
        user.setAuthenticationScheme("AuthScheme");
        user.setSpecialiseringar(Arrays.asList("Kirurgi", "Ortopedi"));

        // Setup where user originates from
        user.setOrigin(WebCertUserOriginType.NORMAL.name());

        // Set the user's role
        setUserRole(AuthoritiesConstants.ROLE_LAKARE);

        // Setup MIU
        List<Vardgivare> vardgivare = new ArrayList<>();

        Vardgivare vg1 = new Vardgivare("VG1", "Vardgivare 1");

        Vardenhet vg1ve1 = new Vardenhet("VG1VE1", "Vardenhet 1");
        vg1.getVardenheter().add(vg1ve1);

        Vardenhet vg1ve2 = new Vardenhet("VG1VE2", "Vardenhet 2");
        vg1.getVardenheter().add(vg1ve2);

        Vardgivare vg2 = new Vardgivare("VG2", "Vardgivare 2");

        Vardenhet vg2ve1 = new Vardenhet("VG2VE1", "Vardenhet 3");
        vg2.getVardenheter().add(vg2ve1);

        Vardenhet vg2ve2 = new Vardenhet("VG2VE2", "Vardenhet 4");
        vg2.getVardenheter().add(vg2ve2);

        Mottagning vg2ve2m1 = new Mottagning("VG2VE1M1", "Mottagning 1");
        vg2ve1.getMottagningar().add(vg2ve2m1);

        vardgivare.add(vg1);
        vardgivare.add(vg2);

        user.setVardgivare(vardgivare);
        user.setValdVardenhet(vg2ve2m1);
        user.setValdVardgivare(vg2);
    }

    private void setUserRole(String roleName ) {
        Role role = AUTHORITIES_RESOLVER.getRole(roleName);

        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges()));
    }

}
