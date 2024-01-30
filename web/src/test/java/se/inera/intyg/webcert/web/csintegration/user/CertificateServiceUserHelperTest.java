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

package se.inera.intyg.webcert.web.csintegration.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.user.User;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.webcert.web.service.facade.user.UserService;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class CertificateServiceUserHelperTest {

    private static final String ID = "ID";

    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";

    private static final List<String> HAS_SUBSCRIPTION = List.of("NOT_THIS_ID");
    private static final List<String> HAS_NOT_SUBSCRIPTION = List.of(CARE_PROVIDER_ID);

    private static User user;
    private static WebCertUser webCertUser;
    private static SubscriptionInfo subscription;

    @Mock
    UserService userService;

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    AuthoritiesHelper authoritiesHelper;

    @InjectMocks
    CertificateServiceUserHelper certificateServiceUserHelper;

    @BeforeEach
    void setup() {
        user = mock(User.class);
        webCertUser = mock(WebCertUser.class);
        subscription = mock(SubscriptionInfo.class);

        when(user.getHsaId())
            .thenReturn(ID);

        when(user.getRole())
            .thenReturn("");

        when(userService.getLoggedInUser())
            .thenReturn(user);
        when(webCertUserService.getUser())
            .thenReturn(webCertUser);

        when(webCertUser.getOrigin())
            .thenReturn("DJUPINTEGRATION");
    }

    @Test
    void shouldReturnUserWithId() {
        final var response = certificateServiceUserHelper.get();

        assertEquals(ID, response.getId());
    }

    @Nested
    class Blocked {

        @Test
        void shouldReturnBlockedFalseIfOriginIsNotNormal() {
            final var response = certificateServiceUserHelper.get();

            assertFalse(response.isBlocked());
        }

        @Nested
        class OriginNormal {

            private SelectableVardenhet careProvider;

            @BeforeEach
            void setup() {
                careProvider = mock(SelectableVardenhet.class);
                when(webCertUser.getOrigin())
                    .thenReturn("NORMAL");

                when(webCertUser.getSubscriptionInfo())
                    .thenReturn(subscription);

                when(careProvider.getId())
                    .thenReturn(CARE_PROVIDER_ID);
                when(webCertUser.getValdVardgivare())
                    .thenReturn(careProvider);
            }

            @Test
            void shouldReturnBlockedFalseIfSubscriptionExistsAndNotBlocked() {
                when(subscription.getCareProvidersMissingSubscription())
                    .thenReturn(HAS_SUBSCRIPTION);
                when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL)).thenReturn(false);

                final var response = certificateServiceUserHelper.get();

                assertFalse(response.isBlocked());
            }

            @Test
            void shouldReturnBlockedTrueIfSubscriptionIsMissing() {
                when(subscription.getCareProvidersMissingSubscription())
                    .thenReturn(HAS_NOT_SUBSCRIPTION);

                final var response = certificateServiceUserHelper.get();

                assertTrue(response.isBlocked());
            }

            @Test
            void shouldReturnBlockedTrueIfBlocked() {
                when(subscription.getCareProvidersMissingSubscription())
                    .thenReturn(HAS_SUBSCRIPTION);
                when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL)).thenReturn(true);

                final var response = certificateServiceUserHelper.get();

                assertTrue(response.isBlocked());
            }
        }
    }

    @Nested
    class Role {

        @Test
        void shouldReturnUserWithRoleDoctorFromDoctor() {
            when(user.getRole())
                .thenReturn("DOCTOR");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.DOCTOR, response.getRole());
        }

        @Test
        void shouldReturnUserWithRoleDoctorFromLakare() {
            when(user.getRole())
                .thenReturn("LAKARE");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.DOCTOR, response.getRole());
        }

        @Test
        void shouldReturnUserWithRoleAdministratorFromAdministrator() {
            when(user.getRole())
                .thenReturn("ADMINISTRATOR");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.CARE_ADMIN, response.getRole());
        }

        @Test
        void shouldReturnUserWithRoleAdministratorFromVardadmin() {
            when(user.getRole())
                .thenReturn("VARDADMIN");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.CARE_ADMIN, response.getRole());
        }

        @Test
        void shouldReturnUserWithRoleNurseFromNurse() {
            when(user.getRole())
                .thenReturn("NURSE");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.NURSE, response.getRole());
        }

        @Test
        void shouldReturnUserWithRoleNurseFromSjukskoterska() {
            when(user.getRole())
                .thenReturn("SJUKSKOTERSKA");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.NURSE, response.getRole());
        }

        @Test
        void shouldReturnUserWithRoleMidwifeFromMidwife() {
            when(user.getRole())
                .thenReturn("MIDWIFE");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.MIDWIFE, response.getRole());
        }

        @Test
        void shouldReturnUserWithRoleMidwfeFromBarnmorska() {
            when(user.getRole())
                .thenReturn("BARNMORSKA");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.MIDWIFE, response.getRole());
        }

        @Test
        void shouldReturnUserWithRoleDentistFromDentist() {
            when(user.getRole())
                .thenReturn("DENTIST");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.DENTIST, response.getRole());
        }

        @Test
        void shouldReturnUserWithRoleDentistFromTandlakare() {
            when(user.getRole())
                .thenReturn("TANDLAKARE");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.DENTIST, response.getRole());
        }

        @Test
        void shouldReturnUserWithRoleUnknown() {
            when(user.getRole())
                .thenReturn("");

            final var response = certificateServiceUserHelper.get();

            assertEquals(CertificateServiceUserRole.UNKNOWN, response.getRole());
        }

    }
}