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

package se.inera.intyg.webcert.web.csintegration.certificate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class IntegratedUnitRegistryHelperTest {

    private static final String UNIT_ID = "UNIT_ID";
    private static final String UNIT_NAME = "UNIT_NAME";
    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final String CARE_PROVIDER_NAME = "CARE_PROVIDER_NAME";

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    IntegreradeEnheterRegistry enheterRegistry;

    @Mock
    WebCertUser webCertUser;

    @InjectMocks
    IntegratedUnitRegistryHelper integratedUnitRegistryHelper;

    @Nested
    class NoUser {

        @BeforeEach
        void beforeAll() {
            when(webCertUserService.getUser())
                .thenReturn(webCertUser);
        }

        @Test
        void shouldAddIfDeepIntegration() {
            when(webCertUser.getValdVardenhet())
                .thenReturn(new Vardenhet(UNIT_ID, UNIT_NAME));
            when(webCertUser.getValdVardgivare())
                .thenReturn(new Vardenhet(CARE_PROVIDER_ID, CARE_PROVIDER_NAME));
            final var expectedEntry = new IntegreradEnhetEntry(
                UNIT_ID,
                UNIT_NAME,
                CARE_PROVIDER_ID,
                CARE_PROVIDER_NAME
            );
            when(webCertUser.getOrigin())
                .thenReturn("DJUPINTEGRATION");

            integratedUnitRegistryHelper.addUnit();

            verify(enheterRegistry).putIntegreradEnhet(expectedEntry, false, true);
        }

        @Test
        void shouldNotAddIfNotDeepIntegration() {
            when(webCertUser.getOrigin())
                .thenReturn("NORMAL");

            integratedUnitRegistryHelper.addUnit();

            verify(enheterRegistry, times(0)).putIntegreradEnhet(any(), anyBoolean(), anyBoolean());
        }
    }

    @Nested
    class HasUser {

        @Test
        void shouldAddIfDeepIntegration() {
            when(webCertUser.getValdVardenhet())
                .thenReturn(new Vardenhet(UNIT_ID, UNIT_NAME));
            when(webCertUser.getValdVardgivare())
                .thenReturn(new Vardenhet(CARE_PROVIDER_ID, CARE_PROVIDER_NAME));
            final var expectedEntry = new IntegreradEnhetEntry(
                UNIT_ID,
                UNIT_NAME,
                CARE_PROVIDER_ID,
                CARE_PROVIDER_NAME
            );
            when(webCertUser.getOrigin())
                .thenReturn("DJUPINTEGRATION");

            integratedUnitRegistryHelper.addUnit(webCertUser);

            verify(enheterRegistry).putIntegreradEnhet(expectedEntry, false, true);
        }

        @Test
        void shouldNotAddIfNotDeepIntegration() {
            when(webCertUser.getOrigin())
                .thenReturn("NORMAL");

            integratedUnitRegistryHelper.addUnit(webCertUser);

            verify(enheterRegistry, times(0)).putIntegreradEnhet(any(), anyBoolean(), anyBoolean());
        }
    }
}