/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.fkstub.validation;

import static java.util.Arrays.asList;
import static se.inera.intyg.common.support.Constants.PERSON_ID_OID;
import static se.inera.intyg.common.support.Constants.SAMORDNING_ID_OID;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.intyg.common.util.logging.LogMarkers;

public final class PatientValidator {

    private static final Logger LOG = LoggerFactory.getLogger(PatientValidator.class);

    private static final List<String> PATIENT_ID_OIDS = asList(PERSON_ID_OID, SAMORDNING_ID_OID);

    private static final String PERSON_NUMBER_REGEX = "[0-9]{8}[-+]?[0-9]{4}";
    private static final String PERSON_NUMBER_WITHOUT_DASH_REGEX = "[0-9]{12}";

    private PatientValidator() {
    }

    /**
     * Validate and correct patient information.
     *
     * @return true if valid enough to continue
     */
    public static boolean validateAndCorrect(String certificateId, PatientType patient, List<String> validationErrors) {
        if (patient == null) {
            validationErrors.add("No Patient element found!");
            return false;
        }

        // Check patient id - mandatory
        if (patient.getPersonId() == null
                || patient.getPersonId().getExtension() == null
                || patient.getPersonId().getExtension().isEmpty()) {
            validationErrors.add("No Patient Id found!");
            return false;
        }
        // Correct personnummer without dashes
        String personNumber = patient.getPersonId().getExtension();
        if (Pattern.matches(PERSON_NUMBER_WITHOUT_DASH_REGEX, personNumber)) {
            patient.getPersonId().setExtension(formatWithDash(personNumber));
            LOG.warn(LogMarkers.VALIDATION, "Validation warning for intyg " + certificateId + ": Person-id " + personNumber
                    + " is lacking a separating dash - corrected.");
        }
        // Check patient o.i.d.
        if (patient.getPersonId().getRoot() == null || !PATIENT_ID_OIDS.contains(patient.getPersonId().getRoot())) {
            validationErrors.add(String.format("Wrong o.i.d. for Patient Id! Should be %s", Joiner.on(" or ").join(PATIENT_ID_OIDS)));
        }

        // Check format of patient id (has to be a valid personnummer)
        if (!Pattern.matches(PERSON_NUMBER_REGEX, personNumber)) {
            validationErrors.add("Wrong format for person-id! Valid format is YYYYMMDD-XXXX or YYYYMMDD+XXXX.");
        }
        return true;
    }

    // CHECKSTYLE:OFF MagicNumber
    private static String formatWithDash(String personNumber) {
        return personNumber.substring(0, 8) + "-" + personNumber.substring(8);
    }
    // CHECKSTYLE:ON MagicNumber
}
