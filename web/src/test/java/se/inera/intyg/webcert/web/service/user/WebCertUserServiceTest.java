/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.infra.security.filter.SessionTimeoutFilter.TIME_TO_INVALIDATE_ATTRIBUTE_NAME;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.FindByIndexNameSessionRepository;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.authorities.AuthoritiesResolverUtil;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.infra.security.common.model.UserOriginType;
import se.inera.intyg.webcert.common.service.user.LoggedInWebcertUser;
import se.inera.intyg.webcert.persistence.anvandarmetadata.model.AnvandarPreference;
import se.inera.intyg.webcert.persistence.anvandarmetadata.repository.AnvandarPreferenceRepository;
import se.inera.intyg.webcert.web.auth.bootstrap.AuthoritiesConfigurationTestSetup;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@RunWith(MockitoJUnitRunner.class)
public class WebCertUserServiceTest extends AuthoritiesConfigurationTestSetup {

    private static final String VARDGIVARE_1 = "VG1";
    private static final String VARDGIVARE_2 = "VG2";

    private static final String VARDENHET_1 = "VG1VE1";
    private static final String VARDENHET_2 = "VG1VE2";
    private static final String VARDENHET_3 = "VG2VE1";
    private static final String VARDENHET_4 = "VG2VE2";

    private static final String MOTTAGNING_1 = "VG1VE1M1";
    private static final String MOTTAGNING_2 = "VG1VE1M2";

    private static final String MOTTAGNING_3 = "VG1VE2M1";

    @Mock
    private AnvandarPreferenceRepository anvandarPreferenceRepository;
    @Mock
    private FindByIndexNameSessionRepository<?> sessionRepository;
    @Mock
    private LoggedInWebcertUserFactory loggedInWebcertUserFactory;

    @InjectMocks
    public WebCertUserServiceImpl webcertUserService;

    @Test
    public void testCheckIfAuthorizedForUnit() {
        // anv inloggad på VE1 på VG1
        WebCertUser user = createWebCertUser(false);

        assertTrue("ska kunna titta på ett intyg inom VE1",
            webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_1, true));
        assertFalse("ska INTE kunna titta på ett intyg inom VE2",
            webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_2, true));
        assertTrue("ska kunna redigera ett intyg inom VE1",
            webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_1, false));
        assertFalse("ska INTE kunna redigera ett intyg inom VE2",
            webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_2, false));
    }

    @Test
    public void testCheckIfAuthorizedForUnitWhenIntegrated() {
        // anv i JS-läge inloggad på VE1 på VG1
        WebCertUser user = createWebCertUser(true);

        assertTrue("ska kunna titta på ett intyg inom VE1",
            webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_1, true));
        assertTrue("ska kunna titta på ett intyg inom VE2",
            webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_2, true));
        assertTrue("ska kunna redigera ett intyg inom VE1",
            webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_1, false));
        assertFalse("ska INTE kunna redigera ett intyg inom VE2",
            webcertUserService.checkIfAuthorizedForUnit(user, VARDGIVARE_1, VARDENHET_2, false));
    }

    @Test
    public void testStoreExistingUserMetadata() {
        WebCertUser user = createWebCertUser(false);
        applyUserToThreadLocalCtx(user);
        when(anvandarPreferenceRepository.findByHsaIdAndKey("HSA-id", "key1"))
            .thenReturn(new AnvandarPreference("HSA-id", "key1", "value1"));

        webcertUserService.storeUserPreference("key1", "value1");
        assertEquals("value1", user.getAnvandarPreference().get("key1"));
        verify(anvandarPreferenceRepository, times(1)).findByHsaIdAndKey("HSA-id", "key1");
        verify(anvandarPreferenceRepository, times(1)).save(any(AnvandarPreference.class));
    }

    @Test
    public void testStoreNonExistingUserMetadata() {
        WebCertUser user = createWebCertUser(false);
        applyUserToThreadLocalCtx(user);
        when(anvandarPreferenceRepository.findByHsaIdAndKey("HSA-id", "key1")).thenReturn(null);

        webcertUserService.storeUserPreference("key1", "value1");
        assertEquals("value1", user.getAnvandarPreference().get("key1"));
        verify(anvandarPreferenceRepository, times(1)).findByHsaIdAndKey("HSA-id", "key1");
        verify(anvandarPreferenceRepository, times(1)).save(any(AnvandarPreference.class));
    }

    @Test
    public void testDeleteStoredAnvandarPreference() {
        AnvandarPreference anvandarPreference = new AnvandarPreference("HSA-id", "key1", "value1");
        WebCertUser user = createWebCertUser(false);
        applyUserToThreadLocalCtx(user);
        when(anvandarPreferenceRepository.findByHsaIdAndKey(user.getHsaId(), "key1")).thenReturn(anvandarPreference);
        webcertUserService.deleteUserPreference("key1");
        verify(anvandarPreferenceRepository, times(1)).findByHsaIdAndKey("HSA-id", "key1");
        verify(anvandarPreferenceRepository, times(1)).delete(anvandarPreference);
    }

    @Test
    public void testDeleteUnknownAnvandarPreference() {
        WebCertUser user = createWebCertUser(false);
        applyUserToThreadLocalCtx(user);
        when(anvandarPreferenceRepository.findByHsaIdAndKey(user.getHsaId(), "key1")).thenReturn(null);
        webcertUserService.deleteUserPreference("key1");
        verify(anvandarPreferenceRepository, times(1)).findByHsaIdAndKey("HSA-id", "key1");
        verify(anvandarPreferenceRepository, times(0)).delete(any(AnvandarPreference.class));
    }

    @Test
    public void testDeleteAllAnvandarPreferences() {
        WebCertUser user = createWebCertUser(false);
        applyUserToThreadLocalCtx(user);
        when(anvandarPreferenceRepository.getAnvandarPreference(user.getHsaId())).thenReturn(buildMapOfAllUserPrefs());
        when(anvandarPreferenceRepository.findByHsaIdAndKey(user.getHsaId(), "key1")).thenReturn(new AnvandarPreference());
        when(anvandarPreferenceRepository.findByHsaIdAndKey(user.getHsaId(), "key2")).thenReturn(new AnvandarPreference());

        webcertUserService.deleteUserPreferences();

        verify(anvandarPreferenceRepository, times(1)).findByHsaIdAndKey("HSA-id", "key1");
        verify(anvandarPreferenceRepository, times(1)).findByHsaIdAndKey("HSA-id", "key2");
        verify(anvandarPreferenceRepository, times(2)).delete(any(AnvandarPreference.class));
    }

    @Test
    public void testGetMiuOk() {
        WebCertUser user = createWebCertUser(false);
        assertEquals("Mitt uppdrag", user.getSelectedMedarbetarUppdragNamn());
    }

    @Test
    public void testGetMiuWhenOnMottagning() {
        WebCertUser user = createWebCertUser(false);
        ((Vardenhet) user.getValdVardenhet()).getMottagningar().add(buildMottagning1());
        user.changeValdVardenhet(MOTTAGNING_1);
        assertEquals("Mitt mottagningsuppdrag", user.getSelectedMedarbetarUppdragNamn());
    }

    @Test
    public void testUserHasAccessToSiblingMottagningAndParentEnhet() {
        WebCertUser user = setupUserMottagningAccessTest();
        user.changeValdVardenhet(MOTTAGNING_1);

        assertUserHasExpectedAccess();
    }

    @Test
    public void testUserHasAccessToChildMottagning() {
        WebCertUser user = setupUserMottagningAccessTest();
        user.changeValdVardenhet(VARDENHET_1);

        assertUserHasExpectedAccess();
    }

    @Test
    public void testUserHasNoReadOnlyAccessToParentVardEnhetWhenNORMAL() {
        WebCertUser user = setupUserMottagningAccessTest();
        user.changeValdVardenhet(MOTTAGNING_1);
        user.setOrigin(UserOriginType.NORMAL.name());

        assertFalse(webcertUserService.isAuthorizedForUnit(VARDENHET_1, true));
    }

    @Test
    public void testUserHasReadOnlyAccessToParentVardEnhetWhenDJUPINTEGRATION() {
        WebCertUser user = setupUserMottagningAccessTest();
        user.changeValdVardenhet(MOTTAGNING_1);
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        assertTrue(webcertUserService.isAuthorizedForUnit(VARDENHET_1, true));
    }

    @Test
    public void testUserHasReadOnlyAccessToSiblingMottagningWhenDJUPINTEGRATION() {
        WebCertUser user = setupUserMottagningAccessTest();
        user.changeValdVardenhet(MOTTAGNING_1);
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        assertTrue(webcertUserService.isAuthorizedForUnit(MOTTAGNING_2, true));
    }

    @Test
    public void testUserHasReadOnlyAccessToCousinMottagningWhenDJUPINTEGRATION() {
        WebCertUser user = setupUserMottagningAccessTest();
        user.changeValdVardenhet(MOTTAGNING_1);
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        assertTrue(webcertUserService.isAuthorizedForUnit(MOTTAGNING_2, true));
    }

    @Test
    public void testUserHasAccessToParentVardEnhetWhenDJUPINTEGRATION() {
        WebCertUser user = setupUserMottagningAccessTest();
        user.changeValdVardenhet(MOTTAGNING_1);
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        assertTrue(webcertUserService.isAuthorizedForUnit(VARDENHET_1, false));
    }

    @Test
    public void testUserHasAccessToSiblingMottagningWhenDJUPINTEGRATION() {
        WebCertUser user = setupUserMottagningAccessTest();
        user.changeValdVardenhet(MOTTAGNING_1);
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        assertTrue(webcertUserService.isAuthorizedForUnit(MOTTAGNING_2, false));
    }

    @Test
    public void testUserHasNoAccessToCousinMottagningWhenDJUPINTEGRATION() {
        WebCertUser user = setupUserMottagningAccessTest();
        user.changeValdVardenhet(MOTTAGNING_1);
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());

        assertFalse(webcertUserService.isAuthorizedForUnit(MOTTAGNING_3, false));
    }

    @Test
    public void testUserHasNoAccessToMottagningMissingVardenhetWhenDJUPINTEGRATION() {
        WebCertUser user = setupUserMottagningAccessTest();
        user.setValdVardenhet(buildMottagning1());
        user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        user.getVardgivare().remove(0);

        assertFalse(webcertUserService.isAuthorizedForUnit(MOTTAGNING_3, false));
    }

    @Test
    public void testLogout() {
        final var sessionId = "sessionId";
        final var session = mock(HttpSession.class);
        when(session.getId()).thenReturn(sessionId);

        webcertUserService.scheduleSessionRemoval(session);

        verify(session).setAttribute(eq(TIME_TO_INVALIDATE_ATTRIBUTE_NAME), anyLong());
    }

    @Test
    public void testLogoutCancel() {
        final var sessionId = "sessionId";
        final var session = mock(HttpSession.class);
        when(session.getId()).thenReturn(sessionId);

        webcertUserService.cancelScheduledLogout(session);

        verify(session).setAttribute(eq(TIME_TO_INVALIDATE_ATTRIBUTE_NAME), eq(null));
    }

    @Test
    public void testLogoutNow() {
        String sessionId = "sessionId";
        HttpSession session = mock(HttpSession.class);

        when(session.getId()).thenReturn(sessionId);

        webcertUserService.removeSessionNow(session);

        verify(sessionRepository).deleteById(sessionId);
        verify(session).invalidate();
        verify(session).setMaxInactiveInterval(0);
    }

    @Test
    public void testIsValdVardenhetMottagning() {
        WebCertUser user = createWebCertUser(false);
        Mottagning mottagning = new Mottagning(MOTTAGNING_1, "Mottagningen");
        mottagning.setParentHsaId(VARDENHET_1);
        ((Vardenhet) user.getValdVardenhet()).getMottagningar().add(mottagning);

        // After setup vald vardenhet is vg1ve1
        assertFalse(user.isValdVardenhetMottagning());

        // Change to our added mottagnings
        user.setValdVardenhet(((Vardenhet) user.getValdVardenhet()).getMottagningar().get(0));
        assertTrue(user.isValdVardenhetMottagning());

        user.setValdVardenhet(null);
        assertFalse(user.isValdVardenhetMottagning());
    }

    @Test
    public void shallReturnLoggedInWebcertUserWhenUserLoggedIn() {
        final var expected = LoggedInWebcertUser.builder()
            .staffId("HSA-id")
            .unitId("VG1VE1")
            .careProviderId("VG1")
            .role(AuthoritiesConstants.ROLE_LAKARE)
            .origin(UserOriginType.NORMAL.name())
            .build();

        final var webcertUser = createWebCertUser(false);
        applyUserToThreadLocalCtx(webcertUser);

        when(loggedInWebcertUserFactory.create(webcertUser)).thenReturn(expected);

        final var actual = webcertUserService.getLoggedInWebcertUser();
        assertEquals(expected, actual);
    }

    @Test
    public void shallReturnEmptyLoggedInWebcertUserWhenNoUserLoggedIn() {
        final var expected = LoggedInWebcertUser.builder()
            .build();

        applyUserToThreadLocalCtx(null);

        final var actual = webcertUserService.getLoggedInWebcertUser();
        assertEquals(expected, actual);
    }

    @Test
    public void shallReturnEmptyLoggedInWebcertUserWhenNoAuthenticationContextPresent() {
        final var expected = LoggedInWebcertUser.builder()
            .build();

        final var actual = webcertUserService.getLoggedInWebcertUser();
        assertEquals(expected, actual);
    }

    private WebCertUser setupUserMottagningAccessTest() {
        WebCertUser user = createWebCertUser(true);
        ((Vardenhet) user.getValdVardenhet()).getMottagningar().add(buildMottagning1());
        ((Vardenhet) user.getValdVardenhet()).getMottagningar().add(buildMottagning2());
        applyUserToThreadLocalCtx(user);
        return user;
    }

    private void assertUserHasExpectedAccess() {
        assertTrue(webcertUserService.isUserAllowedAccessToUnit(MOTTAGNING_1));
        assertTrue(webcertUserService.isUserAllowedAccessToUnit(MOTTAGNING_2));
        assertTrue(webcertUserService.isUserAllowedAccessToUnit(VARDENHET_1));
        assertFalse(webcertUserService.isUserAllowedAccessToUnit(VARDENHET_2));
    }

    private Mottagning buildMottagning1() {
        Mottagning mottagning = new Mottagning(MOTTAGNING_1, "Mottagningen");
        mottagning.setParentHsaId(VARDENHET_1);
        return mottagning;
    }

    private Mottagning buildMottagning2() {
        Mottagning mottagning = new Mottagning(MOTTAGNING_2, "Mottagningen 2");
        mottagning.setParentHsaId(VARDENHET_1);
        return mottagning;
    }

    private Map<String, String> buildMapOfAllUserPrefs() {
        Map<String, String> prefs = new HashMap<>();
        prefs.put("key1", "value1");
        prefs.put("key2", "value2");
        return prefs;
    }

    private void applyUserToThreadLocalCtx(final WebCertUser user) {
        Authentication auth = new AbstractAuthenticationToken(null) {
            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return user;
            }
        };
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private WebCertUser createWebCertUser(boolean fromJS) {

        WebCertUser user = buildUserPrincipal();

        user.setNamn("A Name");
        user.setHsaId("HSA-id");
        user.setForskrivarkod("Forskrivarkod");
        user.setAuthenticationScheme("AuthScheme");
        user.setSpecialiseringar(Arrays.asList("Kirurgi", "Ortopedi"));
        user.setBefattningar(Arrays.asList("Specialistläkare"));

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

        user.setMiuNamnPerEnhetsId(buildMiuMap());

        if (fromJS) {
            user.setOrigin(UserOriginType.DJUPINTEGRATION.name());
        } else {
            user.setOrigin(UserOriginType.NORMAL.name());
        }

        return user;
    }

    private Map<String, String> buildMiuMap() {
        Map<String, String> map = new HashMap<>();
        map.put(VARDENHET_1, "Mitt uppdrag");
        map.put(MOTTAGNING_1, "Mitt mottagningsuppdrag");
        return map;
    }

    private WebCertUser buildUserPrincipal() {
        Role role = AUTHORITIES_RESOLVER.getRole(AuthoritiesConstants.ROLE_LAKARE);

        WebCertUser user = new WebCertUser();
        user.setRoles(AuthoritiesResolverUtil.toMap(role));
        user.setAuthorities(AuthoritiesResolverUtil.toMap(role.getPrivileges(), Privilege::getName));

        return user;
    }

}