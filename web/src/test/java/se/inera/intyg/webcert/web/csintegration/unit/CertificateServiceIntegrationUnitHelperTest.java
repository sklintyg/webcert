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

package se.inera.intyg.webcert.web.csintegration.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Mottagning;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.security.common.model.IntygUser;

@ExtendWith(MockitoExtension.class)
class CertificateServiceIntegrationUnitHelperTest {

    private static final CertificateServiceUnitDTO CONVERTED_UNIT = CertificateServiceUnitDTO.builder().build();
    private static final CertificateServiceUnitDTO CONVERTED_CARE_UNIT = CertificateServiceUnitDTO.builder().build();


    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final String CARE_PROVIDER_NAME = "CARE_PROVIDER_NAME";
    private static final String CARE_UNIT_ID = "CARE_UNIT_ID";
    private static final String CARE_UNIT_NAME = "CARE_UNIT_NAME";
    private static final String SUB_UNIT_ID = "SUB_UNIT_ID";
    private static final String SUB_UNIT_NAME = "SUB_UNIT_NAME";

    private final Vardenhet chosenCareUnit = new Vardenhet(CARE_UNIT_ID, CARE_UNIT_NAME);
    private final Vardgivare chosenCareProvider = new Vardgivare(CARE_PROVIDER_ID, CARE_PROVIDER_NAME);
    private final Mottagning chosenSubUnit = new Mottagning(SUB_UNIT_ID, SUB_UNIT_NAME);

    @Mock
    IntygUser intygUser;

    @Mock
    CertificateServiceVardenhetConverter certificateServiceVardenhetConverter;

    @InjectMocks
    CertificateServiceIntegrationUnitHelper certificateServiceIntegrationUnitHelper;

    @BeforeEach
    void setup() {
        chosenCareProvider.setVardenheter(
            List.of(
                chosenCareUnit,
                new Vardenhet("NOT_IT", "NOT_IT")
            )
        );

        chosenSubUnit.setParentHsaId(CARE_UNIT_ID);
    }

    @Nested
    class TestCareProvider {

        @BeforeEach
        void setup() {
            when(intygUser.getValdVardgivare())
                .thenReturn(chosenCareProvider);
        }

        @Test
        void shouldReturnLoggedInCareProviderId() {
            final var response = certificateServiceIntegrationUnitHelper.getCareProvider(intygUser);

            assertEquals(CARE_PROVIDER_ID, response.getId());
        }

        @Test
        void shouldReturnLoggedInCareProviderName() {
            final var response = certificateServiceIntegrationUnitHelper.getCareProvider(intygUser);

            assertEquals(CARE_PROVIDER_NAME, response.getName());
        }
    }

    @Nested
    class LoggedIntoCareUnit {

        @BeforeEach
        void setup() {
            when(intygUser.getValdVardenhet())
                .thenReturn(chosenCareUnit);
            when(certificateServiceVardenhetConverter.convert(chosenCareUnit, false))
                .thenReturn(CONVERTED_CARE_UNIT);
        }

        @Test
        void shouldReturnConvertedChosenCareUnitAsCareUnit() {
            final var response = certificateServiceIntegrationUnitHelper.getCareUnit(intygUser);

            assertEquals(CONVERTED_CARE_UNIT, response);
        }

        @Test
        void shouldReturnConvertedChosenCareUnitAsUnit() {
            final var response = certificateServiceIntegrationUnitHelper.getUnit(intygUser);

            assertEquals(CONVERTED_CARE_UNIT, response);
        }
    }

    @Nested
    class LoggedIntoSubUnit {

        @BeforeEach
        void setup() {
            when(intygUser.getValdVardenhet())
                .thenReturn(chosenSubUnit);
        }

        @Test
        void shouldReturnConvertedCareUnitOfChosenSubUnitAsCareUnit() {
            when(intygUser.getValdVardgivare())
                .thenReturn(chosenCareProvider);
            when(certificateServiceVardenhetConverter.convert(chosenCareUnit, false))
                .thenReturn(CONVERTED_CARE_UNIT);

            final var response = certificateServiceIntegrationUnitHelper.getCareUnit(intygUser);

            assertEquals(CONVERTED_CARE_UNIT, response);
        }

        @Test
        void shouldReturnConvertedChosenSubUnitAsUnit() {
            when(certificateServiceVardenhetConverter.convert(chosenSubUnit, false))
                .thenReturn(CONVERTED_UNIT);

            final var response = certificateServiceIntegrationUnitHelper.getUnit(intygUser);

            assertEquals(CONVERTED_UNIT, response);
        }
    }
}
