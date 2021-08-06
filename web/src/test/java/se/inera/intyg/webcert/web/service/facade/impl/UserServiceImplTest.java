/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private WebCertUserService webCertUserService;

    @InjectMocks
    private UserServiceImpl userService;

    private WebCertUser user;

    private static final String CARE_PROVIDER_NAME = "CARE_PROVIDER_NAME";
    private static final String CARE_UNIT_NAME = "CARE_UNIT_NAME";
    private static final String HSA_ID = "HSA_ID";
    private static final String NAME = "NAME";
    private static final String ROLE = "ROLE";

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

        final var careProvider = mock(SelectableVardenhet.class);
        doReturn(CARE_PROVIDER_NAME)
            .when(careProvider)
            .getNamn();

        final var careUnit = mock(SelectableVardenhet.class);
        doReturn(CARE_UNIT_NAME)
            .when(careUnit)
            .getNamn();

        doReturn(careProvider)
            .when(user)
            .getValdVardgivare();

        doReturn(careUnit)
            .when(user)
            .getValdVardenhet();
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
        void shallReturnWithLoggedInUnit() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(CARE_UNIT_NAME, actualUser.getLoggedInUnit());
        }

        @Test
        void shallReturnWithLoggedInCareProvider() {
            final var actualUser = userService.getLoggedInUser();
            assertEquals(CARE_PROVIDER_NAME, actualUser.getLoggedInCareProvider());
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
}