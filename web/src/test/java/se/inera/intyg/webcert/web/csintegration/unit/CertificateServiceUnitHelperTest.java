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
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

@ExtendWith(MockitoExtension.class)
class CertificateServiceUnitHelperTest {

    private static final CertificateServiceUnitDTO CONVERTED_UNIT = CertificateServiceUnitDTO.builder().build();
    private static final CertificateServiceUnitDTO CONVERTED_CARE_UNIT = CertificateServiceUnitDTO.builder().build();

    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final String CARE_PROVIDER_NAME = "CARE_PROVIDER_NAME";
    private static final String CARE_UNIT_ID = "CARE_UNIT_ID";
    private static final String CARE_UNIT_NAME = "CARE_UNIT_NAME";
    private static final String SUB_UNIT_ID = "SUB_UNIT_ID";
    private static final String SUB_UNIT_NAME = "SUB_UNIT_NAME";

    private static final String NOT_CHOSEN_CARE_PROVIDER_ID = "NOT_CHOSEN_ID";
    private static final String NOT_CHOSEN_CARE_PROVIDER_NAME = "NOT_CHOSEN_NAME";
    private static final String NOT_CHOSEN_CARE_UNIT_ID = "CARE_UNIT_NOT_CHOSEN_ID";
    private static final String NOT_CHOSEN_CARE_UNIT_NAME = "CARE_UNIT_NOT_CHOSEN_NAME";
    private static final String NOT_CHOSEN_SUB_UNIT_ID = "SUB_UNIT_NOT_CHOSEN_ID";
    private static final String NOT_CHOSEN_SUB_UNIT_NAME = "SUB_UNIT_NOT_CHOSEN_NAME";

    @Mock
    WebCertUserService webCertUserService;

    @Mock
    WebCertUser user;

    @Mock
    CertificateServiceVardenhetConverter certificateServiceVardenhetConverter;

    @InjectMocks
    CertificateServiceUnitHelper certificateServiceUnitHelper;

    private final Vardenhet chosenCareUnit = new Vardenhet(CARE_UNIT_ID, CARE_UNIT_NAME);
    private final Vardgivare chosenCareProvider = new Vardgivare(CARE_PROVIDER_ID, CARE_PROVIDER_NAME);
    private final Vardenhet chosenSubUnit = new Vardenhet(SUB_UNIT_ID, SUB_UNIT_NAME);

    @BeforeEach
    void setup() {
        when(webCertUserService.getUser())
            .thenReturn(user);

        when(user.getValdVardgivare())
            .thenReturn(new Vardgivare(CARE_PROVIDER_ID, CARE_PROVIDER_NAME));

        chosenCareUnit.setMottagningar(
            List.of(
                new Mottagning(SUB_UNIT_ID, SUB_UNIT_NAME),
                new Mottagning(NOT_CHOSEN_SUB_UNIT_ID, NOT_CHOSEN_SUB_UNIT_NAME)
            )
        );

        chosenCareProvider.setVardenheter(
            List.of(
                new Vardenhet(NOT_CHOSEN_CARE_UNIT_ID, NOT_CHOSEN_CARE_UNIT_NAME),
                chosenCareUnit,
                chosenSubUnit
            )
        );
    }

    @Nested
    class TestCareProvider {

        @Test
        void shouldReturnLoggedInCareProviderId() {
            final var response = certificateServiceUnitHelper.getCareProvider();

            assertEquals(CARE_PROVIDER_ID, response.getId());
        }

        @Test
        void shouldReturnLoggedInCareProviderName() {
            final var response = certificateServiceUnitHelper.getCareProvider();

            assertEquals(CARE_PROVIDER_NAME, response.getName());
        }
    }

    @Nested
    class LoggedIntoCareUnit {

        @BeforeEach
        void setup() {
            when(user.getValdVardenhet())
                .thenReturn(new Vardenhet(CARE_UNIT_ID, CARE_UNIT_NAME));

            when(user.getVardgivare()).thenReturn(List.of(
                new Vardgivare(NOT_CHOSEN_CARE_PROVIDER_ID, NOT_CHOSEN_CARE_PROVIDER_NAME),
                chosenCareProvider
            ));
        }

        @Test
        void shouldReturnConvertedChosenCareUnitAsCareUnit() {
            when(certificateServiceVardenhetConverter.convert(chosenCareUnit))
                .thenReturn(CONVERTED_CARE_UNIT);

            final var response = certificateServiceUnitHelper.getCareUnit();

            assertEquals(CONVERTED_CARE_UNIT, response);
        }

        @Test
        void shouldReturnConvertedChosenCareUnitAsUnit() {
            when(certificateServiceVardenhetConverter.convert(chosenCareUnit))
                .thenReturn(CONVERTED_CARE_UNIT);

            final var response = certificateServiceUnitHelper.getUnit();

            assertEquals(CONVERTED_CARE_UNIT, response);
        }
    }

    @Nested
    class LoggedIntoSubUnit {

        @BeforeEach
        void setup() {
            when(user.getValdVardenhet())
                .thenReturn(new Vardenhet(SUB_UNIT_ID, SUB_UNIT_NAME));

            when(user.getVardgivare()).thenReturn(List.of(
                new Vardgivare(NOT_CHOSEN_CARE_PROVIDER_ID, NOT_CHOSEN_CARE_PROVIDER_NAME),
                chosenCareProvider
            ));
        }

        @Test
        void shouldReturnConvertedCareUnitOfChosenSubUnitAsCareUnit() {
            when(certificateServiceVardenhetConverter.convert(chosenCareUnit))
                .thenReturn(CONVERTED_CARE_UNIT);

            final var response = certificateServiceUnitHelper.getCareUnit();

            assertEquals(CONVERTED_CARE_UNIT, response);
        }

        @Test
        void shouldReturnConvertedChosenSubUnitAsUnit() {
            when(certificateServiceVardenhetConverter.convert(chosenSubUnit))
                .thenReturn(CONVERTED_UNIT);

            final var response = certificateServiceUnitHelper.getUnit();

            assertEquals(CONVERTED_UNIT, response);
        }
    }
}