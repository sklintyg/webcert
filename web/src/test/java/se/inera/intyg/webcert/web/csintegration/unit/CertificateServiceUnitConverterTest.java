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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.metadata.Unit;

@ExtendWith(MockitoExtension.class)
class CertificateServiceUnitConverterTest {

    @InjectMocks
    CertificateServiceUnitConverter certificateServiceUnitConverter;

    private Unit createUnit() {
        return Unit.builder()
            .unitId("unitId")
            .unitName("unitName")
            .address("address")
            .zipCode("zipCode")
            .city("city")
            .phoneNumber("phoneNumber")
            .email("email")
            .isInactive(true)
            .build();
    }

    @Test
    void shouldConvertId() {
        final var unit = createUnit();
        final var response = certificateServiceUnitConverter.convert(unit);

        assertEquals(unit.getUnitId(), response.getId());
    }

    @Test
    void shouldConvertName() {
        final var unit = createUnit();
        final var response = certificateServiceUnitConverter.convert(unit);

        assertEquals(unit.getUnitName(), response.getName());
    }

    @Test
    void shouldConvertAddress() {
        final var unit = createUnit();
        final var response = certificateServiceUnitConverter.convert(unit);

        assertEquals(unit.getAddress(), response.getAddress());
    }

    @Test
    void shouldConvertZipCode() {
        final var unit = createUnit();
        final var response = certificateServiceUnitConverter.convert(unit);

        assertEquals(unit.getZipCode(), response.getZipCode());
    }

    @Test
    void shouldConvertCity() {
        final var unit = createUnit();
        final var response = certificateServiceUnitConverter.convert(unit);

        assertEquals(unit.getCity(), response.getCity());
    }

    @Test
    void shouldConvertPhoneNumber() {
        final var unit = createUnit();
        final var response = certificateServiceUnitConverter.convert(unit);

        assertEquals(unit.getPhoneNumber(), response.getPhoneNumber());
    }

    @Test
    void shouldConvertEmail() {
        final var unit = createUnit();
        final var response = certificateServiceUnitConverter.convert(unit);

        assertEquals(unit.getEmail(), response.getEmail());
    }

    @Test
    void shouldConvertInactive() {
        final var unit = createUnit();
        final var response = certificateServiceUnitConverter.convert(unit);

        assertEquals(unit.getIsInactive(), response.getInactive());
    }
}