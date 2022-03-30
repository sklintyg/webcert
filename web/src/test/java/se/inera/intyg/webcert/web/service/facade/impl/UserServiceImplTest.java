/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.user.LoginMethod;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
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
    private static final Map<String, String> PREFERENCES = Map.of("wc.preference", "true");

    @BeforeEach
    void setUp() {
        user = mock(WebCertUser.class);

        doReturn(user)
            .when(webCertUserService)
            .getUser();

        doReturn(HSA_ID)
            .when(user)
            .getHsaId();

        doReturn(NAME)
            .when(user)
            .getNamn();

        doReturn(getNavigableCareProvider())
            .when(user)
            .getVardgivare();

        doReturn(getCareProvider())
            .when(user)
            .getValdVardgivare();

        doReturn(getUnit())
            .when(user)
            .getValdVardenhet();

        doReturn(PREFERENCES)
            .when(user)
            .getAnvandarPreference();
    }

    @Nested
    class TestAttributes {

        @BeforeEach
        void setUp() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();

            doReturn(ROLE)
                .when(user)
                .getRoleTypeName();
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
    }

    @Nested
    class Roles {

        @BeforeEach
        void setUp() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();

        }

        @Test
        void shallReturnWithRole() {
            doReturn(ROLE)
                .when(user)
                .getRoleTypeName();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(ROLE, actualUser.getRole());
        }

        @Test
        void shallReturnWithVardAdministratorRole() {
            doReturn("VARDADMINISTRATOR")
                .when(user)
                .getRoleTypeName();

            final var actualUser = userService.getLoggedInUser();
            assertEquals("Vårdadministratör", actualUser.getRole());
        }

        @Test
        void shallReturnRoleWithoutExtension() {
            doReturn("Läkare - inom EU")
                .when(user)
                .getRoleTypeName();

            final var actualUser = userService.getLoggedInUser();
            assertEquals("Läkare", actualUser.getRole());
        }
    }

    @Nested
    class SigningMethod {

        @BeforeEach
        void setUp() {
            doReturn(ROLE)
                .when(user)
                .getRoleTypeName();
        }

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
    }

    @Nested
    class LoginMethod {

        @BeforeEach
        void setUp() {
            doReturn(ROLE)
                    .when(user)
                    .getRoleTypeName();
        }

        @Test
        void shallReturnWithLoginMethodFake() {
            doReturn(AuthenticationMethod.FAKE)
                    .when(user)
                    .getAuthenticationMethod();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(se.inera.intyg.common.support.facade.model.user.LoginMethod.FAKE, actualUser.getLoginMethod());
        }

        @Test
        void shallReturnWithLoginMethodSiths() {
            doReturn(AuthenticationMethod.SITHS)
                    .when(user)
                    .getAuthenticationMethod();

            final var actualUser = userService.getLoggedInUser();
            assertEquals(se.inera.intyg.common.support.facade.model.user.LoginMethod.SITHS, actualUser.getLoginMethod());
        }
    }

    @Nested
    class InactiveUnitTests {

        @BeforeEach
        void setUp() {
            doReturn(AuthenticationMethod.SITHS)
                .when(user)
                .getAuthenticationMethod();

            doReturn(ROLE)
                .when(user)
                .getRoleTypeName();
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

    private List<Vardgivare> getNavigableCareProvider() {
        final var unit = (Mottagning) getUnit();

        final var careUnit = new Vardenhet();
        careUnit.setId(CARE_UNIT_ID);
        careUnit.setNamn(CARE_UNIT_NAME);
        careUnit.setMottagningar(List.of(unit));

        final var careProvider = (Vardgivare) getCareProvider();
        careProvider.setVardenheter(List.of(careUnit));

        return List.of(careProvider);
    }

    private SelectableVardenhet getCareProvider() {
        final var careProvider = new Vardgivare();
        careProvider.setId(CARE_PROVIDER_ID);
        careProvider.setNamn(CARE_PROVIDER_NAME);
        return careProvider;
    }

    private SelectableVardenhet getUnit() {
        final var unit = new Mottagning();
        unit.setId(UNIT_ID);
        unit.setNamn(UNIT_NAME);
        return unit;
    }

    private IntegrationParameters getParameters(Boolean inactiveUnit) {
        final var params = new IntegrationParameters(null, null, null, null,
            null, null, null, null, null,
            false, false, inactiveUnit, false);
        return params;
    }
}
