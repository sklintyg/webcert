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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private static final String EXPECTED_WORKPLACE_CODE = "expectedWorkplaceCode";
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
        unit.setArbetsplatskod(EXPECTED_WORKPLACE_CODE);
    }

    @Test
    void shallConvertUnitId() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit, false);
        assertEquals(EXPECTED_UNIT_ID, unit.getId());
    }

    @Test
    void shallConvertUnitName() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit, false);
        assertEquals(EXPECTED_UNIT_NAME, unit.getName());
    }

    @Test
    void shallConvertPostalAddress() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit, false);
        assertEquals(EXPECTED_ADDRESS, unit.getAddress());
    }

    @Test
    void shallConvertPostalCode() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit, false);
        assertEquals(EXPECTED_ZIP_CODE, unit.getZipCode());
    }

    @Test
    void shallConvertPostalCity() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit, false);
        assertEquals(EXPECTED_CITY, unit.getCity());
    }

    @Test
    void shallConvertTelephoneNumber() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit, false);
        assertEquals(EXPECTED_PHONE_NUMBER, unit.getPhoneNumber());
    }

    @Test
    void shallConvertEmail() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit, false);
        assertEquals(EXPECTED_MAIL, unit.getEmail());
    }

    @Test
    void shallConvertWorkplaceCode() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit, false);
        assertEquals(EXPECTED_WORKPLACE_CODE, unit.getWorkplaceCode());
    }

    @Test
    void shallConvertInactiveUnit() {
        final var unit = certificateServiceVardenhetConverter.convert(this.unit, true);
        assertTrue(unit.getInactive());
    }
}
