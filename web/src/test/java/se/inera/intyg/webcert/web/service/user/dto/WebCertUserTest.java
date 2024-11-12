/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.user.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.webcert.web.auth.common.AuthConstants;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class WebCertUserTest {

    @Nested
    class SjfTest {

        @Test
        void shallReturnTrueIfParameterIsNotNullAndSjfIsTrue() {
            final var integrationParameters = mock(IntegrationParameters.class);
            final var webCertUser = new WebCertUser();
            webCertUser.setParameters(integrationParameters);

            doReturn(true).when(integrationParameters).isSjf();
            assertTrue(webCertUser.isSjfActive());
        }

        @Test
        void shallReturnFalseIfParameterIsNotNullAndSjfIsFalse() {
            final var integrationParameters = mock(IntegrationParameters.class);
            final var webCertUser = new WebCertUser();
            webCertUser.setParameters(integrationParameters);

            doReturn(false).when(integrationParameters).isSjf();
            assertFalse(webCertUser.isSjfActive());
        }

        @Test
        void shallReturnFalseIfParameterIsNull() {
            final var webCertUser = new WebCertUser();
            assertFalse(webCertUser.isSjfActive());
        }
    }

    @Nested
    class IsInactiveUnitTest {

        @Test
        void shallReturnTrueIfParameterIsNotNullAndInactiveUnitTrue() {
            final var integrationParameters = mock(IntegrationParameters.class);
            final var webCertUser = new WebCertUser();
            webCertUser.setParameters(integrationParameters);

            doReturn(true).when(integrationParameters).isInactiveUnit();
            assertTrue(webCertUser.isUnitInactive());
        }

        @Test
        void shallReturnFalseIfParameterIsNotNullAndInactiveUnitIsFalse() {
            final var integrationParameters = mock(IntegrationParameters.class);
            final var webCertUser = new WebCertUser();
            webCertUser.setParameters(integrationParameters);

            doReturn(false).when(integrationParameters).isInactiveUnit();
            assertFalse(webCertUser.isUnitInactive());
        }

        @Test
        void shallReturnFalseIfParameterIsNull() {
            final var webCertUser = new WebCertUser();
            assertFalse(webCertUser.isUnitInactive());
        }
    }

    @Nested
    class RelyingPartyRegistrationId {

        @Test
        void shouldReturnRegistrationIdElegForAuthMethodBankId() {
            final var integrationParameters = mock(IntegrationParameters.class);
            final var webCertUser = new WebCertUser();
            webCertUser.setAuthenticationMethod(AuthenticationMethod.BANK_ID);
            assertEquals(AuthConstants.REGISTRATION_ID_ELEG, webCertUser.getRelyingPartyRegistrationId());
        }

        @Test
        void shouldReturnRegistrationIdElegForAuthMethodMobiltBankId() {
            final var integrationParameters = mock(IntegrationParameters.class);
            final var webCertUser = new WebCertUser();
            webCertUser.setAuthenticationMethod(AuthenticationMethod.MOBILT_BANK_ID);
            assertEquals(AuthConstants.REGISTRATION_ID_ELEG, webCertUser.getRelyingPartyRegistrationId());
        }

        @Test
        void shouldReturnRegistrationIdSithsNormalForAuthMethodSiths() {
            final var integrationParameters = mock(IntegrationParameters.class);
            final var webCertUser = new WebCertUser();
            webCertUser.setAuthenticationMethod(AuthenticationMethod.SITHS);
            assertEquals(AuthConstants.REGISTRATION_ID_SITHS_NORMAL, webCertUser.getRelyingPartyRegistrationId());
        }

        @Test
        void shouldReturnRegistrationIdSithsNormalForAuthMethodNetId() {
            final var integrationParameters = mock(IntegrationParameters.class);
            final var webCertUser = new WebCertUser();
            webCertUser.setAuthenticationMethod(AuthenticationMethod.NET_ID);
            assertEquals(AuthConstants.REGISTRATION_ID_SITHS_NORMAL, webCertUser.getRelyingPartyRegistrationId());
        }

        @Test
        void shouldThrowIllegalStateExceptionForAuthMethodFake() {
            final var integrationParameters = mock(IntegrationParameters.class);
            final var webCertUser = new WebCertUser();
            webCertUser.setAuthenticationMethod(AuthenticationMethod.FAKE);
            assertThrows(IllegalStateException.class, webCertUser::getRelyingPartyRegistrationId);
        }
    }
}
