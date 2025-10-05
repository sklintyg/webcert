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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.infra.security.common.model.Role;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class LoggedInWebcertUserFactoryTest {

    @InjectMocks
    private LoggedInWebcertUserFactory factory;

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateFromIntygUser {

        private IntygUser intygUser;

        @BeforeEach
        void setUp() {
            intygUser = mock(IntygUser.class);
        }

        @Test
        void shallIncludeStaffId() {
            final var expected = "staffId";
            when(intygUser.getHsaId()).thenReturn(expected);

            final var actual = factory.create(intygUser);
            assertEquals(expected, actual.getStaffId());
        }

        @Test
        void shallIncludeUnitId() {
            final var expected = "unitId";
            when(intygUser.getValdVardenhet()).thenReturn(new Mottagning(expected, "testenhet"));

            final var actual = factory.create(intygUser);
            assertEquals(expected, actual.getUnitId());
        }

        @Test
        void shallIncludeCareProviderId() {
            final var expected = "careProviderId";
            when(intygUser.getValdVardgivare()).thenReturn(new Vardgivare(expected, "testvårdgivare"));

            final var actual = factory.create(intygUser);
            assertEquals(expected, actual.getCareProviderId());
        }

        @Test
        void shallIncludeRoleWhenSingleRole() {
            final var expected = "role";
            when(intygUser.getRoles()).thenReturn(Map.of(expected, new Role()));

            final var actual = factory.create(intygUser);
            assertEquals(expected, actual.getRole());
        }

        @Test
        void shallNotIncludeOrigin() {
            final var actual = factory.create(intygUser);
            assertNull(actual.getOrigin(), "Origin shall not be included when creating from IntygUser");
            verify(intygUser, never()).getOrigin();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CreateFromWebCertUser {

        private WebCertUser webCertUser;

        @BeforeEach
        void setUp() {
            webCertUser = mock(WebCertUser.class);
        }

        @Test
        void shallIncludeStaffId() {
            final var expected = "staffId";
            when(webCertUser.getHsaId()).thenReturn(expected);

            final var actual = factory.create(webCertUser);
            assertEquals(expected, actual.getStaffId());
        }

        @Test
        void shallIncludeUnitId() {
            final var expected = "unitId";
            when(webCertUser.getValdVardenhet()).thenReturn(new Mottagning(expected, "testenhet"));

            final var actual = factory.create(webCertUser);
            assertEquals(expected, actual.getUnitId());
        }

        @Test
        void shallIncludeCareProviderId() {
            final var expected = "careProviderId";
            when(webCertUser.getValdVardgivare()).thenReturn(new Vardgivare(expected, "testvårdgivare"));

            final var actual = factory.create(webCertUser);
            assertEquals(expected, actual.getCareProviderId());
        }

        @Test
        void shallIncludeRoleWhenSingleRole() {
            final var expected = "role";
            when(webCertUser.getRoles()).thenReturn(Map.of(expected, new Role()));

            final var actual = factory.create(webCertUser);
            assertEquals(expected, actual.getRole());
        }

        @Test
        void shallIncludeOrigin() {
            final var expected = "origin";
            when(webCertUser.getOrigin()).thenReturn(expected);

            final var actual = factory.create(webCertUser);
            assertEquals(expected, actual.getOrigin());
        }
    }
}