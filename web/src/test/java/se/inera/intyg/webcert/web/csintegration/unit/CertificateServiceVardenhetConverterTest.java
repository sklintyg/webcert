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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;

class CertificateServiceVardenhetConverterTest {

    private static final String EXPECTED_UNIT_ID = "expectedUnitId";
    private static final String EXPECTED_UNIT_NAME = "expectedUnitName";
    private static final String EXPECTED_ADDRESS = "expectedAddress";
    private static final String EXPECTED_ZIP_CODE = "expectedZipCode";
    private static final String EXPECTED_CITY = "expectedCity";
    private static final String EXPECTED_PHONE_NUMBER = "expectedPhoneNumber";
    private static final String EXPECTED_MAIL = "expectedMail";
    private CertificateServiceVardenhetConverter certificateServiceVardenhetConverter;
    private Vardenhet unit;

    @BeforeEach
    void setUp() {
        certificateServiceVardenhetConverter = new CertificateServiceVardenhetConverter();
        unit = new Vardenhet();
        unit.setId(EXPECTED_UNIT_ID);
        unit.setNamn(EXPECTED_UNIT_NAME);
        unit.setPostadress(EXPECTED_ADDRESS);
        unit.setPostnummer(EXPECTED_ZIP_CODE);
        unit.setEpost(EXPECTED_MAIL);
        unit.setPostort(EXPECTED_CITY);
        unit.setTelefonnummer(EXPECTED_PHONE_NUMBER);
    }

    @Test
    void shallConvertUnitId() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit);
        assertEquals(EXPECTED_UNIT_ID, unit.getId());
    }

    @Test
    void shallConvertUnitName() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit);
        assertEquals(EXPECTED_UNIT_NAME, unit.getName());
    }

    @Test
    void shallConvertPostalAddress() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit);
        assertEquals(EXPECTED_ADDRESS, unit.getAddress());
    }

    @Test
    void shallConvertPostalCode() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit);
        assertEquals(EXPECTED_ZIP_CODE, unit.getZipCode());
    }

    @Test
    void shallConvertPostalCity() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit);
        assertEquals(EXPECTED_CITY, unit.getCity());
    }

    @Test
    void shallConvertTelephoneNumber() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit);
        assertEquals(EXPECTED_PHONE_NUMBER, unit.getPhoneNumber());
    }

    @Test
    void shallConvertEmail() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit);
        assertEquals(EXPECTED_MAIL, unit.getEmail());
    }

    @Test
    void shallConvertInactiveToTrue() {
        final var activeUnit = new Vardenhet();
        activeUnit.setStart(LocalDateTime.now().minusDays(1));
        activeUnit.setEnd(LocalDateTime.now().plusDays(1));
        final var unit = certificateServiceVardenhetConverter.convert(activeUnit);
        assertTrue(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToFalse() {
        final var inactiveUnit = new Vardenhet();
        inactiveUnit.setStart(LocalDateTime.now().plusDays(1));
        inactiveUnit.setEnd(LocalDateTime.now().plusDays(2));
        final var unit = certificateServiceVardenhetConverter.convert(inactiveUnit);
        assertFalse(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToTrueIfStartDateAndEndDateIsNull() {
        final var activeUnit = new Vardenhet();
        final var unit = certificateServiceVardenhetConverter.convert(activeUnit);
        assertTrue(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToTrueIfStartDateIsNullAndEndDateIsAfterNow() {
        final var activeUnit = new Vardenhet();
        activeUnit.setEnd(LocalDateTime.now().plusDays(1));
        final var unit = certificateServiceVardenhetConverter.convert(activeUnit);
        assertTrue(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToFalseIfStartDateIsNullAndEndDateIsBeforeNow() {
        final var inactiveUnit = new Vardenhet();
        inactiveUnit.setEnd(LocalDateTime.now().minusDays(1));
        final var unit = certificateServiceVardenhetConverter.convert(inactiveUnit);
        assertFalse(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToTrueIfStartDateIsBeforeNowAndEndDateIsNull() {
        final var activeUnit = new Vardenhet();
        activeUnit.setStart(LocalDateTime.now().minusDays(1));
        final var unit = certificateServiceVardenhetConverter.convert(activeUnit);
        assertTrue(unit.getInactive());
    }

    @Test
    void shallConvertInactiveToFalseIfStartDateIsAfterNowAndEndDateIsNull() {
        final var inactiveUnit = new Vardenhet();
        inactiveUnit.setStart(LocalDateTime.now().plusDays(1));
        final var unit = certificateServiceVardenhetConverter.convert(inactiveUnit);
        assertFalse(unit.getInactive());
    }
}