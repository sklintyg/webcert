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
package se.inera.intyg.webcert.web.service.facade.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static se.inera.intyg.common.support.facade.model.user.LoginMethod.BANK_ID;
import static se.inera.intyg.common.support.facade.model.user.LoginMethod.BANK_ID_MOBILE;
import static se.inera.intyg.common.support.facade.model.user.LoginMethod.FAKE;
import static se.inera.intyg.common.support.facade.model.user.LoginMethod.SITHS;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.facade.user.UserServiceImpl;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionAction;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private UserServiceImpl userService;

    private WebCertUser user;

    private static final String CARE_PROVIDER_NAME = "CARE_PROVIDER_NAME";
    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final String CARE_UNIT_NAME = "CARE_UNIT_NAME";
    private static final String CARE_UNIT_ID = "CARE_UNIT_ID";
    private static final String UNIT_NAME = "UNIT_NAME";
    private static final String UNIT_ID = "UNIT_ID";
    private static final String HSA_ID = "HSA_ID";
    private static final String NAME = "NAME";
    private static final String ROLE = "ROLE";
    private static final String ORIGIN = "ORIGIN";
    public static final String ROLE_NAME = "ROLE_NAME";
    public static final String ROLE_DESCRIPTION = "ROLE_DESCRIPTION";
    private static final Map<String, String> PREFERENCES = Map.of("wc.preference", "true");

    private static final String SORT_FIRST = "anakonda";
    private static final String SORT_SECOND = "Åkersork";
    private static final String SORT_THIRD = "Älgantilop";
    private static final List<String> UNSORTED_NAMES = List.of(SORT_THIRD, SORT_FIRST, SORT_SECOND);
    private static final String LAUNCH_ID = "97f279ba-7d2b-4b0a-8665-7adde08f26f4";
    private static final String LAUNCH_FROM_ORIGIN = "normal";

    @BeforeEach
    void setUp() {
        user = mock(WebCertUser.class);

        doReturn(user)
            .when(webCertUserService)
            .getUser();
    }

    @Nested
    class TestAttributes {

        @BeforeEach
        void setUp() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();

            doReturn(getCareProvider(CARE_PROVIDER_NAME))
                .when(user)
                .getValdVardgivare();

            doReturn(getUnit(UNIT_NAME))
                .when(user)
                .getValdVardenhet();

            doReturn(getNavigableCareProvider())
                .when(user)
                .getVardgivare();

            doReturn(HSA_ID)
                .when(user)
                .getHsaId();

            doReturn(NAME)
                .when(user)
                .getNamn();

            doReturn(PREFERENCES)
                .when(user)
                .getAnvandarPreference();

            doReturn(ORIGIN)
                .when(user)
                .getOrigin();
        }

        @Test
        void shallReturnWithHsaId() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(HSA_ID, actualUser.getHsaId());
        }

        @Test
        void shallReturnWithName() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(NAME, actualUser.getName());
        }

        @Test
        void shallReturnWithLoggedInUnitName() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(UNIT_NAME, actualUser.getLoggedInUnit().getUnitName());
        }

        @Test
        void shallReturnWithLoggedInUnitUnitId() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(UNIT_ID, actualUser.getLoggedInUnit().getUnitId());
        }

        @Test
        void shallReturnWithLoggedInCareProviderName() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(CARE_PROVIDER_NAME, actualUser.getLoggedInCareProvider().getUnitName());
        }

        @Test
        void shallReturnWithLoggedInCareProviderUnitId() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(CARE_PROVIDER_ID, actualUser.getLoggedInCareProvider().getUnitId());
        }

        @Test
        void shallReturnWithLoggedInCareUnitName() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(CARE_UNIT_NAME, actualUser.getLoggedInCareUnit().getUnitName());
        }

        @Test
        void shallReturnWithLoggedInCareUnitId() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(CARE_UNIT_ID, actualUser.getLoggedInCareUnit().getUnitId());
        }

        @Test
        void shallReturnProtectedPerson() {
            doReturn(true)
                .when(user)
                .isSekretessMarkerad();
            final var actualUser = userService.getLoggedInUser();
            assertTrue(actualUser.isProtectedPerson());
        }

        @Test
        void shallReturnNotProtectedPerson() {
            final var actualUser = userService.getLoggedInUser();
            assertFalse(actualUser.isProtectedPerson());
        }

        @Test
        void shallReturnUserPreferences() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(PREFERENCES, actualUser.getPreferences());
        }

        @Test
        void shallReturnMapCareProviders() {
            final var actualUser = userService.getLoggedInUser();
            final var careProvider = actualUser.getCareProviders().get(0);
            final var careUnit = careProvider.getCareUnits().get(0);
            final var unit = careUnit.getUnits().get(0);

            assertEquals(CARE_PROVIDER_ID, careProvider.getId());
            assertEquals(CARE_UNIT_ID, careUnit.getUnitId());
            assertEquals(UNIT_ID, unit.getUnitId());
        }

        @Test
        void shallReturnOrigin() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(ORIGIN, actualUser.getOrigin());
        }
    }

    @Nested
    class UserWithNoSelectedLoginUnits {

        @BeforeEach
        void setUp() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();

            doReturn(null)
                .when(user)
                .getValdVardenhet();
        }

        @Test
        void shallReturnUnsetUnitWhenUserWithNoSelectedLoginUnit() {
            final var actualUser = userService.getLoggedInUser();

            assertAll(
                () -> assertNull(actualUser.getLoggedInUnit().getUnitName()),
                () -> assertNull(actualUser.getLoggedInUnit().getUnitId())
            );
        }

        @Test
        void shallReturnUnsetCareUnitWhenUserWithNoSelectedLoginUnit() {
            final var actualUser = userService.getLoggedInUser();

            assertAll(
                () -> assertNull(actualUser.getLoggedInCareUnit().getUnitName()),
                () -> assertNull(actualUser.getLoggedInCareUnit().getUnitId())
            );
        }

        @Test
        void shallReturnUnsetCareProviderWhenUserWithNoSelectedLoginUnit() {
            final var actualUser = userService.getLoggedInUser();

            assertAll(
                () -> assertNull(actualUser.getLoggedInCareProvider().getUnitName()),
                () -> assertNull(actualUser.getLoggedInCareProvider().getUnitId())
            );
        }
    }

    @Nested
    class Roles {

        @Test
        void shallReturnRoleDescription() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();

            doReturn(getRole())
                .when(user)
                .getRoles();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(ROLE_DESCRIPTION, actualUser.getRole());
        }

        @Test
        void shallReturnMissingRoleDescriptionWhenUserRoleIsNull() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();

            doReturn(null)
                .when(user)
                .getRoles();

            final var actualUser = userService.getLoggedInUser();
            assertEquals("Roll ej angiven", actualUser.getRole());
        }

        @Test
        void shallReturnMissingRoleDescriptionWhenUserHasNoRoles() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();

            doReturn(Collections.emptyMap())
                .when(user)
                .getRoles();

            final var actualUser = userService.getLoggedInUser();
            assertEquals("Roll ej angiven", actualUser.getRole());
        }

        @Test
        void shallThrowNullpointerExceptionIfRolesMapHasNullValues() {
            final var nullValueRoleMap = new HashMap<String, Role>();
            nullValueRoleMap.put(ROLE, null);
            doReturn(nullValueRoleMap)
                .when(user)
                .getRoles();

            final var exception = assertThrows(NullPointerException.class, () -> userService.getLoggedInUser());
            assertEquals(NullPointerException.class, exception.getClass());
        }
    }

    @Nested
    class SigningMethod {

        @Test
        void shallReturnWithSigningMethodFake() {
            doReturn(AuthenticationMethod.FAKE)
                .when(user)
                .getAuthenticationMethod();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(se.inera.intyg.common.support.facade.model.user.SigningMethod.FAKE, actualUser.getSigningMethod());
        }

        @Test
        void shallReturnWithSigningMethodDSS() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(se.inera.intyg.common.support.facade.model.user.SigningMethod.DSS, actualUser.getSigningMethod());
        }

        @Test
        void shallReturnWithSigningMethodBankId() {
            doReturn(AuthenticationMethod.MOBILT_BANK_ID)
                .when(user)
                .getAuthenticationMethod();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(se.inera.intyg.common.support.facade.model.user.SigningMethod.MOBILT_BANK_ID, actualUser.getSigningMethod());
        }
    }

    @Nested
    class LoginMethod {

        @Test
        void shallReturnWithLoginMethodFake() {
            doReturn(AuthenticationMethod.FAKE)
                .when(user)
                .getAuthenticationMethod();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(FAKE, actualUser.getLoginMethod());
        }

        @Test
        void shallReturnWithLoginMethodSiths() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(SITHS, actualUser.getLoginMethod());
        }

        @Test
        void shallReturnWithLoginMethodBankId() {
            doReturn(AuthenticationMethod.BANK_ID)
                .when(user)
                .getAuthenticationMethod();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(BANK_ID, actualUser.getLoginMethod());
        }

        @Test
        void shallReturnWithLoginMethodBankIdMobile() {
            doReturn(AuthenticationMethod.MOBILT_BANK_ID)
                .when(user)
                .getAuthenticationMethod();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(BANK_ID_MOBILE, actualUser.getLoginMethod());
        }
    }

    @Nested
    class InactiveUnitTests {

        @BeforeEach
        void setUp() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();
        }

        @Test
        void shallReturnWithActiveLoggedInUnit() {
            doReturn(getParameters(false))
                .when(user)
                .getParameters();

            final var actualUser = userService.getLoggedInUser();
            assertFalse(actualUser.getLoggedInUnit().getIsInactive());
        }

        @Test
        void shallReturnWithInactiveLoggedInUnit() {
            doReturn(getParameters(true))
                .when(user)
                .getParameters();

            final var actualUser = userService.getLoggedInUser();
            assertTrue(actualUser.getLoggedInUnit().getIsInactive());
        }
    }

    @Nested
    class SortCareUnitsAlphabeticallyTests {

        @BeforeEach
        void setUp() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user).getAuthenticationMethod();

            doReturn(getUnsortedCareProviders())
                .when(user).getVardgivare();
        }

        @Test
        public void shallSortCareProviders() {
            final var actualUser = userService.getLoggedInUser();

            final var careProviders = actualUser.getCareProviders();
            assertAll(
                () -> assertEquals(SORT_FIRST, careProviders.get(0).getName()),
                () -> assertEquals(SORT_SECOND, careProviders.get(1).getName()),
                () -> assertEquals(SORT_THIRD, careProviders.get(2).getName())
            );
        }

        @Test
        public void shallSortCareUnits() {
            final var actualUser = userService.getLoggedInUser();

            final var careUnits = actualUser.getCareProviders().get(0).getCareUnits();
            assertAll(
                () -> assertEquals(SORT_FIRST, careUnits.get(0).getUnitName()),
                () -> assertEquals(SORT_SECOND, careUnits.get(1).getUnitName()),
                () -> assertEquals(SORT_THIRD, careUnits.get(2).getUnitName())
            );
        }

        @Test
        public void shallSortUnits() {
            final var actualUser = userService.getLoggedInUser();

            final var units = actualUser.getCareProviders().get(0).getCareUnits().get(0).getUnits();
            assertAll(
                () -> assertEquals(SORT_FIRST, units.get(0).getUnitName()),
                () -> assertEquals(SORT_SECOND, units.get(1).getUnitName()),
                () -> assertEquals(SORT_THIRD, units.get(2).getUnitName())
            );
        }
    }

    @Nested
    class Subscriptions {

        @BeforeEach
        void setUp() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user).getAuthenticationMethod();

            doReturn(getNavigableCareProvider())
                .when(user)
                .getVardgivare();
        }

        @Test
        void careProviderIsNotConsideredAsMissingSubscription() {
            final var loggedInUser = userService.getLoggedInUser();
            assertFalse(loggedInUser.getCareProviders().get(0).isMissingSubscription());
        }
        
        @Test
        void careProviderIsNotConsideredAsMissingSubscriptionIfActionIsNone() {
            final var subscriptionInfo = new SubscriptionInfo();
            subscriptionInfo.setCareProvidersMissingSubscription(
                Collections.singletonList(CARE_PROVIDER_ID)
            );
            subscriptionInfo.setSubscriptionAction(SubscriptionAction.NONE);

            doReturn(subscriptionInfo).when(user).getSubscriptionInfo();

            final var loggedInUser = userService.getLoggedInUser();
            assertFalse(loggedInUser.getCareProviders().get(0).isMissingSubscription());
        }

        @Test
        void careProviderIsConsideredMissingSubscriptionIfActionIsBlock() {
            final var subscriptionInfo = new SubscriptionInfo();
            subscriptionInfo.setCareProvidersMissingSubscription(
                Collections.singletonList(CARE_PROVIDER_ID)
            );
            subscriptionInfo.setSubscriptionAction(SubscriptionAction.BLOCK);

            doReturn(subscriptionInfo).when(user).getSubscriptionInfo();

            final var loggedInUser = userService.getLoggedInUser();
            assertTrue(loggedInUser.getCareProviders().get(0).isMissingSubscription());
        }
    }

    @Nested
    class LaunchFromOrigin {

        @BeforeEach
        void setUp() {
            doReturn(AuthenticationMethod.FAKE)
                .when(user)
                .getAuthenticationMethod();
        }

        @Test
        void shouldReturnUserWithLaunchFromOrigin() {
            doReturn(LAUNCH_FROM_ORIGIN)
                .when(user).getLaunchFromOrigin();

            final var loggedInUser = userService.getLoggedInUser();

            assertEquals(loggedInUser.getLaunchFromOrigin(), LAUNCH_FROM_ORIGIN);
        }

        @Test
        void shouldNotReturnUserWithLaunchFromOrigin() {
            final var loggedInUser = userService.getLoggedInUser();

            assertNull(loggedInUser.getLaunchFromOrigin());
        }
    }

    @Nested
    class LaunchId {

        @BeforeEach
        void setUp() {
            doReturn(AuthenticationMethod.FAKE)
                .when(user)
                .getAuthenticationMethod();
        }

        @Test
        void shouldReturnUserWithLaunchId() {
            doReturn(getParameters(LAUNCH_ID))
                .when(user).getParameters();

            final var loggedInUser = userService.getLoggedInUser();

            assertEquals(loggedInUser.getLaunchId(), LAUNCH_ID);
        }

        @Test
        void shouldNotReturnUserWithLaunchId() {
            final var loggedInUser = userService.getLoggedInUser();

            assertNull(loggedInUser.getLaunchId());
        }

        @Test
        void shouldNotReturnUserWithLaunchIdIfItsNotAddedInIntegrationsParameters() {
            doReturn(getParameters(true))
                .when(user).getParameters();

            final var loggedInUser = userService.getLoggedInUser();

            assertNull(loggedInUser.getLaunchId());
        }
    }

    private IntegrationParameters getParameters(String launchId) {
        return new IntegrationParameters(null, null, null, null,
            null, null, null, null, null,
            false, false, false, false, launchId);
    }

    private List<Vardgivare> getNavigableCareProvider() {
        final var unit = (Mottagning) getUnit(UNIT_NAME);

        final var careUnit = new Vardenhet();
        careUnit.setId(CARE_UNIT_ID);
        careUnit.setNamn(CARE_UNIT_NAME);
        careUnit.setMottagningar(List.of(unit));

        final var careProvider = (Vardgivare) getCareProvider(CARE_PROVIDER_NAME);
        careProvider.setId(CARE_PROVIDER_ID);
        careProvider.setVardenheter(List.of(careUnit));

        return List.of(careProvider);
    }

    private SelectableVardenhet getCareProvider(String careProviderName) {
        final var careProvider = new Vardgivare();
        careProvider.setId(CARE_PROVIDER_ID);
        careProvider.setNamn(careProviderName);
        return careProvider;
    }

    private SelectableVardenhet getCareUnit(String careUnitName) {
        final var careUnit = new Vardenhet();
        careUnit.setId(UNIT_ID);
        careUnit.setNamn(careUnitName);
        return careUnit;
    }

    private SelectableVardenhet getUnit(String unitName) {
        final var unit = new Mottagning();
        unit.setId(UNIT_ID);
        unit.setNamn(unitName);
        return unit;
    }

    private Map<String, Role> getRole() {
        final var role = new Role();
        role.setName(ROLE_NAME);
        role.setDesc(ROLE_DESCRIPTION);
        role.setPrivileges(Collections.emptyList());
        return Collections.singletonMap(ROLE, role);
    }

    private IntegrationParameters getParameters(Boolean inactiveUnit) {
        return new IntegrationParameters(null, null, null, null,
            null, null, null, null, null,
            false, false, inactiveUnit, false, null);
    }

    private List<Vardgivare> getUnsortedCareProviders() {
        final var units = UNSORTED_NAMES.stream()
            .map(name -> (Mottagning) getUnit(name))
            .collect(Collectors.toList());
        final var careUnits = UNSORTED_NAMES.stream()
            .map(name -> (Vardenhet) getCareUnit(name))
            .collect(Collectors.toList());
        final var careProviders = UNSORTED_NAMES.stream()
            .map(name -> (Vardgivare) getCareProvider(name))
            .collect(Collectors.toList());
        careUnits.forEach(careUnit -> careUnit.setMottagningar(units));
        careProviders.forEach(careProvider -> careProvider.setVardenheter(careUnits));

        return careProviders;
    }
}
