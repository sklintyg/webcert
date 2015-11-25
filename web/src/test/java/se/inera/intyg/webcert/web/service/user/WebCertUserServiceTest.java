package se.inera.intyg.webcert.web.service.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.common.support.modules.support.feature.ModuleFeature;
import se.inera.intyg.webcert.common.common.security.authority.UserPrivilege;
import se.inera.intyg.webcert.common.common.security.authority.UserRole;
import se.inera.webcert.hsa.model.Vardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class WebCertUserServiceTest {

    public static final String VARDGIVARE_1 = "VG1";
    public static final String VARDGIVARE_2 = "VG2";

    public static final String VARDENHET_1 = "VG1VE1";
    public static final String VARDENHET_2 = "VG1VE2";
    public static final String VARDENHET_3 = "VG2VE1";
    public static final String VARDENHET_4 = "VG2VE2";

    public WebCertUserServiceImpl webcertUserService = new WebCertUserServiceImpl();

    @Test
    public void testCheckIfAuthorizedForUnit() {
        // anv inloggad på VE1 på VG1
        WebCertUser user = createWebCertUser(false);

        assertTrue("ska kunna titta på ett intyg inom VE1", webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_1, true));
        assertFalse("ska INTE kunna titta på ett intyg inom VE2", webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_2, true));
        assertTrue("ska kunna redigera ett intyg inom VE1", webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_1, false));
        assertFalse("ska INTE kunna redigera ett intyg inom VE2",
                webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_2, false));
    }

    @Test
    public void testCheckIfAuthorizedForUnitWhenIntegrated() {
        // anv i JS-läge inloggad på VE1 på VG1
        WebCertUser user = createWebCertUser(true);

        assertTrue("ska kunna titta på ett intyg inom VE1", webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_1, true));
        assertTrue("ska kunna titta på ett intyg inom VE2", webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_2, true));
        assertTrue("ska kunna redigera ett intyg inom VE1", webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_1, false));
        assertFalse("ska INTE kunna redigera ett intyg inom VE2",
                webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_2, false));
    }

    @Test
    public void testEnableFeatures() {

        WebCertUser user = createWebCertUser(false);

        assertEquals(0, user.getAktivaFunktioner().size());

        webcertUserService.enableFeatures(user, WebcertFeature.HANTERA_FRAGOR, WebcertFeature.HANTERA_INTYGSUTKAST);

        assertEquals(2, user.getAktivaFunktioner().size());
    }

    @Test
    public void testEnableModuleFeatures() {

        WebCertUser user = createWebCertUser(false);

        assertEquals(0, user.getAktivaFunktioner().size());

        // base features must be enabled first
        webcertUserService.enableFeatures(user, WebcertFeature.HANTERA_FRAGOR, WebcertFeature.HANTERA_INTYGSUTKAST);

        assertEquals(2, user.getAktivaFunktioner().size());

        webcertUserService.enableModuleFeatures(user, "fk7263", ModuleFeature.HANTERA_FRAGOR, ModuleFeature.HANTERA_INTYGSUTKAST);

        assertEquals(4, user.getAktivaFunktioner().size());
    }

    private WebCertUser createWebCertUser(boolean fromJS) {

        WebCertUser user = createUser();

        user.setNamn("A Name");
        user.setHsaId("HSA-id");
        user.setForskrivarkod("Forskrivarkod");
        user.setAuthenticationScheme("AuthScheme");
        user.setSpecialiseringar(Arrays.asList("Kirurgi", "Ortopedi"));

        List<Vardgivare> vardgivare = new ArrayList<>();

        Vardgivare vg1 = new Vardgivare(VARDGIVARE_1, "Vardgivare 1");

        Vardenhet vg1ve1 = new Vardenhet(VARDENHET_1, "Vardenhet 1");
        vg1.getVardenheter().add(vg1ve1);
        vg1.getVardenheter().add(new Vardenhet(VARDENHET_2, "Vardenhet 2"));

        Vardgivare vg2 = new Vardgivare(VARDGIVARE_2, "Vardgivare 2");

        vg2.getVardenheter().add(new Vardenhet(VARDENHET_3, "Vardenhet 3"));
        vg2.getVardenheter().add(new Vardenhet(VARDENHET_4, "Vardenhet 4"));

        vardgivare.add(vg1);
        vardgivare.add(vg2);

        user.setVardgivare(vardgivare);

        user.setValdVardenhet(vg1ve1);
        user.setValdVardgivare(vg1);

        if (fromJS) {
            user.setRoles(createUserRoles(UserRole.ROLE_LAKARE_DJUPINTEGRERAD));
        }

        return user;
    }

    private WebCertUser createUser() {
        WebCertUser user = new WebCertUser();
        user.setRoles(getGrantedRole());
        user.setAuthorities(getGrantedPrivileges());
        return user;
    }

    private Map<String, UserRole> getGrantedRole() {
        return createUserRoles(UserRole.ROLE_LAKARE);
    }

    private Map<String, UserPrivilege> getGrantedPrivileges() {
        List<UserPrivilege> list = Arrays.asList(UserPrivilege.values());

        // convert list to map
        Map<String, UserPrivilege> privilegeMap = Maps.uniqueIndex(list, new Function<UserPrivilege, String>() {
            @Override
            public String apply(UserPrivilege userPrivilege) {
                return userPrivilege.name();
            }
        });

        return privilegeMap;
    }

    private Map<String, UserRole> createUserRoles(UserRole... userRoles) {
        Map<String, UserRole> roles = new HashMap<>();

        for (UserRole userRole : userRoles) {
            roles.put(userRole.name(), userRole);
        }

        return roles;
    }

}
