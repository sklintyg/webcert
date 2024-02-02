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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.infra.integration.hsatk.model.Unit;

class CertificateServiceHsaUnitConverterTest {

    private static final String EXPECTED_UNIT_ID = "expectedUnitId";
    private static final String EXPECTED_UNIT_NAME = "expectedUnitName";
    private static final String EXPECTED_ADDRESS = "expectedAddress";
    private static final String EXPECTED_ZIP_CODE = "expectedZipCode";
    private static final String EXPECTED_CITY = "expectedCity";
    private static final String EXPECTED_PHONE_NUMBER = "expectedPhoneNumber";
    private static final String EXPECTED_MAIL = "expectedMail";
    private CertificateServiceHsaUnitConverter certificateServiceHsaUnitConverter;
    private Unit unit;

    @BeforeEach
    void setUp() {
        certificateServiceHsaUnitConverter = new CertificateServiceHsaUnitConverter();
        unit = new Unit();
        unit.setUnitHsaId(EXPECTED_UNIT_ID);
        unit.setUnitName(EXPECTED_UNIT_NAME);
        unit.setPostalAddress(List.of(EXPECTED_ADDRESS));
        unit.setPostalCode(EXPECTED_ZIP_CODE);
        unit.setMail(EXPECTED_MAIL);
    }

    @Test
    void shallConvertUnitId() {
        final var unit = certificateServiceHsaUnitConverter.convert(this.unit);
        assertEquals(EXPECTED_UNIT_ID, unit.getId());
    }

    @Test
    void shallConvertUnitName() {
        final var unit = certificateServiceHsaUnitConverter.convert(this.unit);
        assertEquals(EXPECTED_UNIT_NAME, unit.getName());
    }

    @Test
    void shallConvertPostalAddressIfNotEmpty() {
        final var unit = certificateServiceHsaUnitConverter.convert(this.unit);
        assertEquals(EXPECTED_ADDRESS, unit.getAddress());
    }

    @Test
    void shallNotConvertPostalAddressIfEmpty() {
        final var unitWithoutPostalAddress = new Unit();
        final var unit = certificateServiceHsaUnitConverter.convert(unitWithoutPostalAddress);
        assertNull(unit.getAddress());
    }

    @Test
    void shallConvertPostalCode() {
        final var unit = certificateServiceHsaUnitConverter.convert(this.unit);
        assertEquals(EXPECTED_ZIP_CODE, unit.getZipCode());
    }

    @Test
    void shallConvertCityFromLastIndexInPostalAddress() {
        final var unitWithMultipleAddress = new Unit();
        unitWithMultipleAddress.setPostalAddress(List.of(EXPECTED_ADDRESS, EXPECTED_CITY));
        final var unit = certificateServiceHsaUnitConverter.convert(unitWithMultipleAddress);
        assertEquals(EXPECTED_CITY, unit.getCity());
    }

    @Test
    void shallNotConvertCityIfPostalAddressIsEmpty() {
        final var unitWithoutPostalAddress = new Unit();
        final var unit = certificateServiceHsaUnitConverter.convert(unitWithoutPostalAddress);
        assertNull(unit.getCity());
    }

    @Test
    void shallConvertTelephoneNumber() {
        final var unitWithTelephoneNumber = new Unit();
        unitWithTelephoneNumber.setTelephoneNumber(List.of(EXPECTED_PHONE_NUMBER));
        final var unit = certificateServiceHsaUnitConverter.convert(unitWithTelephoneNumber);
        assertEquals(EXPECTED_PHONE_NUMBER, unit.getPhoneNumber());
    }

    @Test
    void shallConvertEmail() {
        final var unit = certificateServiceHsaUnitConverter.convert(this.unit);
        assertEquals(EXPECTED_MAIL, unit.getEmail());
    }

    @Test
    void shallConvertInactiveToTrue() {
        final var activeUnit = new Unit();
        activeUnit.setUnitStartDate(LocalDateTime.now().minusDays(1));
        activeUnit.setUnitEndDate(LocalDateTime.now().plusDays(1));
        final var unit = certificateServiceHsaUnitConverter.convert(activeUnit);
        assertTrue(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToFalse() {
        final var inactiveUnit = new Unit();
        inactiveUnit.setUnitStartDate(LocalDateTime.now().plusDays(1));
        inactiveUnit.setUnitEndDate(LocalDateTime.now().plusDays(2));
        final var unit = certificateServiceHsaUnitConverter.convert(inactiveUnit);
        assertFalse(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToTrueIfStartDateAndEndDateIsNull() {
        final var activeUnit = new Unit();
        final var unit = certificateServiceHsaUnitConverter.convert(activeUnit);
        assertTrue(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToTrueIfStartDateIsNullAndEndDateIsAfterNow() {
        final var activeUnit = new Unit();
        activeUnit.setUnitEndDate(LocalDateTime.now().plusDays(1));
        final var unit = certificateServiceHsaUnitConverter.convert(activeUnit);
        assertTrue(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToFalseIfStartDateIsNullAndEndDateIsBeforeNow() {
        final var inactiveUnit = new Unit();
        inactiveUnit.setUnitEndDate(LocalDateTime.now().minusDays(1));
        final var unit = certificateServiceHsaUnitConverter.convert(inactiveUnit);
        assertFalse(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToTrueIfStartDateIsBeforeNowAndEndDateIsNull() {
        final var activeUnit = new Unit();
        activeUnit.setUnitStartDate(LocalDateTime.now().minusDays(1));
        final var unit = certificateServiceHsaUnitConverter.convert(activeUnit);
        assertTrue(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToFalseIfStartDateIsAfterNowAndEndDateIsNull() {
        final var inactiveUnit = new Unit();
        inactiveUnit.setUnitStartDate(LocalDateTime.now().plusDays(1));
        final var unit = certificateServiceHsaUnitConverter.convert(inactiveUnit);
        assertFalse(unit.getInactive());
    }
}
