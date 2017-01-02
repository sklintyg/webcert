/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

public class PersonnummerChecksumValidatorTest {

    @Test
    public void testValidateValid() {
        // personnummer
        parameterizedValidateTest("191212121212", false);
        parameterizedValidateTest("19121212-1212", false);
        parameterizedValidateTest("1212121212", false);
        parameterizedValidateTest("121212-1212", false);
        parameterizedValidateTest("19800131-0005", false);
        // samordningsnummer
        parameterizedValidateTest("198001910002", false);
        parameterizedValidateTest("19800191-0002", false);
        parameterizedValidateTest("8001910002", false);
        parameterizedValidateTest("800191-0002", false);
    }

    @Test
    public void testValidateInvalid() {
        parameterizedValidateTest("190101010101", true);
        parameterizedValidateTest("19800131-000x", true);
        parameterizedValidateTest("18400505+0002", true);
        parameterizedValidateTest("30130823-0000", true);
        parameterizedValidateTest("19800131-0001", true);
        parameterizedValidateTest("19800131-0002", true);
        parameterizedValidateTest("8001310003", true);
        parameterizedValidateTest("19800131-0004", true);
        parameterizedValidateTest("198001310006", true);
        parameterizedValidateTest("19800131-0007", true);
        parameterizedValidateTest("19800131-0008", true);
        parameterizedValidateTest("19800131-0009", true);
        parameterizedValidateTest("19800131-A009", true);
        parameterizedValidateTest("invalid", true);
    }

    private void parameterizedValidateTest(String pnr, boolean errorExpected) {
        ResultValidator errors = ResultValidator.newInstance();
        PersonnummerChecksumValidator.validate(new Personnummer(pnr), errors);
        if (errorExpected) {
            assertTrue(errors.hasErrors());
        } else {
            assertFalse(errors.hasErrors());
        }
    }
}
