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

package se.inera.intyg.webcert.web.csintegration.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.services.BefattningService;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Feature;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.Role;

@ExtendWith(MockitoExtension.class)
class CertificateServiceIntegrationUserHelperTest {

    private static final String ID = "ID";
    private static final List<String> SPECIALITIES = List.of("SpecialityOne", "SpecialityTwo");
    private static final List<String> PROFESSIONAL_LICENCES = List.of("RoleOne", "RoleTwo");
    private static final String CODE_ONE = "CODE_ONE";
    private static final String DESCRIPTION_ONE = "DESCRIPTION_ONE";
    private static final String CODE_TWO = "CODE_TWO";
    private static final String DESCRIPTION_TWO = "DESCRIPTION_TWO";
    private static final List<String> PA_TITLES = List.of(CODE_ONE, CODE_TWO);
    private static IntygUser intygUser;

    @InjectMocks
    CertificateServiceIntegrationUserHelper certificateServiceIntegrationUserHelper;

    @BeforeEach
    void setup() {
        intygUser = mock(IntygUser.class);

        when(intygUser.getHsaId())
            .thenReturn(ID);

        when(intygUser.getSpecialiseringar())
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
            when(intygUser.getRoles())
                .thenReturn(roles);
        }

        @Test
        void shouldReturnUserWithId() {
            final var response = certificateServiceIntegrationUserHelper.get(intygUser);

            assertEquals(ID, response.getId());
        }

        @Test
        void shallReturnUserWithFirstName() {
            final var expectedName = "expectedName";
            doReturn(expectedName).when(intygUser).getFornamn();

            final var response = certificateServiceIntegrationUserHelper.get(intygUser);
            assertEquals(expectedName, response.getFirstName());
        }

        @Test
        void shallReturnUserWithLastName() {
            final var expectedName = "expectedName";
            doReturn(expectedName).when(intygUser).getEfternamn();

            final var response = certificateServiceIntegrationUserHelper.get(intygUser);
            assertEquals(expectedName, response.getLastName());
        }

        @Test
        void shallReturnUserWithFullName() {
            final var expectedName = "expectedName";
            doReturn(expectedName).when(intygUser).getNamn();

            final var response = certificateServiceIntegrationUserHelper.get(intygUser);
            assertEquals(expectedName, response.getFullName());
        }

        @Test
        void shallReturnUserWithSrsActiveTrue() {
            final var features = Map.of(AuthoritiesConstants.FEATURE_SRS, new Feature());
            doReturn(features).when(intygUser).getFeatures();

            final var response = certificateServiceIntegrationUserHelper.get(intygUser);
            assertTrue(response.getSrsActive());
        }

        @Test
        void shallReturnUserWithSrsActiveFalse() {
            final var features = Collections.emptyMap();
            doReturn(features).when(intygUser).getFeatures();

            final var response = certificateServiceIntegrationUserHelper.get(intygUser);
            assertFalse(response.getSrsActive());
        }

        @Test
        void shouldReturnUserWithSpecialities() {
            final var response = certificateServiceIntegrationUserHelper.get(intygUser);

            assertEquals(SPECIALITIES, response.getSpecialities());
        }

        @Nested
        class Blocked {

            @Test
            void shouldReturnBlockedFalse() {
                final var response = certificateServiceIntegrationUserHelper.get(intygUser);

                assertFalse(response.getBlocked());
            }
        }

        @Nested
        class Agreement {

            @Test
            void shouldReturnAgreementTrue() {
                final var response = certificateServiceIntegrationUserHelper.get(intygUser);

                assertTrue(response.getAgreement());
            }
        }

        @Nested
        class TestRole {

            @Test
            void shouldReturnUserWithRoleDoctorFromDoctor() {
                addRole(AuthoritiesConstants.ROLE_LAKARE);

                final var response = certificateServiceIntegrationUserHelper.get(intygUser);

                assertEquals(CertificateServiceUserRole.DOCTOR, response.getRole());
            }

            @Test
            void shouldReturnUserWithRolePrivateDoctorFromPrivatlakare() {
                addRole(AuthoritiesConstants.ROLE_PRIVATLAKARE);

                final var response = certificateServiceIntegrationUserHelper.get(intygUser);

                assertEquals(CertificateServiceUserRole.PRIVATE_DOCTOR, response.getRole());
            }

            @Test
            void shouldReturnUserWithRoleAdministratorFromAdministrator() {
                addRole(AuthoritiesConstants.ROLE_ADMIN);

                final var response = certificateServiceIntegrationUserHelper.get(intygUser);

                assertEquals(CertificateServiceUserRole.CARE_ADMIN, response.getRole());
            }

            @Test
            void shouldReturnUserWithRoleNurseFromSjukskoterska() {
                addRole(AuthoritiesConstants.ROLE_SJUKSKOTERSKA);

                final var response = certificateServiceIntegrationUserHelper.get(intygUser);

                assertEquals(CertificateServiceUserRole.NURSE, response.getRole());
            }

            @Test
            void shouldReturnUserWithRoleMidwifeFromBarnmorska() {
                addRole(AuthoritiesConstants.ROLE_BARNMORSKA);

                final var response = certificateServiceIntegrationUserHelper.get(intygUser);

                assertEquals(CertificateServiceUserRole.MIDWIFE, response.getRole());
            }

            @Test
            void shouldReturnUserWithRoleDentistFromTandlakare() {
                addRole(AuthoritiesConstants.ROLE_TANDLAKARE);

                final var response = certificateServiceIntegrationUserHelper.get(intygUser);

                assertEquals(CertificateServiceUserRole.DENTIST, response.getRole());
            }

            @Test
            void shouldThrowExceptionIfUnknownRole() {
                addRole("");

                assertThrows(IllegalArgumentException.class, () -> certificateServiceIntegrationUserHelper.get(intygUser));
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

                when(intygUser.getBefattningar())
                    .thenReturn(PA_TITLES);

                try (MockedStatic<BefattningService> utilities = Mockito.mockStatic(BefattningService.class)) {
                    utilities.when(() -> BefattningService.getDescriptionFromCode(CODE_ONE)).thenReturn(Optional.of(DESCRIPTION_ONE));
                    utilities.when(() -> BefattningService.getDescriptionFromCode(CODE_TWO)).thenReturn(Optional.of(DESCRIPTION_TWO));

                    final var response = certificateServiceIntegrationUserHelper.get(intygUser);

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

                when(intygUser.getBefattningar())
                    .thenReturn(PA_TITLES);

                try (MockedStatic<BefattningService> utilities = Mockito.mockStatic(BefattningService.class)) {
                    utilities.when(() -> BefattningService.getDescriptionFromCode(CODE_ONE)).thenReturn(Optional.empty());
                    utilities.when(() -> BefattningService.getDescriptionFromCode(CODE_TWO)).thenReturn(Optional.empty());

                    final var response = certificateServiceIntegrationUserHelper.get(intygUser);

                    assertEquals(expectedPaTitles, response.getPaTitles());
                }
            }

            @Test
            void shouldReturnEmptyPaTitles() {
                final var expectedPaTitles = Collections.emptyList();

                when(intygUser.getBefattningar())
                    .thenReturn(Collections.emptyList());

                final var response = certificateServiceIntegrationUserHelper.get(intygUser);

                assertEquals(expectedPaTitles, response.getPaTitles());
            }
        }

        @Test
        void shallIncludeAccessScope() {
            assertEquals(AccessScopeType.WITHIN_CARE_UNIT, certificateServiceIntegrationUserHelper.get(intygUser).getAccessScope());
        }

        @Test
        void shallIncludeHealthCareProfessionalLicence() {
            when(intygUser.getLegitimeradeYrkesgrupper())
                .thenReturn(PROFESSIONAL_LICENCES);

            assertEquals(PROFESSIONAL_LICENCES,
                certificateServiceIntegrationUserHelper.get(intygUser).getHealthCareProfessionalLicence());
        }

        @Test
        void shallIncludeAllowCopyTrue() {
            assertTrue(certificateServiceIntegrationUserHelper.get(intygUser).getAllowCopy());
        }

        private void addRole(String roleName) {
            roles.remove("FIRST");
            final var role = new Role();
            role.setName(roleName);
            roles.put("ROLE", role);
        }
    }
}