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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.services.BefattningService;
import se.inera.intyg.infra.integration.hsatk.model.legacy.SelectableVardenhet;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.subscription.dto.SubscriptionInfo;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.integration.dto.IntegrationParameters;

@ExtendWith(MockitoExtension.class)
class CertificateServiceUserHelperTest {

    private static final String ID = "ID";

    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";

    private static final List<String> HAS_SUBSCRIPTION = List.of("NOT_THIS_ID");
    private static final List<String> HAS_NOT_SUBSCRIPTION = List.of(CARE_PROVIDER_ID);
    private static final List<String> SPECIALITIES = List.of("SpecialityOne", "SpecialityTwo");
    private static final String CODE_ONE = "CODE_ONE";
    private static final String DESCRIPTION_ONE = "DESCRIPTION_ONE";
    private static final String CODE_TWO = "CODE_TWO";
    private static final String DESCRIPTION_TWO = "DESCRIPTION_TWO";
    private static final List<String> PA_TITLES = List.of(CODE_ONE, CODE_TWO);

    private static WebCertUser webCertUser;
    private static SubscriptionInfo subscription;
    private IntegrationParameters parameters;


    @Mock
    WebCertUserService webCertUserService;

    @Mock
    AuthoritiesHelper authoritiesHelper;

    @InjectMocks
    CertificateServiceUserHelper certificateServiceUserHelper;

    @BeforeEach
    void setup() {
        webCertUser = mock(WebCertUser.class);
        subscription = mock(SubscriptionInfo.class);

        when(webCertUser.getHsaId())
            .thenReturn(ID);

        when(webCertUserService.getUser())
            .thenReturn(webCertUser);

        when(webCertUser.getOrigin())
            .thenReturn("DJUPINTEGRATION");

        when(webCertUser.getSpecialiseringar())
            .thenReturn(SPECIALITIES);
    }

    @Nested
    class HasRole {

        private final Map<String, Role> roles = new HashMap<>();

        @BeforeEach
        void setup() {
            final var role = new Role();
            role.setName(AuthoritiesConstants.ROLE_LAKARE);
            roles.put("FIRST", role);
            when(webCertUser.getRoles())
                .thenReturn(roles);
        }

        @Test
        void shouldReturnUserWithId() {
            final var response = certificateServiceUserHelper.get();

            assertEquals(ID, response.getId());
        }

        @Test
        void shouldReturnUserWithSpecialities() {
            final var response = certificateServiceUserHelper.get();

            assertEquals(SPECIALITIES, response.getSpecialities());
        }

        @Nested
        class Blocked {

            @Test
            void shouldReturnBlockedFalseIfOriginIsNotNormal() {
                final var response = certificateServiceUserHelper.get();

                assertFalse(response.getBlocked());
            }

            @Nested
            class OriginNormal {

                @BeforeEach
                void setup() {
                    final var careProvider = mock(SelectableVardenhet.class);
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

                    assertFalse(response.getBlocked());
                }

                @Test
                void shouldReturnBlockedTrueIfSubscriptionIsMissing() {
                    when(subscription.getCareProvidersMissingSubscription())
                        .thenReturn(HAS_NOT_SUBSCRIPTION);

                    final var response = certificateServiceUserHelper.get();

                    assertTrue(response.getBlocked());
                }

                @Test
                void shouldReturnBlockedTrueIfBlocked() {
                    when(subscription.getCareProvidersMissingSubscription())
                        .thenReturn(HAS_SUBSCRIPTION);
                    when(authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_ENABLE_BLOCK_ORIGIN_NORMAL)).thenReturn(true);

                    final var response = certificateServiceUserHelper.get();

                    assertTrue(response.getBlocked());
                }
            }
        }

        @Nested
        class TestRole {

            @Test
            void shouldReturnUserWithRoleDoctorFromDoctor() {
                addRole(AuthoritiesConstants.ROLE_LAKARE);

                final var response = certificateServiceUserHelper.get();

                assertEquals(CertificateServiceUserRole.DOCTOR, response.getRole());
            }

            @Test
            void shouldReturnUserWithRolePrivateDoctorFromPrivatlakare() {
                addRole(AuthoritiesConstants.ROLE_PRIVATLAKARE);

                final var response = certificateServiceUserHelper.get();

                assertEquals(CertificateServiceUserRole.PRIVATE_DOCTOR, response.getRole());
            }

            @Test
            void shouldReturnUserWithRoleAdministratorFromAdministrator() {
                addRole(AuthoritiesConstants.ROLE_ADMIN);

                final var response = certificateServiceUserHelper.get();

                assertEquals(CertificateServiceUserRole.CARE_ADMIN, response.getRole());
            }

            @Test
            void shouldReturnUserWithRoleNurseFromSjukskoterska() {
                addRole(AuthoritiesConstants.ROLE_SJUKSKOTERSKA);

                final var response = certificateServiceUserHelper.get();

                assertEquals(CertificateServiceUserRole.NURSE, response.getRole());
            }

            @Test
            void shouldReturnUserWithRoleMidwifeFromBarnmorska() {
                addRole(AuthoritiesConstants.ROLE_BARNMORSKA);

                final var response = certificateServiceUserHelper.get();

                assertEquals(CertificateServiceUserRole.MIDWIFE, response.getRole());
            }

            @Test
            void shouldReturnUserWithRoleDentistFromTandlakare() {
                addRole(AuthoritiesConstants.ROLE_TANDLAKARE);

                final var response = certificateServiceUserHelper.get();

                assertEquals(CertificateServiceUserRole.DENTIST, response.getRole());
            }

            @Test
            void shouldThrowExceptionIfUnknownRole() {
                addRole("");

                assertThrows(IllegalArgumentException.class, () -> certificateServiceUserHelper.get());
            }
        }

        @Nested
        class PaTitles {

            @Test
            void shouldReturnPaTitlesWhenDescriptionExists() {
                final var expectedPaTitles = List.of(
                    PaTitleDTO.builder()
                        .code(CODE_ONE)
                        .description(DESCRIPTION_ONE)
                        .build(),
                    PaTitleDTO.builder()
                        .code(CODE_TWO)
                        .description(DESCRIPTION_TWO)
                        .build()
                );

                when(webCertUser.getBefattningar())
                    .thenReturn(PA_TITLES);

                try (MockedStatic<BefattningService> utilities = Mockito.mockStatic(BefattningService.class)) {
                    utilities.when(() -> BefattningService.getDescriptionFromCode(CODE_ONE)).thenReturn(Optional.of(DESCRIPTION_ONE));
                    utilities.when(() -> BefattningService.getDescriptionFromCode(CODE_TWO)).thenReturn(Optional.of(DESCRIPTION_TWO));

                    final var response = certificateServiceUserHelper.get();

                    assertEquals(expectedPaTitles, response.getPaTitles());
                }
            }

            @Test
            void shouldReturnPaTitlesWhenDescriptionDoesntExists() {
                final var expectedPaTitles = List.of(
                    PaTitleDTO.builder()
                        .code(CODE_ONE)
                        .description(CODE_ONE)
                        .build(),
                    PaTitleDTO.builder()
                        .code(CODE_TWO)
                        .description(CODE_TWO)
                        .build()
                );

                when(webCertUser.getBefattningar())
                    .thenReturn(PA_TITLES);

                try (MockedStatic<BefattningService> utilities = Mockito.mockStatic(BefattningService.class)) {
                    utilities.when(() -> BefattningService.getDescriptionFromCode(CODE_ONE)).thenReturn(Optional.empty());
                    utilities.when(() -> BefattningService.getDescriptionFromCode(CODE_TWO)).thenReturn(Optional.empty());

                    final var response = certificateServiceUserHelper.get();

                    assertEquals(expectedPaTitles, response.getPaTitles());
                }
            }

            @Test
            void shouldReturnEmptyPaTitles() {
                final var expectedPaTitles = Collections.emptyList();

                when(webCertUser.getBefattningar())
                    .thenReturn(Collections.emptyList());

                final var response = certificateServiceUserHelper.get();

                assertEquals(expectedPaTitles, response.getPaTitles());
            }
        }

        @Nested
        class IsSjfTest {

            @BeforeEach
            void setUp() {
                parameters = mock(IntegrationParameters.class);
            }

            @Test
            void shouldSetSjfToTrue() {
                when(webCertUser.getParameters()).thenReturn(parameters);
                when(parameters.isSjf()).thenReturn(true);
                final var response = certificateServiceUserHelper.get();
                assertTrue(response.getSjf());
            }

            @Test
            void shouldSetSjfToFalse() {
                when(webCertUser.getParameters()).thenReturn(parameters);
                when(parameters.isSjf()).thenReturn(false);
                final var response = certificateServiceUserHelper.get();
                assertFalse(response.getSjf());
            }

            @Test
            void shouldSetSjfToNull() {
                final var response = certificateServiceUserHelper.get();
                assertNull(response.getSjf());
            }
        }

        private void addRole(String roleName) {
            roles.remove("FIRST");
            final var role = new Role();
            role.setName(roleName);
            roles.put("ROLE", role);
        }
    }
}
