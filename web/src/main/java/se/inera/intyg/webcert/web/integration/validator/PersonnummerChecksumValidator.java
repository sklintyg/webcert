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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.inera.intyg.common.support.modules.support.api.dto.InvalidPersonNummerException;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.common.support.validate.ValidatorUtil;

public final class PersonnummerChecksumValidator {

    private static final Pattern PERSONNUMMER_PATTERN = Pattern.compile("(\\d{8})(\\d{3})(\\d)");
    private static final int PERSONNUMMER_DATE_GROUP = 1;
    private static final int PERSONNUMMER_BIRTHNUMBER_GROUP = 2;
    private static final int PERSONNUMMER_CHECKSUM_GROUP = 3;

    private PersonnummerChecksumValidator() {
    }

    public static void validate(Personnummer personnummer, ResultValidator errors) {
        String pnr = null;
        try {
            pnr = personnummer.getNormalizedPnr();
        } catch (InvalidPersonNummerException e) {
            errors.addError(e.getMessage());
            return;
        }
        Matcher m = PERSONNUMMER_PATTERN.matcher(pnr);
        if (!m.matches()) {
            errors.addError(String.format("Could not parse the SSN '%s' (format should be 'yyyyMMdd-nnnn')", pnr));
            return;
        }
        String dateString = m.group(PERSONNUMMER_DATE_GROUP);
        String nnn = m.group(PERSONNUMMER_BIRTHNUMBER_GROUP);
        int mod10 = Integer.parseInt(m.group(PERSONNUMMER_CHECKSUM_GROUP));
        checkChecksum(pnr, dateString, nnn, mod10, errors);
    }

    /**
     * Check that the checksum of the personnummer is correct.
     *
     * @param pnr
     *            The personnummer. Used in validation messages.
     * @param dateString
     *            The date as a string at the form <code>yyyyMMdd</code>.
     * @param nnn
     *            The 3 first digits of the last 4.
     * @param mod10
     *            The last digit of the personnummer.
     * @param errors
     *            ResultValidator that validation messages are added to.
     */
    private static void checkChecksum(String pnr, String dateString, String nnn, int mod10, ResultValidator errors) {
        if (ValidatorUtil.calculateMod10(dateString.substring(2) + nnn) != mod10) {
            errors.addError(String.format("The checksum digit in SSN '%s' is invalid", pnr));
        }
    }
}
