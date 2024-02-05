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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.facade.model.user.User;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.webcert.web.service.facade.user.UserService;

@ExtendWith(MockitoExtension.class)
class CertificateServiceUnitHelperTest {

    private static final Unit unit = Unit.builder().build();
    private static final Vardenhet vardEnhet = new Vardenhet();
    private static final Unit careUnit = Unit.builder().build();
    private static final Unit careProvider = Unit.builder().build();
    private static final User user = User.builder()
        .loggedInUnit(unit)
        .loggedInCareUnit(careUnit)
        .loggedInCareProvider(careProvider)
        .build();

    private static final CertificateServiceUnitDTO convertedUnit = new CertificateServiceUnitDTO();

    @Mock
    UserService userService;

    @Mock
    CertificateServiceUnitConverter certificateServiceUnitConverter;

    @Mock
    HsaOrganizationsService hsaOrganizationsService;
    @Mock
    CertificateServiceVardenhetConverter certificateServiceVardenhetConverter;

    @InjectMocks
    CertificateServiceUnitHelper certificateServiceUnitHelper;

    @BeforeEach
    void setUp() {
        when(userService.getLoggedInUser())
            .thenReturn(user);
    }

    @Test
    void shouldReturnConvertedUnit() {
        when(hsaOrganizationsService.getVardenhet(user.getLoggedInUnit().getUnitId()))
            .thenReturn(vardEnhet);
        when(certificateServiceVardenhetConverter.convert(vardEnhet))
            .thenReturn(convertedUnit);
        final var response = certificateServiceUnitHelper.getUnit();

        assertEquals(convertedUnit, response);

    }

    @Test
    void shouldReturnConvertedCareUnit() {
        when(certificateServiceUnitConverter.convert(careUnit))
            .thenReturn(convertedUnit);
        final var response = certificateServiceUnitHelper.getCareUnit();

        assertEquals(convertedUnit, response);

    }

    @Test
    void shouldReturnConvertedCareProvider() {
        when(certificateServiceUnitConverter.convert(careProvider))
            .thenReturn(convertedUnit);
        final var response = certificateServiceUnitHelper.getCareProvider();

        assertEquals(convertedUnit, response);

    }
}